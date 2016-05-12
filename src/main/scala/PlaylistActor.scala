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
case class GetPlaylistSongs()

class PlaylistActor(userID: String, accessToken: String) extends Actor with SpotifyInterfaceImpl  {
	var playlistID = "";
  var songs = List[Song]();

	def receive = {
    case GetAllPlaylists(ctx) => ctx.complete(getAllPlaylists(accessToken, userID)) 
    case (listID: PlaylistID) => playlistID = listID.id
      getPlaylistSongs(accessToken, userID, playlistID).onComplete {
        case Success(discoveredSongs) => songs = discoveredSongs
        case Failure(t) => println("failed to get songs\n" + t)
      }
    	sender ! s"Playlist set to $playlistID"
    case (song: SongID) => 
      val songURI = song.id
      val idStart = songURI.lastIndexOf(':') + 1
      val songID = songURI.slice(idStart, songURI.length)

      val filteredSongs = songs.filter(s => s.id == songID)
      if (accessToken == "" || userID == "" || playlistID == "") {
        sender ! Future("Please login before adding songs")
      } else if (filteredSongs.length > 0) {
        sender ! Future("Song already in playlist")
      } else {
        sender ! addSong(accessToken, userID, playlistID, songURI)
        getSongFromID(songURI).onComplete {
          case Success(discoveredSong) => songs = discoveredSong::songs
          case Failure(t) => println("failed to get song\n" + t)
        }
      }
    case (request: GetPlaylistSongs) => 
      println("returning all songs")
      sender ! songs
  }
}