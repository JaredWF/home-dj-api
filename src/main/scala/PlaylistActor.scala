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
import spray.routing.RequestContext

case class GetAllPlaylists(ctx: RequestContext)
case class GetPlaylistSongs()

class PlaylistActor(userID: String, accessToken: String) extends HttpService with Actor with SpotifyInterfaceImpl  {
  import context.dispatcher

  def actorRefFactory = context

	var playlistID = "";
  var songs = List[Song]();

	def receive = runRoute{
    (post & path("choosePlaylist")) {
      entity(as[PlaylistID]) { p =>
        println("choosing playlist " + p.id)
        val songsFuture = getPlaylistSongs(accessToken, userID, p.id)

        songsFuture.foreach { discoveredSongs => 
          songs = discoveredSongs
          playlistID = p.id
        }

        complete(songsFuture.map(songList => s"Playlist set to ${p.id}"))
      }
    } ~ 
    (post & path("add")) {
      entity(as[SongURI]) { songUri =>
        val idStart = songUri.uri.lastIndexOf(':') + 1
        val songId = songUri.uri.slice(idStart, songUri.uri.length)

        val filteredSongs = songs.filter(s => s.id == songId)
        if (accessToken == "" || userID == "" || playlistID == "") {
          complete(StatusCodes.Forbidden, "Please login before adding songs")
        } else if (filteredSongs.length > 0) {
          complete(StatusCodes.BadRequest, "Song already in playlist")
        } else {
          getSongFromID(songId).onComplete {
            case Success(discoveredSong) => songs = discoveredSong::songs
            case Failure(t) => println("failed to get song\n" + t)
          }

          complete(addSong(accessToken, userID, playlistID, songUri.uri))
        }
      }
    } ~ 
    (get & path("")) {
      getFromResource("search.html")
    } ~ 
    (get & path("getAllSongs")) {
      complete(songs)
    } ~
    (get & path("getPlaylists")) {
      complete(getAllPlaylists(accessToken, userID))
    }
  }
}