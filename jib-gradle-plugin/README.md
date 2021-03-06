![beta](https://img.shields.io/badge/stability-beta-darkorange.svg)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/google/cloud/tools/jib/com.google.cloud.tools.jib.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=gradle)](https://plugins.gradle.org/plugin/com.google.cloud.tools.jib)
[![Gitter version](https://img.shields.io/gitter/room/gitterHQ/gitter.svg)](https://gitter.im/google/jib)

# Jib - Containerize your Gradle Java project

Jib is a [Gradle](https://gradle.org/) plugin for building Docker and [OCI](https://github.com/opencontainers/image-spec) images for your Java applications.

For information about the project, see the [Jib project README](../README.md).
For the Maven plugin, see the [jib-maven-plugin project](../jib-maven-plugin).

## Upcoming Features

See [Milestones](https://github.com/GoogleContainerTools/jib/milestones) for planned features. [Get involved with the community](https://github.com/GoogleContainerTools/jib/tree/master#get-involved-with-the-community) for the latest updates.

## Quickstart

### Setup

*Make sure you are using Gradle version 4.6 or later.*

In your Gradle Java project, add the plugin to your `build.gradle`:

```groovy
plugins {
  id 'com.google.cloud.tools.jib' version '0.9.11'
}
```

*See the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.google.cloud.tools.jib) for more details.*

You can containerize your application easily with one command:

```shell
gradle jib --image=<MY IMAGE>
```

This builds and pushes a container image for your application to a container registry. *If you encounter authentication issues, see [Authentication Methods](#authentication-methods).*

To build to a Docker daemon, use:

```shell
gradle jibDockerBuild
```

If you would like to set up Jib as part of your Gradle build, follow the guide below.

## Configuration

Configure the plugin by setting the image to push to:

#### Using [Google Container Registry (GCR)](https://cloud.google.com/container-registry/)...

*Make sure you have the [`docker-credential-gcr` command line tool](https://cloud.google.com/container-registry/docs/advanced-authentication#docker_credential_helper). Jib automatically uses `docker-credential-gcr` for obtaining credentials. See [Authentication Methods](#authentication-methods) for other ways of authenticating.*

For example, to build the image `gcr.io/my-gcp-project/my-app`, the configuration would be:

```groovy
jib.to.image = 'gcr.io/my-gcp-project/my-app'
```

#### Using [Amazon Elastic Container Registry (ECR)](https://aws.amazon.com/ecr/)...

*Make sure you have the [`docker-credential-ecr-login` command line tool](https://github.com/awslabs/amazon-ecr-credential-helper). Jib automatically uses `docker-credential-ecr-login` for obtaining credentials. See [Authentication Methods](#authentication-methods) for other ways of authenticating.*

For example, to build the image `aws_account_id.dkr.ecr.region.amazonaws.com/my-app`, the configuration would be:

```groovy
jib.to.image = 'aws_account_id.dkr.ecr.region.amazonaws.com/my-app'
```

#### Using [Docker Hub Registry](https://hub.docker.com/)...

*Make sure you have a [docker-credential-helper](https://github.com/docker/docker-credential-helpers#available-programs) set up. For example, on macOS, the credential helper would be `docker-credential-osxkeychain`. See [Authentication Methods](#authentication-methods) for other ways of authenticating.*

For example, to build the image `my-docker-id/my-app`, the configuration would be:

```groovy
jib.to.image = 'my-docker-id/my-app'
```

#### *TODO: Add more examples for common registries.*

### Build Your Image

Build your container image with:

```shell
gradle jib
```

Subsequent builds are much faster than the initial build.

*Having trouble? Let us know by [submitting an issue](/../../issues/new), contacting us on [Gitter](https://gitter.im/google/jib), or posting to the [Jib users forum](https://groups.google.com/forum/#!forum/jib-users).*

#### Build to Docker daemon

Jib can also build your image directly to a Docker daemon. This uses the `docker` command line tool and requires that you have `docker` available on your `PATH`.

```shell
gradle jibDockerBuild
```

If you are using [`minikube`](https://github.com/kubernetes/minikube)'s remote Docker daemon, make sure you [set up the correct environment variables](https://github.com/kubernetes/minikube/blob/master/docs/reusing_the_docker_daemon.md) to point to the remote daemon:

```shell
eval $(minikube docker-env)
gradle jibDockerBuild
```

#### Build an image tarball

You can build and save your image to disk as a tarball with:

```shell
gradle jibBuildTar
```

This builds and saves your image to `build/jib-image.tar`, which you can load into docker with:

```shell
docker load --input build/jib-image.tar
```

### Run `jib` with each build

You can also have `jib` run with each build by attaching it to the `build` task:

```groovy
tasks.build.dependsOn tasks.jib
```

Then, ```gradle build``` will build and containerize your application.

### Export to a Docker context

Jib can also export a Docker context so that you can build with Docker, if needed:

```shell
gradle jibExportDockerContext
```

The Docker context will be created at `build/jib-docker-context` by default. You can change this directory with the `targetDir` configuration option or the `--jibTargetDir` parameter:

```shell
gradle jibExportDockerContext --jibTargetDir=my/docker/context/
```

You can then build your image with Docker:

```shell
docker build -t myimage my/docker/context/
```

## Extended Usage

The plugin provides the `jib` extension for configuration with the following options for customizing the image build:

Field | Type | Default | Description
--- | --- | --- | ---
`to` | [`to`](#to-closure) | *Required* | Configures the target image to build your application to.
`from` | [`from`](#from-closure) | See [`from`](#from-closure) | Configures the base image to build your application on top of.
`container` | [`container`](#container-closure) | See [`container`](#container-closure) | Configures the container that is run from your built image.
`dockerClient` | [`dockerClient`](#dockerClient-closure) | See [`dockerClient`](#dockerClient-closure) | Configures the docker command line tool that pushes image directly to a local Docker daemon.
`allowInsecureRegistries` | `boolean` | `false` | If set to true, Jib ignores HTTPS certificate errors and may fall back to HTTP as a last resort. Leaving this parameter set to `false` is strongly recommended, since HTTP communication is unencrypted and visible to others on the network, and insecure HTTPS is no better than plain HTTP. [If accessing a registry with a self-signed certificate, adding the certificate to your Java runtime's trusted keys](https://github.com/GoogleContainerTools/jib/tree/master/docs/self_sign_cert.md) may be an alternative to enabling this option.
`useProjectOnlyCache` | `boolean` | `false` | If set to `true`, Jib does not share a cache between different Maven projects.

<a name="from-closure"></a>`from` is a closure with the following properties:

Property | Type | Default | Description
--- | --- | --- | ---
`image` | `String` | `gcr.io/distroless/java` | The image reference for the base image.
`auth` | [`auth`](#auth-closure) | *None* | Specify credentials directly (alternative to `credHelper`).
`credHelper` | `String` | *None* | Suffix for the credential helper that can authenticate pulling the base image (following `docker-credential-`).

<a name="to-closure"></a>`to` is a closure with the following properties:

Property | Type | Default | Description
--- | --- | --- | ---
`image` | `String` | *Required* | The image reference for the target image. This can also be specified via the `--image` command line option.
`auth` | [`auth`](#auth-closure) | *None* | Specify credentials directly (alternative to `credHelper`).
`credHelper` | `String` | *None* | Suffix for the credential helper that can authenticate pulling the base image (following `docker-credential-`).
`tags` | `List<String>` | *None* | Additional tags to push to.

<a name="auth-closure"></a>`auth` is a closure with the following properties (see [Using Specific Credentials](#using-specific-credentials)):

Property | Type
--- | ---
`username` | `String`
`password` | `String`

<a name="dockerClient-closure"></a>`dockerClient` is a closure with the following properties:

Property | Type | Default | Description
--- | --- | --- | ---
`environment` | `Map<String, String>` | ``{}`` | Environment variables for docker cli
`executable` | `String` | ``"docker"``| A name or a path to a docker executable

<a name="container-closure"></a>`container` is a closure with the following properties:

Property | Type | Default | Description
--- | --- | --- | ---
`appRoot` | `String` | `/app` | The root directory on the container where the app's contents are placed.
`args` | `List<String>` | *None* | Default main method arguments to run your application with.
`entrypoint` | `List<String>` | *None* | The command to start the container with (similar to Docker's [ENTRYPOINT](https://docs.docker.com/engine/reference/builder/#entrypoint) instruction). If set, then `jvmFlags` and `mainClass` are ignored.
`environment` | `Map<String, String>` | *None* | Key-value pairs for setting environment variables on the container (similar to Docker's [ENV](https://docs.docker.com/engine/reference/builder/#env) instruction).
`format` | `String` | `Docker` | Use `OCI` to build an [OCI container image](https://www.opencontainers.org/).
`jvmFlags` | `List<String>` | *None* | Additional flags to pass into the JVM when running your application.
`labels` | `Map<String, String>` | *None* | Key-value pairs for applying image metadata (similar to Docker's [LABEL](https://docs.docker.com/engine/reference/builder/#label) instruction).
`mainClass` | `String` | *Inferred\** | The main class to launch your application from.
`ports` | `List<String>` | *None* | Ports that the container exposes at runtime (similar to Docker's [EXPOSE](https://docs.docker.com/engine/reference/builder/#expose) instruction).
`useCurrentTimestamp` | `boolean` | `false` | By default, Jib wipes all timestamps to guarantee reproducibility. If this parameter is set to `true`, Jib will set the image's creation timestamp to the time of the build, which sacrifices reproducibility for easily being able to tell when your image was created.

You can also configure HTTP connection/read timeouts for registry interactions using the `jib.httpTimeout` system property, configured in milliseconds via commandline (the default is `20000`; you can also set it to `0` for infinite timeout):

```shell
gradle jib -Djib.httpTimeout=3000
```

*\* Uses the main class defined in the `jar` task or tries to find a valid main class.*

### Example

In this configuration, the image:
* Is built from a base of `openjdk:alpine` (pulled from Docker Hub)
* Is pushed to `localhost:5000/my-image:built-with-jib`, `localhost:5000/my-image:tag2`, and `localhost:5000/my-image:latest`
* Runs by calling `java -Xms512m -Xdebug -Xmy:flag=jib-rules -cp app/libs/*:app/resources:app/classes mypackage.MyApp some args`
* Exposes port 1000 for tcp (default), and ports 2000, 2001, 2002, and 2003 for udp
* Has two labels (key1:value1 and key2:value2)
* Is built as OCI format

```groovy
jib {
  from {
    image = 'openjdk:alpine'
  }
  to {
    image = 'localhost:5000/my-image/built-with-jib'
    credHelper = 'osxkeychain'
    tags = ['tag2', 'latest']
  }
  container {
    jvmFlags = ['-Xms512m', '-Xdebug', '-Xmy:flag=jib-rules']
    mainClass = 'mypackage.MyApp'
    args = ['some', 'args']
    ports = ['1000', '2000-2003/udp']
    labels = [key1:'value1', key2:'value2']
    format = 'OCI'
  }
}
```

### Adding Arbitrary Files to the Image

*\* Note: this is an incubating feature and may change in the future.*

You can add arbitrary, non-classpath files to the image by placing them in a `src/main/jib` directory. This will copy all files within the `jib` folder to the image's root directory, maintaining the same structure (e.g. if you have a text file at `src/main/jib/dir/hello.txt`, then your image will contain `/dir/hello.txt` after being built with Jib).

You can configure a different directory by using the `extraDirectory` parameter in your `build.gradle`:

```groovy
jib {
  ...
  // Copies files from 'src/main/custom-extra-dir' instead of 'src/main/jib'
  extraDirectory = file('src/main/custom-extra-dir')
  ...
}
```

### Authentication Methods

Pushing/pulling from private registries require authorization credentials. These can be [retrieved using Docker credential helpers](#using-docker-credential-helpers)<!-- or in the `jib` extension-->. If you do not define credentials explicitly, Jib will try to [use credentials defined in your Docker config](/../../issues/101) or infer common credential helpers.

#### Using Docker Credential Helpers

Docker credential helpers are CLI tools that handle authentication with various registries.

Some common credential helpers include:

* Google Container Registry: [`docker-credential-gcr`](https://cloud.google.com/container-registry/docs/advanced-authentication#docker_credential_helper)
* AWS Elastic Container Registry: [`docker-credential-ecr-login`](https://github.com/awslabs/amazon-ecr-credential-helper)
* Docker Hub Registry: [`docker-credential-*`](https://github.com/docker/docker-credential-helpers)
<!--* Azure Container Registry: [`docker-credential-acr-*`](https://github.com/Azure/acr-docker-credential-helper)
-->

Configure credential helpers to use by specifying them as a `credHelper` for their respective image in the `jib` extension.

*Example configuration:*
```groovy
jib {
  from {
    image = 'aws_account_id.dkr.ecr.region.amazonaws.com/my-base-image'
    credHelper = 'ecr-login'
  }
  to {
    image = 'gcr.io/my-gcp-project/my-app'
    credHelper = 'gcr'
  }
}
```

#### Using Specific Credentials

You can specify credentials directly in the extension for the `from` and/or `to` images.

```groovy
jib {
  from {
    image = 'aws_account_id.dkr.ecr.region.amazonaws.com/my-base-image'
    auth {
      username = USERNAME // Defined in 'gradle.properties'.
      password = PASSWORD
    }
  }
  to {
    image = 'gcr.io/my-gcp-project/my-app'
    auth {
      username = 'oauth2accesstoken'
      password = 'gcloud auth print-access-token'.execute().text.trim()
    }
  }
}
```

These credentials can be stored in `gradle.properties`, retrieved from a command (like `gcloud auth print-access-token`), or read in from a file.

For example, you can use a key file for authentication (for GCR, see [Using a JSON key file](https://cloud.google.com/container-registry/docs/advanced-authentication#using_a_json_key_file)):

```groovy
jib {
  to {
    image = 'gcr.io/my-gcp-project/my-app'
    auth {
      username = '_json_key'
      password = file('keyfile.json').text
    }
  }
}
```

## How Jib Works

See the [Jib project README](/../../#how-jib-works).

## Frequently Asked Questions (FAQ)

See the [Jib project FAQ](../docs/faq.md).

## Community

See the [Jib project README](/../../#community).

[![Analytics](https://cloud-tools-for-java-metrics.appspot.com/UA-121724379-2/jib-gradle-plugin)](https://github.com/igrigorik/ga-beacon)
