sbt-groovy
==========

A sbt plugin for groovy

## Requirements

- sbt 0.13.x



## Quick start

Add plugin to *project/plugins.sbt*:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("com.github.3tty0n" % "sbt-groovy" % "0.1.0-SNAPSHOT")
```

For *.sbt* build definitions, inject the plugin settings in *build.sbt*:

```scala
lazy val `groovy-module` = (project in file("groovy-module")).
  enablePlugins(
    GroovyPlugin
  ).
  configs(
    Groovy,
    GroovyTest
  )
```

## Configuration

```scala
lazy val `groovy-module` = (project in file("groovy-module")).
  ...
  .settings(
    groovySource in Compile := (sourceDirectory in Compile).value / "groovy",
    groovySource in Test := (sourceDirectory in Test).value / "groovy"
  )
```

## Usage

```bash
$ sbt
> groovyc
```
