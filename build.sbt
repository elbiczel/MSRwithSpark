name := "loc"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.1.201403241930-r"

libraryDependencies += "org.apache.spark" %% "spark-core" % "0.9.1"

libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.4.0"

scalacOptions ++= Seq("-deprecation", "-unchecked")

resolvers ++= Seq(
  "Maven Central" at "http://repo1.maven.org/maven2",
  "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
  "OSS Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/")
