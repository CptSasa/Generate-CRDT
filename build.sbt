scalaVersion := "3.2.2"
libraryDependencies ++= Seq(
  "de.tu-darmstadt.stg" %% "kofre" % "0.33.0",
)
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked"
)
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.17.0" % "test"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.16"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"
//libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.13.10"
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.8.1"
