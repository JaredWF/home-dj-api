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

trait EndpointActor extends HttpService with SpotifyInterfaceImpl  {

  val loginRedirect = "https://accounts.spotify.com/authorize?client_id=" + System.getenv("client_id") + "&response_type=code&redirect_uri=" + System.getenv("redirect_uri") + "&scope=playlist-modify-public"

  var accessToken = ""
  var userID = ""
  var playlistID = ""

  lazy val route = pingRoute ~ loginRoute ~ finishAuthorize ~ addSongRoute ~ searchPage

  def pingRoute = path("ping") {
    get { 
      complete("pong!") 
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
      get {

        getAccessToken(code) match {
          case Success(token) => 
            accessToken = token

            getUserID(accessToken) match {
              case Success(id) => 
                userID = id

                getPlaylistID(accessToken) match {
                  case Success(playlist) => 
                    playlistID = playlist
                    println(s"logged in $userID")
                    complete(s"Login successful\n\nuserID: $userID \ntoken: $accessToken \nplaylistID: $playlistID")
                  case Failure(playlistEx) => 
                    println(s"playlist retrieval failed for user $userID and code $code and token $accessToken")
                    complete("Error getting first playlist: \n" + playlistEx)
                }
              case Failure(userEx) => 
                println(s"userID retrieval failed for code $code and token $accessToken")
                complete("Error getting user id: \n" + userEx)
            }
          case Failure(tokenEx) => 
            println(s"token retrieval failed for code $code\n" + tokenEx)
            complete("Error getting token: \n" + tokenEx)
        }

      }
    }
  }

  def addSongRoute = path("add") {
    post {
      entity(as[Song]) { song =>
        if (accessToken == "" || userID == "" || playlistID == "") {
          complete("Please login before adding songs")
        } else {
          val songID = song.id
          addSong(accessToken, userID, playlistID, songID) match {
            case Success(snapshotID) => complete(snapshotID)
            case Failure(ex) => 
              println(s"failed to add song $songID to playlist $playlistID for user $userID with token $accessToken")
              complete("Failed to add song: \n" + ex)
          }
        }
      }
    }
  }

  def searchPage = 
    get {
      compressResponse()(getFromResourceDirectory("")) ~
      path("") {
        getFromResource("index.html")
      }
    }
}