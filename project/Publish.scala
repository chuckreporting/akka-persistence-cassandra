/**
 * Copyright (C) 2009-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package akka

import sbt._
import sbt.Keys._
import java.io.File
import sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction
import com.typesafe.sbt.pgp.PgpKeys

object Publish extends AutoPlugin {

  val defaultPublishTo = settingKey[File]("Default publish directory")

  override def trigger = allRequirements
  override def requires = sbtrelease.ReleasePlugin

  override lazy val projectSettings = Seq(
    crossPaths := false,
    pomExtra := akkaPomExtra,
    publishTo := akkaPublishTo.value,
    credentials ++= akkaCredentials,
    organizationName := "Typesafe Inc.",
    organizationHomepage := Some(url("http://www.typesafe.com")),
    homepage := Some(url("https://github.com/akka/akka-persistence-cassandra")),
    publishMavenStyle := true,
    pomIncludeRepository := { x => false },
    defaultPublishTo := crossTarget.value / "repository",
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )

  def akkaPomExtra = {
    <scm>
      <url>git@github.com:akka/akka-persistence-cassandra.git</url>
      <connection>scm:git:git@github.com:akka/akka-persistence-cassandra.git</connection>
    </scm>
    <developers>
      <developer>
        <id>contributors</id>
        <name>Contributors</name>
        <email>akka-dev@googlegroups.com</email>
        <url>https://github.com/akka/akka-persistence-cassandra/graphs/contributors</url>
      </developer>
    </developers>
  }

  private def akkaPublishTo = Def.setting {
    sonatypeRepo(version.value) orElse localRepo(defaultPublishTo.value)
  }

  private def sonatypeRepo(version: String): Option[Resolver] = {
    val nexus = sysPropOrDefault("nexusurl","thatone") //"http://nexus.aws.aspect.com:8081/nexus/"
    if (version endsWith "-SNAPSHOT") {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    }

    else {
      Some("releases" at nexus + "content/repositories/releases")
    }
  }

  private def localRepo(repository: File) =
    Some(Resolver.file("Default Local Repository", repository))

  def sysPropOrDefault(propName:String,default:String):String = Option(System.getProperty(propName)).getOrElse(default)

  private def akkaCredentials: Seq[Credentials] = {
    val project = sysPropOrDefault("project","thatone")
    val username = sysPropOrDefault("username","change_me")
    val password = sysPropOrDefault("password","chuckNorris")
    Seq(Credentials("Sonatype Nexus Repository Manager", project,username, password))
    // Option(System.getProperty("akka.publish.credentials", null)).map(f => Credentials(new File(f))).toSeq
  }

}
