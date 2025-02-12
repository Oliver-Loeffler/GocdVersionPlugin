# GocdVersionPlugin for Gradle

Auto-generating version numbers for [GoCD](https://www.gocd.org/)  automated builds from [Git](https://git-scm.com/) tags.
----

This plugin has the purpose to work with [GoCD](https://www.gocd.org/) in order to detect if a build runs in a GoCD pipeline on a GoCD build agent.
Depending on that, the project version identifier will be updated. There are no dependencies to other plugins. The only external dependency is Eclipses [JGit](https://www.eclipse.org/jgit/).
Versions `0.0.2` till `0.0.11` are working with Java-8 and newer. Since version 0.0.12, Java 11 is now again the baseline.

## Combining Git with GoCD

GoCD uses pipelines to build artifacts. Each pipeline has a unique and independent environment and the pipeline instances are also versioned as well using the  environment variable `GO_PIPELINE_COUNTER`. With that it is possible to use the build count including the number of attempts for each build/pipeline instance within a version number. 

This means, that just re-running the build pipeline, a new version number is created, which will help then to separate if a problem is caused by an erroneous build or by a bug in source code. Another point is, that when the GoCD environment `GO_PIPELINE_COUNTER` is not detected, a deployment operation could be suppressed. This means, that e.g. local builds cannot be published into remote repositories.

# How does the Plugin work?

As of now for a Java library the Gradle code (Groovy DSL) looks as follows:

```groovy
plugins {
    id "java-library"
 // id "com.palantir.git-version" version "0.15.0"
    id 'net.raumzeitfalle.gradle.gocdversion' version '0.0.11'
}

// Git details are provided by git-version plugin
// version = gocdVersion(versionDetails().lastTag+'.'+versionDetails().commitDistance,
//                       versionDetails().lastTag+'.'+versionDetails().commitDistance+'-'+versionDetails().gitHash).build()

version = gocdVersion().build()
println("Automatic version=" + version)
// There is no need anymore to run other plugins as this one has native git support via JGit.
// Hence, git tags can be used to generate version numbers automatically.

```

In this example I've previously used the [gradle-git-version plugin](https://github.com/palantir/gradle-git-version) plugin to build the basic version number for a project. A version number might look like: MAJOR.MINOR.PATCH (e.g. 2.0.11).
The GocdVersionPlugin provides support for Git using JGit (since 0.0.6), hence another 3rd party plugin is no longer needed, so a version number can be directly generated from Git tag.
This indeed requires, that a useful tag exists. The schema for version number creation is configurable.

The idea is, that the PATCH version can be directly taken from Git history. One could `tag` a specific git commit with an appropriate `MAJOR.MINOR` value.
The count of commits after the last `tag` then represents the `PATCH` version.

When calling `gocdVersion()` without arguments, the plugin will attempt to obtain the latest Git tag and also commit distance to the head. I'm using it in various production scenarios and it has proven to work well.
However, it is mandatory to make the Git clones large enough, to have the commit tags available. 

Approach:
* declare `MAJOR.MINOR` using `git tag` command
* use commit count after last `tag` as `PATCH` version
* A new, automatically created version could look like: MAJOR.MINOR.PATCH.__BUILD__ (or MAJOR.MINOR.PATCH.__GO-PIPELINE-COUNT__)

The `gocdVersion` function takes 2 arguments, the first one is the string to be used as version for automated builds, the second one for manual or local builds.
So the effective version for a project can be different for atomated builds and manually initiated builds.

As of version `0.0.11`, it is now possible to define a regular expression to match the desired Git tags. As the version number often is used in file names, one must ensure that the git tag does not contain characters which are illegal for use in file system paths. 

With the configuration in the example, the created version number looks as follows:

### Example 1, automated build in a GoCD pipeline
* git tag such as `1.0` exists
* there are NO commits behind `1.0`, the commit distance is 0
* the build is executed in a GoCD pipeline with `GO_PIPELINE_COUNTER=153.1`):

```
# (tag.commit-distance.pipeline-counter)
version = 1.0.0.153.1
```

### Example 2, manual (local) build on developer machine
* git tag such as `1.0` exists
* there are NO commits behind `1.0`, the commit distance is 0
* the build is executed on a computer with name ENIAC, there is no environment variable `GO_PIPELINE_COUNTER=xyz`):

```
# (tag.commit-distance-hash.computername.timestamp)
version = 1.0.0-96bfec85d4.ENIAC.20210906000609  
```

Using the `gocdVersion` closure the version schema can be configured.

```groovy

// Defaults
gocdVersion {
    appendPipelineCounterToAutomatedBuilds = true
    appendStageCounterToAutomatedBuilds = true
    appendComputerNameToLocalBuilds = true
    appendTimestampToLocalBuilds = true
    
    defaultTimestampPattern = 'yyyyMMddHHmmss'
    timestampPattern = 'yyyyMMddHHmmss'

    missingGitCommitFallbackTag = '<notag>'
    suitableTagRegex = '^\\d*([.]\\d*)?([.]\\d*)?$'
}
```

To generate the version number from git tags, one needs to add following line to the Gradle build at the point where the version is defined:

```groovy
gocdVersion {
	suitableTagRegex = '^\\d*([.]\\d*)?([.]\\d*)?$'
}

version = gocdVersion().build()
```

The effective version number schema can be verified using the `printGocdEnvironment` task on commandline.

```shell
> ./gradlew printGocdEnvironment
---------------------------------------------------------
Gocd Pipeline Environment
---------------------------------------------------------
GO_SERVER_URL                   = http://192.168.1.2:8053
GO_PIPELINE_GROUP_NAME          = engineering
GO_ENVIRONMENT_NAME             = windows-java-20
GO_TRIGGER_USER                 = doejohn
GO_AGENT_RESOURCES              = windows,java20,special
GO_PIPELINE_NAME                = test-build
GO_PIPELINE_COUNTER             = 12.1
GO_PIPELINE_LABEL               = 1.12.1
GO_STAGE_NAME                   = build
GO_STAGE_COUNTER                = 1
GO_JOB_NAME                     = build
COMPUTERNAME                    = ENIAC
---------------------------------------------------------
Project version                 = 20220526.2.12.1
Is automated build?             = true
Automated Version using Git Tag = 20220526.2
---------------------------------------------------------

```

### Example 3, generating a MSI/WIX compatible version number from Git tag
* git tag such as `20220930` exists
* there were 12 commits after the tag, so the commit distance is 12

```
# (tag.commit-distance.pipeline-counter)
def jpkgversion = jpackageVersion(versionDetails().lastTag+'.'+versionDetails().commitDistance).build()
println(jpkgversion)
22.39.12
```

### Example 4, accessing GoCD environment variables and Git tag in a Gradle build

```groovy

println( gocdEnvironmentName() )
println( gocdPipelineGroupName() )
println( gocdPipelineName() )
println( gitTagVersion() )
println( gocdPipelineCounter() )
println( gocdPipelineLabel() )
println( gocdStageName() )
println( gocdStageCounter() )
println( gocdJobName() )
println( gocdComputerName() )
println( gocdTriggerUser() )
println( isAutomatedBuild() )
println( gocdGitMaterialBranchName("GIT_MATERIAL_NAME") )
println( gocdComputerName() )
println( gocdServerUrl() )
println( gocdVersion() )
println( gocdVersion("versionForAutomatedBuilds", "versionForManualBuilds") )

```

### Example 5, simplest way to get an automatic version number from Git tag

* Version schema: `git tag`.`commit count since tag` (e.g. `1.0.9`)
* The nice thing is now, that no other plug in is needed and it now works for sub-projects as well.

```groovy
buildscript {
  repositories {
      mavenLocal()
  }
  dependencies {
      classpath 'net.raumzeitfalle.gradle.gocd:GocdVersionPlugin:0.0.8'
  }
}

plugins {
    id 'java-library'
    id 'net.raumzeitfalle.gradle.gocdversion' version '0.0.8'
}

version = gocdVersion().build()
println("Automatic version=" + version)

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.1'
    api 'org.apache.commons:commons-math3:3.6.1'
    implementation 'com.google.guava:guava:30.1.1-jre'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

## License

Copyright 2021, 2025 Oliver Löffler

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
