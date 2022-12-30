# Project Idea: GocdVersionPlugin for Gradle

Warning, this is very experimental and not meant to be used in production environments.
It is also tailored for a special use case and for me it is a learning experience on how Gradle plugins work.

When continuous delivery is used, it can be valuable to have reliable version numbers which include the _commit ID_ and a _build ID_ pointing to a certain status in source code history and build pipeline run.

This can be achieved by using the [gradle-git-version plugin](https://github.com/palantir/gradle-git-version). In some cases, one would like to distinguish between local builds (e.g. performed on a developer PC) and automated builds in a CI/CD environment - this is the task of this plugin. 

This plugin has the purpose to work with GoCD in order to detect if a build runs in a GoCD pipeline on a GoCD build agent.
Depending on that, the project version identifier will be updated.

There are no dependencies to other plugins.

Version `0.0.1` works with Java-11 or newer, version `0.0.2` works again with Java-8 and newer.

## Combining Git with GoCD

GoCD uses pipelines to build artifacts. Each pipeline has a unique and independent environment and the pipeline instances are also versioned as well using the  environment variable `GO_PIPELINE_COUNTER`. With that it is possible to use the build count including the number of attempts for each build/pipeline instance within a version number. 

This means, that just re-running the build pipeline, a new version number is created, which will help then to separate if a problem is caused by an erroneous build or by a bug in source code. Another point is, that when the GoCD environment `GO_PIPELINE_COUNTER` is not detected, a deployment operation could be suppressed. This means, that e.g. local builds cannot be published into remote repositories.

# How does the Plugin work?

As of now for a Java library the Gradle code (Groovy DSL) looks as follows:

```groovy
plugins {
    id "java-library"
    id "com.palantir.git-version" version "0.15.0"
    id 'net.raumzeitfalle.gradle.gocdversion' version '0.0.2'
}

# Git details are provided by git-version plugin
version = gocdVersion(versionDetails().lastTag+'.'+versionDetails().commitDistance,
                      versionDetails().lastTag+'.'+versionDetails().commitDistance+'-'+versionDetails().gitHash).build()
```

In this example I've decided to go with the [gradle-git-version plugin](https://github.com/palantir/gradle-git-version) plugin to build the basic version number for a project. A version number might look like: MAJOR.MINOR.PATCH (e.g. 2.0.11). 

The idea is, that the PATCH version can be directly taken from Git history. One could `tag` a specific git commit with an appropriate `MAJOR.MINOR` value.
The count of commits after the last `tag` then represents the `PATCH` version.

Approach:
* declare `MAJOR.MINOR` using `git tag` command
* use commit count after last `tag` as `PATCH` version
* A new, automatically created version could look like: MAJOR.MINOR.PATCH.__BUILD__ (or MAJOR.MINOR.PATCH.__GO-PIPELINE-COUNT__)

The `gocdVersion` function takes 2 arguments, the first one is the string to be used as version for automated builds, the second one for manual or local builds.
So the effective version for a project can be different for atomated builds and manually initiated builds.

With the configuration in the example, the created version number looks as follows:

#### Example 1, automated build in a GoCD pipeline
* git tag such as `1.0` exists
* there are NO commits behind `1.0`, the commit distance is 0
* the build is executed in a GoCD pipeline with `GO_PIPELINE_COUNTER=153.1`):

```
# (tag.commit-distance.pipeline-counter)
version = 1.0.0.153.1
```

#### Example 2, manual (local) build on developer machine
* git tag such as `1.0` exists
* there are NO commits behind `1.0`, the commit distance is 0
* the build is executed on a computer with name ENIAC, there is no environment variable `GO_PIPELINE_COUNTER=xyz`):

```
# (tag.commit-distance-hash.computername.timestamp)
version = 1.0.0-96bfec85d4.ENIAC.20210906000609  
```

Using the `gocdVersion` closure the version schema can be configured.

```groovy

# Defaults
gocdVersion {
    appendPipelineCounterToAutomatedBuilds = true
    appendComputerNameToLocalBuilds = true
    appendTimestampToLocalBuilds = true
}
```

The effective version number schema can be verified using the `printGocdEnvironment` task on commandline.

```shell
> ./gradlew printGocdEnvironment
---------------------------------------------------
Gocd Pipeline Environment
---------------------------------------------------
GO_PIPELINE_COUNTER       = 12.1
GO_PIPELINE_NAME          = test-build
GO_PIPELINE_LABEL         = 1.12.1
GO_STAGE_NAME             = build
GO_STAGE_COUNTER          = 1
GO_SERVER_URL             = http://192.168.1.2:8053
GO_JOB_NAME               = build
GO_TRIGGER_USER           = changes
COMPUTERNAME              = ENIAC
---------------------------------------------------
Project version           = 20220526.2.12.1
Is automated build?       = true
---------------------------------------------------

```

#### Example 3, generating a MSI/WIX compatible version number from Git tag
* git tag such as `20220930` exists
* there were 12 commits after the tag, so the commit distance is 12

```
# (tag.commit-distance.pipeline-counter)
def jpkgversion = jpackageVersion(versionDetails().lastTag+'.'+versionDetails().commitDistance).build()
println(jpkgversion)
22.39.12
```

## License

Copyright 2021 Oliver Löffler

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
