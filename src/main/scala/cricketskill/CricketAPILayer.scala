package cricketskill


import scala.io.Source
import org.json4s._
import org.json4s.native.JsonMethods._

object CricketAPILayer {
  val API_URL = "http://cricapi.com/api/cricket/"

  def getCricketMatches(): Unit ={
    val matchesString = Source.fromURL(API_URL).mkString
    val matchesJson = parse(matchesString)

    def formatGameString(game:String): String = {
      game
        .replaceAll("&amp;|\\*|v", "")
        .replaceFirst("[0-9]+/[0-9]+", "-")
        .replaceAll("[0-9]+/[0-9]+", "")
        .replaceAll(" +", " ")
        .trim

    }
    val out = for {
      JObject(data) <- matchesJson \ "data"
      JField("title", JString(title)) <- data
    } yield formatGameString(title)

    println(out)

  }
}