/*
 * Copyright 2013-2015 Outworkers, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
lazy val Versions = new {
  val scalatest = "3.0.0"
  val scalaMeter = "0.8.+"
}

import com.twitter.sbt.{GitProject, VersionManagement}
import sbt.Keys._
import sbt._

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.outworkers",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
  resolvers ++= Seq(
    "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype staging" at "http://oss.sonatype.org/content/repositories/staging",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    Resolver.bintrayRepo("websudos", "oss-releases")
  ),
  scalacOptions in ThisBuild ++= Seq(
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    //"-Yinline-warnings",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  fork in Test := true,
  javaOptions in Test ++= Seq("-Xmx2G")
) ++ VersionManagement.newSettings ++
  GitProject.gitSettings ++ Publishing.effectiveSettings

lazy val diesel = (project in file(".")).settings(
    sharedSettings ++ noPublishSettings
  ).settings(
    name := "diesel",
    moduleName := "diesel",
    pgpPassphrase := Publishing.pgpPass
  ).aggregate(
    engine,
    reflection,
    macros
  )

lazy val engine = (project in file("engine")).settings(
    moduleName := "diesel-engine",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % Versions.scalatest,
      "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
      "com.storm-enroute" %% "scalameter" % Versions.scalaMeter % Test
    )
  ).settings(sharedSettings)

lazy val reflection = (project in file("reflection"))
  .settings(
    moduleName := "diesel-reflection",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % Versions.scalatest
    )
  ).settings(
    sharedSettings
  ).dependsOn(
    engine
  )

lazy val macros = (project in file("macros"))
  .settings(
    moduleName := "diesel-macros",
    scalacOptions ++= Seq(
      "-Ymacro-debug-verbose",
      "-Yshow-trees-stringified"
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    unmanagedSourceDirectories in Compile ++= Seq(
      (sourceDirectory in Compile).value / ("scala-2." + {
        CrossVersion.partialVersion(scalaBinaryVersion.value) match {
          case Some((major, minor)) => minor
        }
      })),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % "1.1.1",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      "org.scalatest" %% "scalatest" % Versions.scalatest
    )
  ).settings(
    sharedSettings
  ).dependsOn(
    engine
  )
