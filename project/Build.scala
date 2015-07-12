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

import com.twitter.sbt._
import sbt.Keys._
import sbt._

object PhantomBuild extends Build {

  val UtilVersion = "0.9.8"
  val DatastaxDriverVersion = "2.1.5"
  val ScalaTestVersion = "2.2.4"
  val ShapelessVersion = "2.2.0-RC4"
  val FinagleVersion = "6.25.0"
  val TwitterUtilVersion = "6.24.0"
  val ScroogeVersion = "3.17.0"
  val ScalatraVersion = "2.3.0"
  val PlayVersion = "2.4.0-M1"
  val Json4SVersion = "3.2.11"
  val ScalaMeterVersion = "0.6"
  val CassandraUnitVersion = "2.1.3.2"
  val SparkCassandraVersion = "1.2.0-alpha3"
  val ThriftVersion = "0.5.0"
  val PhantomSbtVersion = "1.9.3"

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

  val publishSettings: Seq[Def.Setting[_]] = Seq(
    publishMavenStyle := true,
    bintray.BintrayKeys.bintrayOrganization := Some("websudos"),
    bintray.BintrayKeys.bintrayRepository := "oss-releases",
    bintray.BintrayKeys.bintrayReleaseOnPublish := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => true},
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )


  val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
    organization := "com.websudos",
    version := "1.9.11-SNAPSHOT",
    scalaVersion := "2.11.6",
    crossScalaVersions := Seq("2.10.5", "2.11.6"),
    resolvers ++= Seq(
      "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
      "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
      "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
      "Twitter Repository"               at "http://maven.twttr.com",
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
    javaOptions in Test ++= Seq("-Xmx2G"),
    testFrameworks in PerformanceTest := Seq(new TestFramework("org.scalameter.ScalaMeterFramework")),
    testOptions in Test := Seq(Tests.Filter(x => !performanceFilter(x))),
    testOptions in PerformanceTest := Seq(Tests.Filter(x => performanceFilter(x))),
    fork in PerformanceTest := true
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ publishSettings ++ StandardProject.newSettings


  lazy val diesel = Project(
    id = "diesel",
    base = file("."),
    settings = sharedSettings
  ).aggregate(
    dieselEngine
  )

  lazy val dieselEngine = Project(
    id = "diesel-engine",
    base = file("engine"),
    settings = sharedSettings
  ).settings(
    libraryDependencies ++= Seq(
      "org.scala-lang"               %  "scala-reflect"                     % scalaVersion.value,
      "org.scalacheck"               %% "scalacheck"                        % "1.11.5"                        % "test, provided",
      "com.websudos"                 %% "util-testing"                      % UtilVersion                     % "test, provided",
      "com.storm-enroute"            %% "scalameter"                        % ScalaMeterVersion               % "test, provided"
    )
  )
}
