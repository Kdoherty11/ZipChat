import de.johoop.jacoco4sbt.{ScalaHTMLReport, XMLReport}

name := """ZipChat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"


libraryDependencies ++= Seq(
  javaJdbc,
  javaWs,
  javaJpa,
  cache,
  "com.typesafe.play.modules" %% "play-modules-redis" % "2.4.1"
)

resolvers ++= Seq(
  "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk",
  "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"
)

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"

libraryDependencies += "org.hibernate" % "hibernate-entitymanager" % "4.3.10.Final"

libraryDependencies += "com.notnoop.apns" % "apns" % "0.1.6"

libraryDependencies += "com.ganyo" % "gcm-server" % "1.0.2"

libraryDependencies += "com.intellij" % "annotations" % "9.0.4"

libraryDependencies += "io.jsonwebtoken" % "jjwt" % "0.5"

libraryDependencies += "com.github.javafaker" % "javafaker" % "0.5"

libraryDependencies += "org.mockito" % "mockito-core" % "2.0.21-beta"

libraryDependencies += "com.google.inject" % "guice" % "4.0"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"

libraryDependencies += "nl.jqno.equalsverifier" % "equalsverifier" % "1.7.3"

libraryDependencies += "com.fiftyonred" % "mock-jedis" % "0.1.2"

jacoco.settings

herokuAppName in Compile := "zipchatapp"

routesGenerator := InjectedRoutesGenerator

fork in run := true

PlayKeys.externalizeResources := false