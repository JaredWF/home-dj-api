package org.eclectek.dj

import scala.util.Try
import scala.concurrent.Future

case class Playlist(name: String, trackCount: Int, imageURL: String, id: String, owner: String)

trait SpotifyInterface {
	def getAccessToken(accessCode: String): Future[String]
	def getUserID(accessToken: String): Future[String]
	def getPlaylistID(accessToken: String): Future[String]
	def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Future[String]
	def getAllPlaylists(accessToken: String, userID: String): Future[List[Playlist]]
}