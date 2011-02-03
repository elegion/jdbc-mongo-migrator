import sbt._

class JMMProject(info: ProjectInfo) extends DefaultProject(info)
{

  val lift_json = "net.liftweb" %% "lift-json" % "2.2"
  lazy val hi = task { println("Hello World"); None }

}