scalaVersion := "2.13.14"
val chiselVersion = "3.6.1"

addCompilerPlugin(("edu.berkeley.cs" %% "chisel3-plugin" % chiselVersion).cross(CrossVersion.full))

libraryDependencies += "edu.berkeley.cs" %% "chisel3" % chiselVersion
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.6.2" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"

scalacOptions ++= Seq(
  "-deprecation",               // show which line uses deprecated stuff
  "-feature",                   // show which line triggers feature warnings
  "-language:reflectiveCalls"   // allow the IO(new Bundle { ... }) pattern without warnings
)