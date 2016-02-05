package org.eclectek.dj

import com.mashape.unirest.http._
import org.json.JSONObject;
import scala.util.Try

trait SpotifyInterfaceImpl extends SpotifyInterface {
  val redirectURI = System.getenv("redirect_uri")

	def getAccessToken(accessCode: String): Try[String] = {
    Try(Unirest.post("https://accounts.spotify.com/api/token")
      .header("Authorization", "Basic " + System.getenv("hashed_secret"))
      .field("grant_type", "authorization_code")
      .field("code", accessCode)
      .field("redirect_uri", redirectURI)
      .asJson).map(json => extractAccessToken(json.getBody.getObject))
  }

	def getUserID(accessToken: String): Try[String] = {
    Try(Unirest.get("https://api.spotify.com/v1/me")
      .header("Authorization", "Bearer " + accessToken)
      .asJson).map(json => extractUserID(json.getBody.getObject))
  }

  def getPlaylistID(accessToken: String): Try[String] = {
    Try(Unirest.get("https://api.spotify.com/v1/me/playlists")
      .header("Authorization", "Bearer " + accessToken)
      .asJson).map(json => extractFirstPlaylistID(json.getBody.getObject))
  }

  def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String] = {   
    Try(Unirest.post("https://api.spotify.com/v1/users/" + userID + "/playlists/" + playlistID + "/tracks?uris=" + songID)
      .header("Authorization", "Bearer " + accessToken)
      .header("Content-Type", "application/json")
      .asJson).map(json => extractSnapshotId(json.getBody().getObject))
  }

	def extractAccessToken(json: JSONObject): String = json.getString("access_token")

  def extractUserID(json: JSONObject): String = json.getString("id")

  def extractFirstPlaylistID(json: JSONObject): String = json.getJSONArray("items").getJSONObject(0).getString("id")

  def extractSnapshotId(json: JSONObject): String = json.getString("snapshot_id")
}