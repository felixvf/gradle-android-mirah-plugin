/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ferey.gradle
import com.google.common.annotations.VisibleForTesting
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.DefaultMirahSourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.mirah.MirahCompile
import org.gradle.util.ConfigureUtil

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicReference
/**
 * AndroidMirahPlugin adds mirah language support to official gradle android plugin.
 */
public class AndroidMirahPlugin implements Plugin<Project> {
    private final FileResolver fileResolver
    @VisibleForTesting
    final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private Project project
    private Object androidPlugin
    private Object androidExtension
    private boolean isLibrary
    private File baseWorkDir
    private final AndroidMirahPluginExtension extension = new AndroidMirahPluginExtension()

    /**
     * Creates a new AndroidMirahPlugin with given file resolver.
     *
     * @param fileResolver the FileResolver
     */
    @Inject
    public AndroidMirahPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    /**
     * Registers the plugin to current project.
     *
     * @param project currnet project
     */
    void apply(Project project, Object androidPlugin, Object androidExtension, Boolean isLibrary) {
        this.project = project
        this.androidPlugin = androidPlugin
        this.androidExtension = androidExtension
        this.isLibrary = isLibrary
        this.baseWorkDir = new File(project.buildDir, "android-mirah")
        updateAndroidExtension()
        updateAndroidSourceSetsExtension()
        androidExtension.buildTypes.whenObjectAdded { updateAndroidSourceSetsExtension() }
        androidExtension.productFlavors.whenObjectAdded { updateAndroidSourceSetsExtension() }
        androidExtension.signingConfigs.whenObjectAdded { updateAndroidSourceSetsExtension() }

        project.afterEvaluate {
            updateAndroidSourceSetsExtension()
            androidExtension.sourceSets.each { it.java.srcDirs(it.mirah.srcDirs) }
            def mainVariants = (isLibrary ? androidExtension.libraryVariants : androidExtension.applicationVariants)
            def allVariants = mainVariants + androidExtension.testVariants // + androidExtension.unitTestVariants
            allVariants.each { variant ->
                addAndroidMirahCompileTask(variant)
            }
        }

        project.tasks.findByName("preBuild").doLast {
            FileUtils.forceMkdir(baseWorkDir)
        }

        project.tasks.withType(MirahCompile) {
            mirahCompileOptions.useAnt = false
        }
    }

    /**
     * Registers the plugin to current project.
     *
     * @param project currnet project
     */
    public void apply(Project project) {
        def (androidPlugin, Boolean isLibrary) = {
            if (project.plugins.hasPlugin("com.android.application")) {
                [project.plugins.findPlugin("com.android.application"), false]
            } else if (project.plugins.hasPlugin("com.android.library")) {
                [project.plugins.findPlugin("com.android.library"), true]
            } else {
                throw new ProjectConfigurationException("Please apply 'com.android.application' or 'com.android.library' plugin before applying 'android-mirah' plugin", null)
            }
        }()
        apply(project, androidPlugin, project.extensions.getByName("android"), isLibrary)
    }

    /**
     * Returns mirah version from mirah-library in given classpath.
     *
     * @param classpath the classpath contains mirah-library
     * @return mirah version
     */
    static String mirahVersionFromClasspath(Collection<File> classpath) {
        def urls = classpath.collect { it.toURI().toURL() }
        def classLoader = new URLClassLoader(urls.toArray(new URL[0]))
        try {
            def propertiesClass
            try {
                propertiesClass = classLoader.loadClass("mirah.util.Properties\$")
            } catch (ClassNotFoundException e) {
                return null
            }
            def versionNumber = propertiesClass.MODULE$.mirahProps["maven.version.number"]
            return new String(versionNumber) // Remove reference from ClassLoader
        } finally {
            if (classLoader instanceof Closeable) {
                classLoader.close()
            }
        }
    }

    /**
     * Updates AndroidPlugin's root extension to work with AndroidMirahPlugin.
     */
    void updateAndroidExtension() {
        androidExtension.metaClass.getMirah = { extension }
        androidExtension.metaClass.mirah = { configureClosure ->
            ConfigureUtil.configure(configureClosure, extension)
            androidExtension
        }
    }

    /**
     * Updates AndroidPlugin's sourceSets extension to work with AndroidMirahPlugin.
     */
    void updateAndroidSourceSetsExtension() {
        androidExtension.sourceSets.each { sourceSet ->
            if (sourceDirectorySetMap.containsKey(sourceSet.name)) {
                return
            }
            def include = "**/*.mirah"
            sourceSet.java.filter.include(include);
            sourceSet.convention.plugins.mirah = new DefaultMirahSourceSet(sourceSet.name + "_AndroidMirahPlugin", fileResolver)
            def mirah = sourceSet.mirah
            mirah.filter.include(include);
            def mirahSrcDir = ["src", sourceSet.name, "mirah"].join(File.separator)
            mirah.srcDir(mirahSrcDir)
            sourceDirectorySetMap[sourceSet.name] = mirah
        }
    }

    /**
     * Updates AndroidPlugin's compilation task to support mirah.
     */
    void addAndroidMirahCompileTask(Object variant) {
        def javaCompileTask = variant.javaCompile
        def mirahSources = variant.variantData.variantConfiguration.sortedSourceProviders.inject([]) { acc, val ->
            acc + val.java.sourceFiles
        }
        addAndroidMirahCompileTask(javaCompileTask, mirahSources)

        def flavor = variant.flavorName.capitalize()
        def buildType = variant.buildType.name.capitalize()
        def files = [
            "",
            buildType,
            flavor,
            flavor + buildType,
        ].unique().collect { project.fileTree("src/test$it/mirah") }.inject([]) { list, dir ->
            list + dir
        }

        def unitTestTaskName = javaCompileTask.name.replaceFirst(/Java$/, 'UnitTestJava')
        project.getTasksByName(unitTestTaskName, false).each {
            addAndroidMirahCompileTask(it, files)
        }
    }

