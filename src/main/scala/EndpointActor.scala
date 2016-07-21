package org.eclectek.dj

import akka.actor._
import spray.routing.HttpService
import spray.http.HttpHeaders._
import spray.http.ContentTypes._
import spray.http.AllOrigins
import spray.http.StatusCodes
import com.mashape.unirest.http._
import spray.httpx.SprayJsonSupport._
import spray.http.MediaTypes
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import collection.mutable.HashMap
import spray.routing.RequestContext
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.httpx.marshalling._
import spray.http._


trait EndpointActor extends HttpService with SpotifyInterfaceImpl  {

  val loginRedirect = "https://accounts.spotify.com/authorize?client_id=" + System.getenv("client_id") + "&response_type=code&redirect_uri=" + System.getenv("redirect_uri") + "&scope=playlist-modify-public"

  val actorMap = new HashMap[String,ActorRef]() //maps our hashes to the corresponding actor
  val userIDMap = new HashMap[String,String]() //maps user ids to our hashes

  lazy val route = pingRoute ~ loginRoute ~ finishAuthorize ~
    pathPrefix(Segment) { hash:String => ctx:RequestContext =>
      if (actorMap.contains(hash)) {
        actorMap(hash) ! ctx
      } else {
        ctx.complete(StatusCodes.BadRequest, "Invalid hash")
      }
    }
  
  implicit val timeout = Timeout(2 seconds)

  def pingRoute = path("ping" / Segment) { (s) =>
    get { 
      complete("pong! " + s) 
    }
  }

  def loginRoute = path("login") {
    get { 
      println("url: " + loginRedirect)
      println(System.getenv("redirect_uri"))
      redirect(loginRedirect, StatusCodes.PermanentRedirect)
    }
  }

  def finishAuthorize = (get & path("finish-authorize")) {
    parameters('code) { (code) =>
      detach() {
        println("Attepmting to authorize code " + code)

        val response = getAccessTokens(code).flatMap { (tokens) =>
          println("found access token " + tokens._1)

          getUserID(tokens._1).map { (id) =>

            println("found userID " + id)


            if (userIDMap.contains(id)) {
              println("user already exists, returning existing hash")
              userIDMap(id)
            } else {
              val hash = Util.getBase36(6)
              println("user doesn't exist, created hash " + hash)
              actorMap += hash -> actorRefFactory.actorOf(Props(new PlaylistActor(id, tokens._1, tokens._2)))
              userIDMap += id -> hash
              hash
            }
          }
        }

        complete(response)
      }
    }
  }

  def stringFormatPlaylists(playlists: List[Playlist], s: String): String = playlists match{
    case head::Nil => s + head.name + "\t" + head.trackCount + "\t" + head.id + "\t" + head.imageURL + "\n"
    case head::tail => stringFormatPlaylists(tail, s + head.name + "\t" + head.trackCount + "\t" + head.id + "\t" + head.imageURL + "\n")
    case _ => ""
  }
}