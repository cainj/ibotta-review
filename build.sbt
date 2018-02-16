import scoverage.ScoverageKeys.{coverageExcludedPackages, coverageFailOnMinimum, coverageHighlighting, coverageMinimum}

name := """anagram"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"





  lazy val scoverageSettings = Seq(
    //Scala Test coverage
    coverageExcludedPackages := "controllers\\.Reverse.*;controllers\\.javascript.*;router\\.*",
    coverageMinimum := 70,
    coverageFailOnMinimum := false,
    coverageHighlighting := true
  )