    /**
     * Updates AndroidPlugin's compilation task to support mirah.
     */
    void addAndroidMirahCompileTask(JavaCompile javaCompileTask, List<File> mirahSources) {
        def taskName = javaCompileTask.name.replace("Java", "Mirah")
        def workDir = new File([baseWorkDir, "tasks", taskName].join(File.separator))

        // To prevent locking classes.jar by JDK6's URLClassLoader
        def libraryClasspath = javaCompileTask.classpath.grep { it.name != "classes.jar" }
        def mirahVersion = mirahVersionFromClasspath(libraryClasspath)
        if (!mirahVersion) {
            return
        }
        project.logger.info("mirah-library version=$mirahVersion detected")
        def zincConfigurationName = "androidMirahPluginZincFor" + javaCompileTask.name
        def zincConfiguration = project.configurations.findByName(zincConfigurationName)
        if (!zincConfiguration) {
            zincConfiguration = project.configurations.create(zincConfigurationName)
            project.dependencies.add(zincConfigurationName, "com.typesafe.zinc:zinc:0.3.7")
        }
        def compilerConfigurationName = "androidMirahPluginMirahCompilerFor" + javaCompileTask.name
        def compilerConfiguration = project.configurations.findByName(compilerConfigurationName)
        if (!compilerConfiguration) {
            compilerConfiguration = project.configurations.create(compilerConfigurationName)
            project.dependencies.add(compilerConfigurationName, "org.mirah:mirah-compiler:$mirahVersion")
        }
        def mirahCompileTask = project.tasks.create(taskName, MirahCompile)
        mirahCompileTask.source = mirahSources
        mirahCompileTask.destinationDir = javaCompileTask.destinationDir
        mirahCompileTask.sourceCompatibility = javaCompileTask.sourceCompatibility
        mirahCompileTask.targetCompatibility = javaCompileTask.targetCompatibility
        mirahCompileTask.mirahCompileOptions.encoding = javaCompileTask.options.encoding
        mirahCompileTask.classpath = javaCompileTask.classpath + project.files(androidPlugin.androidBuilder.bootClasspath)
        mirahCompileTask.mirahClasspath = compilerConfiguration.asFileTree
        mirahCompileTask.zincClasspath = zincConfiguration.asFileTree
        mirahCompileTask.mirahCompileOptions.incrementalOptions.analysisFile = new File(workDir, "analysis.txt")
        if (extension.addparams) {
            mirahCompileTask.mirahCompileOptions.additionalParameters = [extension.addparams]
        }

        def dummyDestinationDir = new File(workDir, "javaCompileDummyDestination") // TODO: More elegant way
        def dummySourceDir = new File(workDir, "javaCompileDummySource") // TODO: More elegant way
        def javaCompileOriginalDestinationDir = new AtomicReference<File>()
        def javaCompileOriginalSource = new AtomicReference<FileCollection>()
        def javaCompileOriginalOptionsCompilerArgs = new AtomicReference<List<String>>()
        javaCompileTask.doFirst {
            // Disable compilation
            javaCompileOriginalDestinationDir.set(javaCompileTask.destinationDir)
            javaCompileOriginalSource.set(javaCompileTask.source)
            javaCompileTask.destinationDir = dummyDestinationDir
            if (!dummyDestinationDir.exists()) {
                FileUtils.forceMkdir(dummyDestinationDir)
            }
            def dummySourceFile = new File(dummySourceDir, "Dummy.java")
            if (!dummySourceFile.exists()) {
                FileUtils.forceMkdir(dummySourceDir)
                dummySourceFile.withWriter { it.write("class Dummy{}") }
            }
            javaCompileTask.source = [dummySourceFile]
            def compilerArgs = javaCompileTask.options.compilerArgs
            javaCompileOriginalOptionsCompilerArgs.set(compilerArgs)
            javaCompileTask.options.compilerArgs = compilerArgs +  "-proc:none"
        }
        javaCompileTask.outputs.upToDateWhen { false }
        javaCompileTask.doLast {
            FileUtils.deleteDirectory(dummyDestinationDir)
            javaCompileTask.destinationDir = javaCompileOriginalDestinationDir.get()
            javaCompileTask.source = javaCompileOriginalSource.get()
            javaCompileTask.options.compilerArgs = javaCompileOriginalOptionsCompilerArgs.get()

            // R.java is appended lazily
            mirahCompileTask.source = [] + new TreeSet(mirahCompileTask.source.collect { it } + javaCompileTask.source.collect { it }) // unique
            def noisyProperties = ["compiler", "includeJavaRuntime", "incremental", "optimize", "useAnt"]
            InvokerHelper.setProperties(mirahCompileTask.options,
                javaCompileTask.options.properties.findAll { !noisyProperties.contains(it.key) })
            noisyProperties.each { property ->
                // Suppress message from deprecated/experimental property as possible
                if (!javaCompileTask.options.hasProperty(property) || !mirahCompileTask.options.hasProperty(property)) {
                    return
                }
                if (mirahCompileTask.options[property] != javaCompileTask.options[property]) {
                    mirahCompileTask.options[property] = javaCompileTask.options[property]
                }
            }
            mirahCompileTask.execute()
            project.logger.lifecycle(mirahCompileTask.path)
        }
    }
}
