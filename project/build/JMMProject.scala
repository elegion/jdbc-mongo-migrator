import sbt._

class JMMProject(info: ProjectInfo) extends DefaultProject(info) with ProguardProject
{
  val lift_json = "net.liftweb" %% "lift-json" % "2.2"
  val casbah = "com.mongodb.casbah" %% "casbah" % "2.0.2"
//  val querulous = "org.scalaquery" % "scalaquery" % "0.9.1"
  val mysql = "mysql" % "mysql-connector-java" % "5.1.12"
  val configgy = "net.lag" % "configgy" % "2.0.0" intransitive()
  lazy val hi = task { println("Hello World"); None }

//program entry point
  override def mainClass: Option[String] = Some("jm.migrator.Launcher")


    //proguard
  override def proguardOptions = List(
    "-keepclasseswithmembers public class * { public static void main(java.lang.String[]); }",
    "-dontoptimize",
    "-dontobfuscate",
    "-dontshrink",
    proguardKeepLimitedSerializability,
    proguardKeepAllScala
    //"-keep interface scala.ScalaObject"
  )
  override def proguardInJars = Path.fromFile(scalaLibraryJar) +++ super.proguardInJars
}