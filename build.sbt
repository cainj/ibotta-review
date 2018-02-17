import scoverage.ScoverageKeys.{coverageExcludedPackages, coverageFailOnMinimum, coverageHighlighting, coverageMinimum}

name := """ibotta anagrams"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val scoverageSettings = Seq(
  //Scala Test coverage
  coverageExcludedPackages := "controllers\\.Reverse.*;controllers\\.javascript.*;router\\.*",
  coverageMinimum := 70,
  coverageFailOnMinimum := false,
  coverageHighlighting := true
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(scoverageSettings)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % Test
libraryDependencies += specs2

