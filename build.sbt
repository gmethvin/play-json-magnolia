organization in ThisBuild := "io.methvin.play"
organizationName in ThisBuild := "Greg Methvin"
startYear in ThisBuild := Some(2018)
licenses in ThisBuild := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))
homepage in ThisBuild := Some(url("https://github.com/gmethvin/play-json-magnolia"))
scmInfo in ThisBuild := Some(
  ScmInfo(url("https://github.com/gmethvin/play-json-magnolia"), "scm:git@github.com:gmethvin/play-json-magnolia.git")
)
developers in ThisBuild := List(
  Developer("gmethvin", "Greg Methvin", "greg@methvin.net", new URL("https://github.com/gmethvin"))
)

scalaVersion in ThisBuild := "2.12.7"
crossScalaVersions in ThisBuild := Seq(scalaVersion.value)

// Project definitions

lazy val `play-json-magnolia` = (project in file("core"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.propensive" %% "magnolia" % "0.10.0",
      "com.typesafe.play" %% "play-json" % "2.6.10",
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
  )

lazy val bench = (project in file("bench"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(noPublishSettings)
  .dependsOn(`play-json-magnolia`)

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(`play-json-magnolia`, bench)

// Release options

lazy val noPublishSettings =
  Seq(PgpKeys.publishSigned := {}, publish := {}, publishLocal := {}, publishArtifact := false, skip in publish := true)

publishMavenStyle in ThisBuild := true
publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

import ReleaseTransformations._
releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

// Scalafmt options

scalafmtOnCompile in ThisBuild := true
