package org.eclectek.dj

import scala.util.Try

trait SpotifyInterface {
	def getAccessToken(accessCode: String): Try[String]
	def getUserID(accessToken: String): Try[String]
	def getPlaylistID(accessToken: String): Try[String]
	def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String]
}