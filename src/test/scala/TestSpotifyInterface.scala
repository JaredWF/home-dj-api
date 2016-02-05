package org.eclectek.dj

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import com.mashape.unirest.http.exceptions._

trait TestSpotifyInterface {
	def getAccessToken(accessCode: String): Try[String] = Failure(new UnirestException("Parsing failed"))
	def getUserID(accessToken: String): Try[String] = Failure(new UnirestException("Parsing failed"))
	def getPlaylistID(accessToken: String): Try[String] = Failure(new UnirestException("Parsing failed"))
	def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String] = Failure(new UnirestException("Parsing failed"))
}