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

trait EndpointActor extends HttpService with SpotifyInterfaceImpl  {

  val loginRedirect = "https://accounts.spotify.com/authorize?client_id=" + System.getenv("client_id") + "&response_type=code&redirect_uri=" + System.getenv("redirect_uri") + "&scope=playlist-modify-public"

  val actorMap = new HashMap[String,ActorRef]()

  lazy val route = pingRoute ~ loginRoute ~ finishAuthorize ~ searchPage ~ startAuthorize ~ getPlaylists

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

  def finishAuthorize = path("finishAuthorize") {
    parameters('code) { (code) =>
      get { ctx =>

        println("Attepmting to authorize code " + code)

        val response = getAccessToken(code).flatMap { (token) =>
          println("found access token " + token)

          getUserID(token).map { (id) =>

            println("found userID " + id)

            val hash = Util.getBase36(6)

            actorMap += hash -> actorRefFactory.actorOf(Props(new PlaylistActor(id, token)))

            hash
          }
        }

        response.foreach(println(_))

        ctx.complete(response)
      }
    }
  }

  def getPlaylists = path("getPlaylists" / Segment) { (actorHash) =>
    get { ctx =>
      actorMap(actorHash) ! GetAllPlaylists(ctx)
    }
  }

  def startAuthorize = path("startAuthorize") {
    get {
      println("serving admin page")
      getFromResource("choosePlaylist.html")
    }
  }

  def stringFormatPlaylists(playlists: List[Playlist], s: String): String = playlists match{
    case head::Nil => s + head.name + "\t" + head.trackCount + "\t" + head.id + "\t" + head.imageURL + "\n"
    case head::tail => stringFormatPlaylists(tail, s + head.name + "\t" + head.trackCount + "\t" + head.id + "\t" + head.imageURL + "\n")
    case _ => ""
  }

  /*def addSongRoute = path("add") {
    post {
      entity(as[Song]) { song =>
        if (accessToken == "" || userID == "" || playlistID == "") {
          complete("Please login before adding songs")
        } else {
          val songID = song.id
          complete( addSong(accessToken, userID, playlistID, songID) /*match {
              case Future.successful(snapshotID) => snapshotID
              case Future.failed(ex) => 
                println(s"failed to add song $songID to playlist $playlistID for user $userID with token $accessToken")
                "Failed to add song: \n" + ex
            }*/
          )
        }
      }
    }
  }*/

  def searchPage = 
    get {
      compressResponse()(getFromResourceDirectory("")) ~
      path("") {
        getFromResource("index.html")
      }
    }
}