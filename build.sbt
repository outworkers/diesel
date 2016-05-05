lazy val Versions = new {
  val util = "0.16.0"
  val scalaTest = "2.2.6"
  val scalaMeter = "0.6"
}

/*
 * Copyright 2013-2015 Websudos, Limited.
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
import sbt.Keys._
import sbt._

val mavenPublishSettings : Seq[Def.Setting[_]] = Seq(
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishMavenStyle := true,
  publishTo <<= version.apply {
    v =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => true },
  pomExtra :=
    <url>https://github.com/websudos/phantom</url>
      <scm>
        <url>git@github.com:websudos/phantom.git</url>
        <connection>scm:git:git@github.com:websudos/phantom.git</connection>
      </scm>
      <developers>
        <developer>
          <id>alexflav</id>
          <name>Flavian Alexandru</name>
          <url>http://github.com/alexflav23</url>
        </developer>
      </developers>
)

def liftVersion(scalaVersion: String): String = {
  scalaVersion match {
    case "2.10.5" => "3.0-M1"
    case _ => "3.0-M2"
  }
}

val PerformanceTest = config("perf").extend(Test)
def performanceFilter(name: String): Boolean = name endsWith "PerformanceTest"

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

val publishSettings: Seq[Def.Setting[_]] = Seq(
  publishMavenStyle := true,
  bintrayOrganization := Some("websudos"),
  bintrayRepository := "oss-releases",
  bintrayReleaseOnPublish := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => true},
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
)

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  organization := "com.websudos",
  version := "0.3.0",
  scalaVersion := "2.10.6",
  crossScalaVersions := Seq("2.10.6", "2.11.7"),
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
  scalacOptions ++= Seq(
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:existentials",
    "-Yinline-warnings",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  fork in Test := true,
  javaOptions in Test ++= Seq("-Xmx2G")
) ++ publishSettings


lazy val diesel = (project in file(".")).settings(
    sharedSettings ++ noPublishSettings
  ).aggregate(
    engine
  )

lazy val engine = (project in file("engine")).settings(
    moduleName := "diesel-engine",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalacheck" %% "scalacheck" % "1.13.1" % "test",
      "com.websudos" %% "util-testing" % Versions.util % "test",
      "com.storm-enroute" %% "scalameter" % Versions.scalaMeter % "test"
    )
  ).settings(sharedSettings)
