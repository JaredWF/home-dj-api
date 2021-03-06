package org.eclectek.dj

import scala.util.Try
import scala.concurrent.Future


trait SpotifyInterface {
	def getAccessTokens(accessCode: String): Future[(String, String)]
  def getTokenFromRefresh(refreshToken: String): Future[String]
	def getUserID(accessToken: String): Future[String]
	def getPlaylistID(accessToken: String): Future[String]
	def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Future[String]
	def getAllPlaylists(accessToken: String, userID: String): Future[List[Playlist]]
	def getPlaylistSongs(accessToken: String, userID: String, playlistID: String): Future[List[Song]]
	def getPlaylistName(accessToken: String, userID: String, playlistID: String): Future[String]
	def getSongFromID(songId: String): Future[Song]
}