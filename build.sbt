
name := """ZipChat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"


libraryDependencies ++= Seq(
  javaJdbc,
  javaWs,
  javaJpa,
  cache,
  "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final",
  "com.typesafe.play.modules" %% "play-modules-redis" % "2.4.0"
)

resolvers ++= Seq(
  "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk",
  "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"
)

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

libraryDependencies += "com.notnoop.apns" % "apns" % "0.1.6"

libraryDependencies += "com.ganyo" % "gcm-server" % "1.0.2"

libraryDependencies += "com.intellij" % "annotations" % "9.0.4"

libraryDependencies += "io.jsonwebtoken" % "jjwt" % "0.5"

libraryDependencies += "com.github.javafaker" % "javafaker" % "0.5"

libraryDependencies += "org.mockito" % "mockito-core" % "2.0.21-beta"

libraryDependencies += "com.google.inject" % "guice" % "4.0"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"

jacoco.settings

herokuAppName in Compile := "zipchatapp"


