import play.PlayJava

name := """ZipChat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.4"


libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final",
  "redis.clients" % "jedis" % "2.6.0"
)

resolvers ++= Seq(
  "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk"
)

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

libraryDependencies += "com.typesafe.play.plugins" %% "play-plugins-redis" % "2.3.1"

libraryDependencies += "com.notnoop.apns" % "apns" % "0.1.6"

libraryDependencies += "com.ganyo" % "gcm-server" % "1.0.2"

libraryDependencies += "com.intellij" % "annotations" % "9.0.4"

libraryDependencies += "io.jsonwebtoken" % "jjwt" % "0.5"

libraryDependencies += "com.github.javafaker" % "javafaker" % "0.5"

libraryDependencies += "org.mockito" % "mockito-core" % "2.0.21-beta"

jacoco.settings

herokuAppName in Compile := "zipchatapp"

