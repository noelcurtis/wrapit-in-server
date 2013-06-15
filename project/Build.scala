import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "wrapit-in-server"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.jsoup" % "jsoup" % "1.7.2",
    "nl.rhinofly" %% "api-s3" % "2.6.1",
    "org.im4java" % "im4java" % "1.4.0"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"
  )

}
