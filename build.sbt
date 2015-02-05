import play.PlayJava

name := """ZipChat"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"