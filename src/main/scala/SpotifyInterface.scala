package org.eclectek.dj

trait SpotifyInterface {
	def getAccessToken(accessCode: String): String
	def getUserID(accessToken: String): String
}