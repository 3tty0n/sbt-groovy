lazy val root = (project in file(".")).
  enablePlugins(GroovyPlugin).
  configs(
    Groovy,
    GroovyTest
  ).
  settings(
    groovySource in Compile := (sourceDirectory in Compile).value / "groovy",
    groovySource in Test := (sourceDirectory in Test).value / "groovy",
    scalaVersion := "2.12.2"
  )