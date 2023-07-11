val ScalatraVersion = "2.8.2"

ThisBuild / scalaVersion := "2.13.9"
ThisBuild / organization := "com.github.GalexVM"

lazy val hello = (project in file("."))
  .settings(
    name := "My first app",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      "com.typesafe.slick" %% "slick" % "3.3.2",
      "com.h2database" % "h2" % "1.4.196",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
      "org.eclipse.jetty" % "jetty-webapp" % "9.4.43.v20210629" % "container",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
      "com.mchange" % "c3p0" % "0.9.5.2",
      "org.scalatra" %% "scalatra-twirl" % "2.8.1"
      //"com.github.japgolly.scalacss" %%% "core" % "0.8.0"
    ),

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
    resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
    resolvers += "Maven Central" at "https://repo1.maven.org/maven2/",
    resolvers += Resolver.sonatypeRepo("snapshots")



  )

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.13.9",
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.7.0",
    // Otras dependencias de Scala.js que puedas necesitar
  )

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

//repite por si acaso
enablePlugins(ScalaJSPlugin)

scalaVersion := "2.13.1" // or any other Scala version >= 2.11.12

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

enablePlugins(SbtTwirl)
enablePlugins(JettyPlugin)

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0"
//libraryDependencies += "org.scalatra.scalate" %% "scalate-core" % "1.1.0"
//libraryDependencies += "com.github.japgolly.scalacss" %%% "core" % "0.8.0-RC1"


