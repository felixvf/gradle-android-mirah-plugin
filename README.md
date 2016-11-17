# gradle-android-mirah-plugin

gradle-android-mirah-plugin adds mirah language support to official gradle android plugin.
See also sample projects at https://github.com/felixvf/gradle-android-mirah-plugin/tree/master/sample

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Supported versions](#supported-versions)
- [Installation](#installation)
  - [1. Add buildscript's dependency](#1-add-buildscripts-dependency)
  - [2. Apply plugin](#2-apply-plugin)
  - [3. Add mirah-library dependency](#3-add-mirah-library-dependency)
  - [4. Put mirah source files](#4-put-mirah-source-files)
  - [5. Implement a workaround for DEX 64K Methods Limit](#5-implement-a-workaround-for-dex-64k-methods-limit)
    - [5.1. Option 1: Use ProGuard](#51-option-1-use-proguard)
    - [5.2. Option 2: Use MultiDex](#52-option-2-use-multidex)
      - [5.2.1. Setup application class if you use customized one](#521-setup-application-class-if-you-use-customized-one)
- [Configuration](#configuration)
- [Complete example of build.gradle with manually configured MultiDexApplication](#complete-example-of-buildgradle-with-manually-configured-multidexapplication)
- [Changelog](#changelog)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Supported versions

| Mirah          | Gradle       | Android Plugin | compileSdkVersion | buildToolsVersion |
| -------------- | ------------ | -------------- | ----------------- | ----------------- |
| 0.1.4 - 0.2.1  | 2.2.1  - 2.5 | 1.1.0 - 1.1.3  | 21 - 22           | 21.1.1 - 22.0.1   |

## Installation

### 1. Add buildscript's dependency

`build.gradle`
```groovy
buildscript {
    dependencies {
        classpath "com.android.tools.build:gradle:1.1.3"
        classpath "de.ferey.gradle:gradle-android-mirah-plugin:1.4"
    }
}
```

### 2. Apply plugin

`build.gradle`
```groovy
apply plugin: "com.android.application"
apply plugin: "de.ferey.android-mirah"
```

### 3. Put mirah source files

Default locations are src/main/mirah, src/androidTest/mirah.
You can customize those directories similar to java.

`build.gradle`
```groovy
android {
    sourceSets {
        main {
            mirah {
                srcDir "path/to/main/mirah" // default: "src/main/mirah"
            }
        }

        androidTest {
            mirah {
                srcDir "path/to/androidTest/mirah" // default: "src/androidTest/mirah"
            }
        }
    }
}
```


## Complete example of build.gradle with manually configured MultiDexApplication

`build.gradle`
```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath "com.android.tools.build:gradle:1.1.3"
        classpath "de.ferey.gradle:gradle-android-mirah-plugin:1.4"
    }
}

repositories {
    jcenter()
}

apply plugin: "com.android.application"
apply plugin: "de.ferey.android-mirah"

android {
    compileSdkVersion "android-22"
    buildToolsVersion "22.0.1"

    defaultConfig {
        targetSdkVersion 22
        testInstrumentationRunner "com.android.test.runner.MultiDexTestRunner"
        versionCode 1
        versionName "1.0"
        multiDexEnabled true
    }

    productFlavors {
        dev {
            minSdkVersion 21 // To reduce compilation time
        }

        prod {
            minSdkVersion 8
        }
    }

    sourceSets {
        main {
            mirah {
                srcDir "path/to/main/mirah" // default: "src/main/mirah"
            }
        }

        androidTest {
            mirah {
                srcDir "path/to/androidTest/mirah" // default: "src/androidTest/mirah"
            }
        }
    }
}

dependencies {
    compile "com.android.support:multidex:1.0.1"
    androidTestCompile "com.android.support:multidex-instrumentation:1.0.1", { exclude module: "multidex" }
}
```

## Changelog
- 1.4 fork [gradle-android-mirah-plugin](https://github.com/felixvf/gradle-android-mirah-plugin) from [gradle-android-scala-plugin](https://github.com/saturday06/gradle-android-mirah-plugin)
- 1.4 Support android plugin 1.1.3. Manual configuration for dex task is now unnecessary (contributed by [sgrif](https://github.com/sgrif))
- 1.3.2 Fix unexpected annotation processor's warnings
- 1.3.1 Support android plugin 0.12.2
- 1.3 Incremental compilation support in mirah 2.11
- 1.2.1 Fix binary compatibility with JDK6
- 1.2 Incremental compilation support in mirah 2.10 / Flavors support
- 1.1 MultiDexApplication support
- 1.0 First release
