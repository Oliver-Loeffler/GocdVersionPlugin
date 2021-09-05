# Project Idea: GitGocdVersionPlugin for Gradle

Warning, this is very experimental and not meant to be used in production environments.
It is also tailored for a special use case and for me it is a learning experience on how Gradle plugins work.

When continuous delivery is used, it can be valuable to have reliable version numbers which include the _commit ID_ and a _build ID_ pointing to a certain status in source code history and build pipeline run.
This can be achieved by using the [gradle-git-version plugin](https://github.com/palantir/gradle-git-version). In some cases, one would like to distinguish between local builds (e.g. performed on a developer PC) and automated builds in a CI/CD environment.

This plugin has the purpose to work with GoCD in order to detect if a build runs in a GoCD pipeline on a GoCD build agent.
Depending on that, the project version identifier will be updated.

There are no dependencies to other plugins.


## Semantic Versioning and Git

* MAJOR version when you make incompatible API changes
* MINOR version when you add functionality in a backwards-compatible manner
* PATCH version when you make backwards-compatible bug fixes

A version number might look like: MAJOR.MINOR.PATCH (e.g. 2.0.11)

The idea is, that the PATCH version can be directly taken from Git history.
One could `tag` a specific git commit with an appropriate `MAJOR.MINOR` value.
The count of commits after the last `tag` then represents the `PATCH` version.

Approach:
* declare `MAJOR.MINOR` using `git tag` command
* use commit count after last `tag` as `PATCH` version

The details here are provided by the [gradle-git-version plugin](https://github.com/palantir/gradle-git-version).
This provides an extension which makes it easy to get details such as the last tag, the commit distance, commit hash values etc.

## Combining Git with GoCD

Using GoCD so called piplines can be defined to build an artifact.
When a pipeline is running, it will have an id which is store in an environment variable `GO_PIPELINE_COUNTER`.

Now one can create a version such as: MAJOR.MINOR.PATCH.__BUILD__

This means, that just re-running the build pipeline, a new version number is created, which will help then to separate if a problem is caused by an erroneous build or by a bug in source code.
Another point is, that when the GoCD environment `GO_PIPELINE_COUNTER` is not detected, a deployment operation could be suppressed. This means, that e.g. local builds cannot be published into remote repositories.

# How does the Plugin work?

As of now for a Java library the Gradle code (Groovy DSL) looks as follows:

```groovy
plugins {
    id "java-library"
    id "com.palantir.git-version" version "0.12.3"
    id 'net.raumzeitfalle.gradle.gocdversion' version '1.0-SNAPSHOT'
}

# Git details are provided by git-version plugin
version = gocdVersion(versionDetails().lastTag+'.'+versionDetails().commitDistance,
                      versionDetails().lastTag+'.'+versionDetails().commitDistance+'-'+versionDetails().gitHash).build()
```

With that configuration, the created version number looks as follows:

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

Using the `gocdVersion` closure the versioning schema can be configured.

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

Gocd Pipeline Environment
=========================

COMPUTERNAME         = ENIAC
GO_PIPELINE_COUNTER  =
Project version      = 1.0.0-96bfec85d4.ENIAC.20210906000609
Is automated build?  = false

```

## License

Copyright 2019 Oliver Löffler

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
