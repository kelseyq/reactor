organization := "com.example"

name := "scalatra-sbt-prototype"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

seq(webSettings :_*)

libraryDependencies ++= Seq(
   "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "org.scalatra" % "scalatra" % "2.1.0-SNAPSHOT",
  "org.scalatra" %% "scalatra-scalate" % "2.0.4",
  "org.scalatra" %% "scalatra-specs2" % "2.0.4" % "test",  
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.0.RC5" % "container",
   "net.liftweb" %% "lift-json" % "2.4-M4",
   "com.mongodb.casbah" %% "casbah" % "3.0.0-SNAPSHOT"
   )

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
