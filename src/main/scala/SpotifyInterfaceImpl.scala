package org.eclectek.dj

import com.mashape.unirest.http._
import org.json.JSONObject;
import org.json.JSONArray;
import scala.util._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

trait SpotifyInterfaceImpl extends SpotifyInterface {
  val redirectURI = System.getenv("redirect_uri")

	def getAccessToken(accessCode: String): Future[String] = {
    flattenFutureTry(Future{
      Try(Unirest.post("https://accounts.spotify.com/api/token")
      .header("Authorization", "Basic " + System.getenv("hashed_secret"))
      .field("grant_type", "authorization_code")
      .field("code", accessCode)
      .field("redirect_uri", redirectURI)
      .asJson).map(json => extractAccessToken(json.getBody.getObject))
    })
  }

	def getUserID(accessToken: String): Future[String] = {
    flattenFutureTry(Future{Try(Unirest.get("https://api.spotify.com/v1/me")
      .header("Authorization", "Bearer " + accessToken)
      .asJson).map(json => extractUserID(json.getBody.getObject))})
  }

  def getPlaylistID(accessToken: String): Future[String] = {
    flattenFutureTry(Future{Try(Unirest.get("https://api.spotify.com/v1/me/playlists")
      .header("Authorization", "Bearer " + accessToken)
      .asJson).map(json => extractFirstPlaylistID(json.getBody.getObject))})
  }

  def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Future[String] = {  
    flattenFutureTry(Future{Try(Unirest.post("https://api.spotify.com/v1/users/" + userID + "/playlists/" + playlistID + "/tracks?uris=" + songID)
      .header("Authorization", "Bearer " + accessToken)
      .header("Content-Type", "application/json")
      .asJson).map(json => extractSnapshotId(json.getBody().getObject))})
  }

  def getAllPlaylists(accessToken: String, userID: String): Future[List[Playlist]] = {
    flattenFutureTry(Future{Try(Unirest.get("https://api.spotify.com/v1/me/playlists")
      .header("Authorization", "Bearer " + accessToken)
      .asJson).map(json => extractAllPlaylists(json.getBody.getObject).filter(_.owner == userID))})
  }

  def getPlaylistSongs(accessToken: String, userID: String, playlistID: String): Future[List[Song]] = {
    flattenFutureTry(Future{Try(Unirest.get(s"https://api.spotify.com/v1/users/$userID/playlists/$playlistID/tracks")
      .header("Authorization", "Bearer " + accessToken)
      .asJson).map(json => extractSongList(json.getBody().getObject))})
  }

  def getSongFromID(songId: String): Future[Song] = {
    flattenFutureTry(Future{Try(Unirest.get(s"https://api.spotify.com/v1/tracks/$songId")
      .asJson).map(json => extractSongFromTrack(json.getBody().getObject))})
  }


	def extractAccessToken(json: JSONObject): String = json.getString("access_token")

  def extractUserID(json: JSONObject): String = json.getString("id")

  def extractFirstPlaylistID(json: JSONObject): String = json.getJSONArray("items").getJSONObject(0).getString("id")

  def extractAllPlaylists(json: JSONObject): List[Playlist] = buildPlaylistList(json.getJSONArray("items"), 0, List[Playlist]())

  def buildPlaylistList(jsonArray: JSONArray, position: Int, playlistList: List[Playlist]): List[Playlist] = {
    if (position < jsonArray.length) {
      val jsonObject = jsonArray.getJSONObject(position)
      val imageArray = jsonObject.getJSONArray("images")
      val newPlaylist = Playlist(jsonObject.getString("name"), 
        jsonObject.getJSONObject("tracks").getInt("total"), 
        imageArray.getJSONObject(math.min(1, imageArray.length - 1)).getString("url"), 
        jsonObject.getString("id"),
        jsonObject.getJSONObject("owner").getString("id"))
      buildPlaylistList(jsonArray, position + 1, newPlaylist::playlistList)
    } else {
      playlistList
    }
  }

  def extractSnapshotId(json: JSONObject): String = {
    json.getString("snapshot_id")
  }

  def extractSongList(json: JSONObject): List[Song] = {
    def buildSongList(jsonArray: JSONArray, position: Int, songList: List[Song]): List[Song] = {
      if (position < jsonArray.length) {
        val track = jsonArray.getJSONObject(position).getJSONObject("track")

        buildSongList(jsonArray, position + 1, extractSongFromTrack(track)::songList)
      } else {
        songList
      }
    }

    buildSongList(json.getJSONArray("items"), 0, List[Song]())
  }

  def extractSongFromTrack(track: JSONObject): Song = {
    val imageArray = track.getJSONObject("album").getJSONArray("images")
    val imageURL = imageArray.getJSONObject(imageArray.length - 1).getString("url")
    val id = track.getString("id")
    val name = track.getString("name")
    val artistArray = track.getJSONArray("artists")
    val collatedNames = buildArtistList(artistArray, 0, List[String]()).foldRight("")((name, list) => list + name + ", ") //folding right to maintain list order
    Song(id, name, collatedNames.slice(0, collatedNames.length - 2), imageURL)
  }

  def buildArtistList(jsonArray: JSONArray, position: Int, artistList: List[String]): List[String] = {
    if (position < jsonArray.length) {
      buildArtistList(jsonArray, position + 1, jsonArray.getJSONObject(position).getString("name")::artistList)
    } else {
      artistList
    }
  }

  def flattenFutureTry[A](future: Future[Try[A]]): Future[A] = {
    future.flatMap {
      case Success(s) => Future.successful(s)
      case Failure(fail) => Future.failed(fail)
    }
  }
}