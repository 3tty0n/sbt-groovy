package sbtgroovy

import java.io.File

import sbt._
import Keys._
import sbt.Path.relativeTo
import sbt.plugins.JvmPlugin


object GroovyPlugin extends AutoPlugin { self =>

  override lazy val projectSettings = groovy.settings ++ testGroovy.settings

  object autoImport {
    lazy val groovyVersion = settingKey[String]("groovy version")
    lazy val groovySource = settingKey[File]("Default groovy source directory")
    lazy val groovyc = taskKey[Seq[File]]("Compile Groovy sources")
    lazy val Groovy = (config("groovy") extend Compile).hide
    lazy val GroovyTest = (config("test-groovy") extend Test).hide
    lazy val GroovyIT = (config("it-groovy") extend IntegrationTest).hide
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = JvmPlugin

  def defaultSettings(config: Configuration) = Seq(
    groovyVersion := "2.4.12",
    libraryDependencies ++= Seq[ModuleID](
      "org.codehaus.groovy" % "groovy-all" % groovyVersion.value % config.name,
      "org.apache.ant" % "ant" % "1.8.2" % config.name
    ),
    managedClasspath in groovyc := {
      Classpaths.managedJars(config, (classpathTypes in groovyc).value, update.value)
    }
  )

  // to avoid namespace clashes, use a nested object
  object groovy {

    lazy val groovycFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Groovy), inTasks(groovyc))

    lazy val compileFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Compile))

    lazy val settings = Seq(ivyConfigurations += Groovy) ++ defaultSettings(Groovy) ++ Seq(
      groovySource in Compile := (sourceDirectory in Compile).value / "groovy",
      unmanagedSourceDirectories in Compile += {
        (groovySource in Compile).value
      },
      classDirectory in(Groovy, groovyc) := (crossTarget in Compile).value / "groovy-classes",
      managedClasspath in groovyc := {
        Classpaths.managedJars(Groovy, (classpathTypes in groovyc).value, update.value)
      },
      groovyc in Compile := {
        val sourceDirectory: File = (groovySource in Compile).value
        val nb = (sourceDirectory ** "*.groovy").get.size

        if (nb > 0) {
          val s: TaskStreams = streams.value
          s.log.info(s"Start Compiling Groovy sources : ${sourceDirectory.getAbsolutePath} ")
          val classDirectories: Seq[File] = classDirectory.all(compileFilter).value ++
            classDirectory.all(groovycFilter).value ++
            Seq((classDirectory in Compile).value)

          val classpath: Seq[File] =
            (managedClasspath in groovyc).value.files ++
              classDirectories ++
              (managedClasspath in Compile).value.files

          s.log.debug(classpath.mkString(";"))
          val stubDirectory: File = (sourceManaged in Compile).value
          val destinationDirectory: File = (classDirectory in(Groovy, groovyc)).value
          new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile()
          ((destinationDirectory ** "*.class").get pair relativeTo(destinationDirectory)).map { case (k, v) =>
            IO.copyFile(k, (resourceManaged in Compile).value / v, preserveLastModified = true)
            (resourceManaged in Compile).value / v
          }
        }
        else {
          Seq.empty
        }
      },
      resourceGenerators in Compile += groovyc in Compile,
      groovyc in Compile := (groovyc in Compile).dependsOn(compile in Compile).value
    )
  }

  object testGroovy {

    lazy val groovycTestFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Groovy), inTasks(groovyc))

    lazy val compileTestFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Test))

    lazy val settings = Seq(ivyConfigurations += GroovyTest) ++ inConfig(GroovyTest) {
      Defaults.testTasks ++
        Seq(
          definedTests := (definedTests in Test).value,
          definedTestNames := (definedTestNames in Test).value,
          fullClasspath := (fullClasspath in Test).value
        )
    } ++ defaultSettings(GroovyTest) ++ Seq(
      groovySource in Test := (sourceDirectory in Test).value / "groovy",
      unmanagedSourceDirectories in Test += {
        (groovySource in Test).value
      },
      classDirectory in(GroovyTest, groovyc) := (crossTarget in Test).value / "groovy-test-classes",
      managedClasspath in groovyc := {
        Classpaths.managedJars(GroovyTest, (classpathTypes in groovyc).value, update.value)
      },
      groovyc in Test := {
        val sourceDirectory: File = (groovySource in Test).value
        val nb = (sourceDirectory ** "*.groovy").get.size
        if (nb > 0) {
          val s: TaskStreams = streams.value
          s.log.info(s"Start Compiling Test Groovy sources : ${sourceDirectory.getAbsolutePath} ")

          val classDirectories: Seq[File] = classDirectory.all(groovy.compileFilter).value ++
            classDirectory.all(groovy.groovycFilter).value ++
            classDirectory.all(compileTestFilter).value ++
            classDirectory.all(groovycTestFilter).value ++
            Seq((classDirectory in Compile).value, (classDirectory in(Groovy, groovyc)).value)

          val classpath: Seq[File] =
            (managedClasspath in groovyc).value.files ++
              classDirectories ++ (managedClasspath in Test).value.files

          s.log.debug(classpath.mkString(";"))
          val stubDirectory: File = (sourceManaged in Test).value
          val destinationDirectory: File = (classDirectory in(GroovyTest, groovyc)).value
          new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile()
          ((destinationDirectory ** "*.class").get pair relativeTo(destinationDirectory)).map { case (k, v) =>
            IO.copyFile(k, (resourceManaged in Test).value / v, preserveLastModified = true)
            (resourceManaged in Test).value / v
          }
        }
        else {
          Seq.empty
        }
      },
      resourceGenerators in Test += groovyc in Test,
      groovyc in Test := (groovyc in Test).dependsOn(compile in Test).value
    )
  }

}
