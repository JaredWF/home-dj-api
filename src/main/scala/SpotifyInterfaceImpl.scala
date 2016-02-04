package org.eclectek.dj

import com.mashape.unirest.http._
import org.json.JSONObject;

class SpotifyInterfaceImpl extends SpotifyInterface {
	def getAccessToken(accessCode: String): String = {
    val responseJSON = Unirest.post("https://accounts.spotify.com/api/token")
      .header("Authorization", "Basic " + System.getenv("hashed_secret"))
      .field("grant_type", "authorization_code")
      .field("code", accessCode)
      .field("redirect_uri", "http://home-dj.herokuapp.com/finishAuthorize")
      .asJson().getBody().getObject

      extractAccessToken(responseJSON)
  }

	def getUserID(accessToken: String): String

	def extractAccessToken(json: JSONObject) : String = json.getString("access_token")
}