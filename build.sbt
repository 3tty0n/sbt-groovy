lazy val `sbt-groovy` = (project in file(".")).
  settings(
    name := "sbt-groovy",
    version := "0.1.0-SNAPSHOT",
    organization := "com.github.3tty0n",
    sbtPlugin := true,
    scalacOptions := Seq("-deprecation", "-unchecked"),
    libraryDependencies ++= Seq(
      "org.codehaus.groovy" % "groovy-all" % "2.4.12"
    )
  ).
  settings(
    publishSettings: _*
  )

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  pomExtra :=
    <url>https://github.com/3tty0n/sbt-groovy</url>
      <developers>
        <developer>
          <id>3tty0n</id>
          <name>Yusuke Izawa</name>
          <url>https://github.com/3tty0n</url>
        </developer>
      </developers>
      <scm>
        <url>git@github.com:3tty0n/sbt-groovy.git</url>
        <connection>scm:git@github.com:3tty0n/sbt-groovy.git</connection>
      </scm>
)