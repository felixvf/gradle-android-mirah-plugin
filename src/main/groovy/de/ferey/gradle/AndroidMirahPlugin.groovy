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
import org.ysb33r.gradle.mirah.MirahSourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.Copy
import org.gradle.api.file.DuplicatesStrategy
import org.ysb33r.gradle.mirah.MirahCompile
import org.gradle.util.ConfigureUtil

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicReference
/**
 * AndroidMirahPlugin adds mirah language support to official gradle android plugin.
 */
public class AndroidMirahPlugin implements Plugin<Project> {
    final static String DEFAULT_MIRAH_VERSION = "0.2.1"
    
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
            def allVariants = mainVariants + androidExtension.testVariants + androidExtension.unitTestVariants
            allVariants.each { variant ->
                addAndroidMirahCompileTask(variant)
            }
        }

        project.tasks.findByName("preBuild").doLast {
            FileUtils.forceMkdir(baseWorkDir)
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
    static String mirahVersionFromClasspath(Iterable<File> classpath) {
        def urls = classpath.collect { it.toURI().toURL() }
        def classLoader = new URLClassLoader(urls.toArray(new URL[0]))
        try {
            def propertiesClass
            try {
                propertiesClass = classLoader.loadClass("org.mirah.tool.MirahArguments")
            } catch (ClassNotFoundException e) {
                return null
            }
            def versionNumber = propertiesClass.VERSION
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
            sourceSet.convention.plugins.mirah = new MirahSourceSet(sourceSet.name + "_AndroidMirahPlugin", fileResolver)
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
            acc + val.mirah
        }
        def mirahCompileTask = addAndroidMirahCompileTask(javaCompileTask, mirahSources, variant)
    }

    /**
     * Updates AndroidPlugin's compilation task to support mirah.
     *
     * Each JavaCompile task gets also a MirahCompile task.
     *
     * This is currently implemented as follows:
     * * The JavaCompile task stays, but gets a different destination directory "_java".
     * * A new MirahCompile task is generated, which also gets a different destination directory "_mirah". The MirahCompile task has the "_java" destination directory in its classpath.
     * * A new Copy copies the files from the "_java" destination directory and the "_mirah" destination directory into the original destination directory.
     *
     * This means that Mirah code can access the .class files of Java code.
     * However, Java code cannot access the .class files of Mirah code (the compiler currently does not support joint compilation, and the Mirah code is compiled after the Java code is compiled).
     * Because all .class files are finally stored in the original destination directory, downstream tasks should work as if the .class files were generated by the JavaCompile task alone. 
     *
     */
    Object addAndroidMirahCompileTask(JavaCompile javaCompileTask, List<File> mirahSources, variant) {
        def taskName = javaCompileTask.name.replaceFirst('Javac$', "Mirahc")

        def mirahVersion = mirahVersionFromClasspath(project.getBuildscript().getConfigurations().getAt("classpath"))
        if (mirahVersion) {
            mirahVersion = mirahVersion.replace(".dev","-SNAPSHOT")
            project.logger.info("mirah version=$mirahVersion detected")
        } else {
            mirahVersion = DEFAULT_MIRAH_VERSION
            project.logger.info("mirah version=$mirahVersion assumed")
        }
        def originalDestinationDir          = javaCompileTask.destinationDir
        def intermediateJavaDestinationDir  = new File(originalDestinationDir.parent,"${originalDestinationDir.name}_java")
        def intermediateMirahDestinationDir = new File(originalDestinationDir.parent,"${originalDestinationDir.name}_mirah")
        
        javaCompileTask.destinationDir = intermediateJavaDestinationDir
        /*
        	Register intermediateMirahDestinationDir as an output of the JavaCompileTask.
        	Actually, it is just the output of the MirahCompile task, but com.android.build.gradle.tasks.factory.AndroidUnitTest.ConfigAction.execute() induces
        	a calls to javaCompileTask.getOutputs().getFiles() _after_ we are called here.
        	Hence, the evaluation will not yield the originalDestinationDir, but the intermediateJavaDestinationDir, which does not contain our output.
        	Thus, we add intermediateMirahDestinationDir to the result of javaCompileTask.getOutputs().
        	We make the contents of intermediateJavaDestinationDir as well as intermediateMirahDestinationDir equivalent to the contents of originalDestinationDir (below in copyMergeClassFilesTask).
        	Hence, either using both intermediateJavaDestinationDir and intermediateMirahDestinationDir as classpath entries, or using originalDestinationDir as classpath entry, should not make a difference.
        */
        javaCompileTask.outputs.dir(intermediateMirahDestinationDir) 
        
        def compilerConfigurationName = "androidMirahPluginMirahCompilerFor" + javaCompileTask.name
        def compilerConfiguration = project.configurations.findByName(compilerConfigurationName)
        if (!compilerConfiguration) {
            compilerConfiguration = project.configurations.create(compilerConfigurationName)
            project.dependencies.add(compilerConfigurationName, "org.mirah:mirah:$mirahVersion")
        }
        def mirahCompileTask = project.tasks.create(taskName, MirahCompile)
        mirahCompileTask.source = mirahSources
        mirahCompileTask.destinationDir      = intermediateMirahDestinationDir
        mirahCompileTask.sourceCompatibility = javaCompileTask.sourceCompatibility
        mirahCompileTask.targetCompatibility = javaCompileTask.targetCompatibility
        mirahCompileTask.classpath           = javaCompileTask.classpath + project.files(intermediateJavaDestinationDir)
        mirahCompileTask.bootClasspath = project.files(androidPlugin.androidBuilder.getBootClasspath(false))
        mirahCompileTask.mirahClasspath = compilerConfiguration.asFileTree
        if (extension.addparams) {
            mirahCompileTask.mirahCompileOptions.additionalParameters = [extension.addparams]
        }
        
        def copyMergeClassFilesTask = project.tasks.create(javaCompileTask.name.replaceFirst('Javac$',"JavaMirahMerge"), Copy)
        copyMergeClassFilesTask.from(intermediateJavaDestinationDir)
        copyMergeClassFilesTask.from(intermediateMirahDestinationDir)
        copyMergeClassFilesTask.into(originalDestinationDir)
        copyMergeClassFilesTask.duplicatesStrategy = DuplicatesStrategy.FAIL // duplicates are an error

        mirahCompileTask.dependsOn(javaCompileTask)
        copyMergeClassFilesTask.dependsOn(mirahCompileTask)
        javaCompileTask.finalizedBy(copyMergeClassFilesTask)
        
        return mirahCompileTask;
    }
}
