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
import spray.routing.RequestContext

case class GetAllPlaylists(ctx: RequestContext)

class PlaylistActor(userID: String, accessToken: String) extends Actor with SpotifyInterfaceImpl  {
	var playlistID = "";

	def receive = {
    case GetAllPlaylists(ctx) => ctx.complete(getAllPlaylists(accessToken, userID)) 
    case (listID: PlaylistID) => playlistID = listID.id
    	sender ! s"Playlist set to $playlistID"
  }
}