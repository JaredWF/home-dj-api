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

class EndpointActor() extends HttpService with Actor  {
	import context.dispatcher
  def actorRefFactory = context

  var accessToken = ""
  var userID = ""
  var playlistID = ""

  def receive = runRoute(pingRoute ~ loginRoute ~ finishAuthorize ~ addSong)

  def pingRoute = path("ping") {
    get { complete("pong!") }
  }

  def loginRoute = path("login") {
    get { 
      println("client_id: " + System.getenv("client_id"))
      println("login url: \n" + "https://accounts.spotify.com/authorize?client_id=" + System.getenv("client_id") + "&response_type=code&redirect_uri=http://home-dj.herokuapp.com/finishAuthorize&scope=playlist-modify-public")
      redirect("https://accounts.spotify.com/authorize?client_id=" + System.getenv("client_id") + "&response_type=code&redirect_uri=http://home-dj.herokuapp.com/finishAuthorize&scope=playlist-modify-public", StatusCodes.PermanentRedirect)
    }
  }

  def finishAuthorize = path("finishAuthorize") {
    parameters('code) { (code) =>
      get {
        accessToken = Unirest.post("https://accounts.spotify.com/api/token")
          .header("Authorization", "Basic " + System.getenv("hashed_secret"))
          .field("grant_type", "authorization_code")
          .field("code", code)
          .field("redirect_uri", "http://home-dj.herokuapp.com/finishAuthorize")
          .asJson().getBody().getObject.getString("access_token")

        userID = Unirest.get("https://api.spotify.com/v1/me")
          .header("Authorization", "Bearer " + accessToken)
          .asJson().getBody().getObject.getString("id")

        playlistID = Unirest.get("https://api.spotify.com/v1/me/playlists")
          .header("Authorization", "Bearer " + accessToken)
          .asJson().getBody().getObject.getJSONArray("items").getJSONObject(0).getString("id")

        println("userID: " + userID + "\ntoken: " + accessToken + "\nplaylistID: " + playlistID)
        complete(userID)
      }
    }
  }

  def addSong = path("add") {
    post {
      respondWithHeader(`Access-Control-Allow-Origin`(AllOrigins)) {
        entity(as[Song]) { song =>
          if (accessToken == "" || userID == "" || playlistID == "") {
            complete("accessToken: " + accessToken + "\nuserID: " + userID + "\nplaylistID: " + playlistID)
          } else {
            val response = Unirest.post("https://api.spotify.com/v1/users/" + userID + "/playlists/" + playlistID + "/tracks?uris=" + song.id)
              .header("Authorization", "Bearer " + accessToken)
              .header("Content-Type", "application/json")
              .asString().getBody()//.asJson().getBody().getObject.getString("snapshot_id")

            println(response)
            complete(song.id.toString)
          }
        }
      }
    }
  }
}