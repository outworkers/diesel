resolvers ++= Seq(
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "jgit-repo" at "http://download.eclipse.org/jgit/maven",
    "Twitter Repo" at "http://maven.twttr.com/",
    "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
    Resolver.bintrayRepo("websudos", "oss-releases")
)

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

