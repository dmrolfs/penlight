import Dependencies._
import BuildSettings._

name := "penlight"
organization in ThisBuild := "com.github.dmrolfs"

scalacOptions in (Compile, console) := Seq()

//scalaVersion in ThisBuild := "2.12.2"

//crossScalaVersions in ThisBuild := Seq( "2.12.2" )

//scalacOptions := Seq(
//  // "-encoding",
//  // "utf8",
//  "-target:jvm-1.8",
//  "-unchecked",
//  "-deprecation",
//  "-feature",
//  "-Xlint:-infer-any",
//  "-Xfatal-warnings",
//  "-language:implicitConversions",
//  //      "-Ylog-classpath",
//  // "-Xlog-implicits",
//  // "-Ymacro-debug-verbose",
//  // "-Ywarn-adapted-args",
//  "-Xlog-reflective-calls"
//)

lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  addCompilerPlugin( "org.scalameta" % "paradise" % "3.0.0-M9" cross CrossVersion.full )
)

lazy val root = Project(
  id = "penlight",
  base = file( "." ),
  settings = defaultBuildSettings ++ metaMacroSettings
)
.settings( libraryDependencies ++= commonDependencies )
