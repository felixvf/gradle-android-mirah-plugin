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
import org.gradle.api.internal.tasks.DefaultScalaSourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.util.ConfigureUtil

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicReference
/**
 * AndroidScalaPlugin adds scala language support to official gradle android plugin.
 */
public class AndroidScalaPlugin implements Plugin<Project> {
    private final FileResolver fileResolver
    @VisibleForTesting
    final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private Project project
    private Object androidPlugin
    private Object androidExtension
    private boolean isLibrary
    private File baseWorkDir
    private final AndroidScalaPluginExtension extension = new AndroidScalaPluginExtension()

    /**
     * Creates a new AndroidScalaPlugin with given file resolver.
     *
     * @param fileResolver the FileResolver
     */
    @Inject
    public AndroidScalaPlugin(FileResolver fileResolver) {
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
        this.baseWorkDir = new File(project.buildDir, "android-scala")
        updateAndroidExtension()
        updateAndroidSourceSetsExtension()
        androidExtension.buildTypes.whenObjectAdded { updateAndroidSourceSetsExtension() }
        androidExtension.productFlavors.whenObjectAdded { updateAndroidSourceSetsExtension() }
        androidExtension.signingConfigs.whenObjectAdded { updateAndroidSourceSetsExtension() }

        project.afterEvaluate {
            updateAndroidSourceSetsExtension()
            androidExtension.sourceSets.each { it.java.srcDirs(it.scala.srcDirs) }
            def mainVariants = (isLibrary ? androidExtension.libraryVariants : androidExtension.applicationVariants)
            def allVariants = mainVariants + androidExtension.testVariants // + androidExtension.unitTestVariants
            allVariants.each { variant ->
                addAndroidScalaCompileTask(variant)
            }
        }

        project.tasks.findByName("preBuild").doLast {
            FileUtils.forceMkdir(baseWorkDir)
        }

        project.tasks.withType(ScalaCompile) {
            scalaCompileOptions.useAnt = false
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
                throw new ProjectConfigurationException("Please apply 'com.android.application' or 'com.android.library' plugin before applying 'android-scala' plugin", null)
            }
        }()
        apply(project, androidPlugin, project.extensions.getByName("android"), isLibrary)
    }

    /**
     * Returns scala version from scala-library in given classpath.
     *
     * @param classpath the classpath contains scala-library
     * @return scala version
     */
    static String scalaVersionFromClasspath(Collection<File> classpath) {
        def urls = classpath.collect { it.toURI().toURL() }
        def classLoader = new URLClassLoader(urls.toArray(new URL[0]))
        try {
            def propertiesClass
            try {
                propertiesClass = classLoader.loadClass("scala.util.Properties\$")
            } catch (ClassNotFoundException e) {
                return null
            }
            def versionNumber = propertiesClass.MODULE$.scalaProps["maven.version.number"]
            return new String(versionNumber) // Remove reference from ClassLoader
        } finally {
            if (classLoader instanceof Closeable) {
                classLoader.close()
            }
        }
    }

    /**
     * Updates AndroidPlugin's root extension to work with AndroidScalaPlugin.
     */
    void updateAndroidExtension() {
        androidExtension.metaClass.getScala = { extension }
        androidExtension.metaClass.scala = { configureClosure ->
            ConfigureUtil.configure(configureClosure, extension)
            androidExtension
        }
    }

    /**
     * Updates AndroidPlugin's sourceSets extension to work with AndroidScalaPlugin.
     */
    void updateAndroidSourceSetsExtension() {
        androidExtension.sourceSets.each { sourceSet ->
            if (sourceDirectorySetMap.containsKey(sourceSet.name)) {
                return
            }
            def include = "**/*.scala"
            sourceSet.java.filter.include(include);
            sourceSet.convention.plugins.scala = new DefaultScalaSourceSet(sourceSet.name + "_AndroidScalaPlugin", fileResolver)
            def scala = sourceSet.scala
            scala.filter.include(include);
            def scalaSrcDir = ["src", sourceSet.name, "scala"].join(File.separator)
            scala.srcDir(scalaSrcDir)
            sourceDirectorySetMap[sourceSet.name] = scala
        }
    }

    /**
     * Updates AndroidPlugin's compilation task to support scala.
     */
    void addAndroidScalaCompileTask(Object variant) {
        def javaCompileTask = variant.javaCompile
        def scalaSources = variant.variantData.variantConfiguration.sortedSourceProviders.inject([]) { acc, val ->
            acc + val.java.sourceFiles
        }
        addAndroidScalaCompileTask(javaCompileTask, scalaSources)

        def flavor = variant.flavorName.capitalize()
        def buildType = variant.buildType.name.capitalize()
        def files = [
            "",
            buildType,
            flavor,
            flavor + buildType,
        ].unique().collect { project.fileTree("src/test$it/scala") }.inject([]) { list, dir ->
            list + dir
        }

        def unitTestTaskName = javaCompileTask.name.replaceFirst(/Java$/, 'UnitTestJava')
        project.getTasksByName(unitTestTaskName, false).each {
            addAndroidScalaCompileTask(it, files)
        }
    }

    /**
     * Updates AndroidPlugin's compilation task to support scala.
     */
    void addAndroidScalaCompileTask(JavaCompile javaCompileTask, List<File> scalaSources) {
        def taskName = javaCompileTask.name.replace("Java", "Scala")
        def workDir = new File([baseWorkDir, "tasks", taskName].join(File.separator))

        // To prevent locking classes.jar by JDK6's URLClassLoader
        def libraryClasspath = javaCompileTask.classpath.grep { it.name != "classes.jar" }
        def scalaVersion = scalaVersionFromClasspath(libraryClasspath)
        if (!scalaVersion) {
            return
        }
        project.logger.info("scala-library version=$scalaVersion detected")
        def zincConfigurationName = "androidScalaPluginZincFor" + javaCompileTask.name
        def zincConfiguration = project.configurations.findByName(zincConfigurationName)
        if (!zincConfiguration) {
            zincConfiguration = project.configurations.create(zincConfigurationName)
            project.dependencies.add(zincConfigurationName, "com.typesafe.zinc:zinc:0.3.7")
        }
        def compilerConfigurationName = "androidScalaPluginScalaCompilerFor" + javaCompileTask.name
        def compilerConfiguration = project.configurations.findByName(compilerConfigurationName)
        if (!compilerConfiguration) {
            compilerConfiguration = project.configurations.create(compilerConfigurationName)
            project.dependencies.add(compilerConfigurationName, "org.scala-lang:scala-compiler:$scalaVersion")
        }
        def scalaCompileTask = project.tasks.create(taskName, ScalaCompile)
        scalaCompileTask.source = scalaSources
        scalaCompileTask.destinationDir = javaCompileTask.destinationDir
        scalaCompileTask.sourceCompatibility = javaCompileTask.sourceCompatibility
        scalaCompileTask.targetCompatibility = javaCompileTask.targetCompatibility
        scalaCompileTask.scalaCompileOptions.encoding = javaCompileTask.options.encoding
        scalaCompileTask.classpath = javaCompileTask.classpath + project.files(androidPlugin.androidBuilder.bootClasspath)
        scalaCompileTask.scalaClasspath = compilerConfiguration.asFileTree
        scalaCompileTask.zincClasspath = zincConfiguration.asFileTree
        scalaCompileTask.scalaCompileOptions.incrementalOptions.analysisFile = new File(workDir, "analysis.txt")
        if (extension.addparams) {
            scalaCompileTask.scalaCompileOptions.additionalParameters = [extension.addparams]
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
            scalaCompileTask.source = [] + new TreeSet(scalaCompileTask.source.collect { it } + javaCompileTask.source.collect { it }) // unique
            def noisyProperties = ["compiler", "includeJavaRuntime", "incremental", "optimize", "useAnt"]
            InvokerHelper.setProperties(scalaCompileTask.options,
                javaCompileTask.options.properties.findAll { !noisyProperties.contains(it.key) })
            noisyProperties.each { property ->
                // Suppress message from deprecated/experimental property as possible
                if (!javaCompileTask.options.hasProperty(property) || !scalaCompileTask.options.hasProperty(property)) {
                    return
                }
                if (scalaCompileTask.options[property] != javaCompileTask.options[property]) {
                    scalaCompileTask.options[property] = javaCompileTask.options[property]
                }
            }
            scalaCompileTask.execute()
            project.logger.lifecycle(scalaCompileTask.path)
        }
    }
}
