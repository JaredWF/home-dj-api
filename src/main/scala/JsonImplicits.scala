package org.eclectek.dj

import spray.json.DefaultJsonProtocol

case class Song(id: String)
case class Playlist(name: String, trackCount: Int, imageURL: String, id: String, owner: String)
case class PlaylistID(id: String)

object Song extends DefaultJsonProtocol {
  implicit val stuffFormat = jsonFormat1(Song.apply)
}

object Playlist extends DefaultJsonProtocol {
	implicit val playlistFormat = jsonFormat5(Playlist.apply)
}

object PlaylistID extends DefaultJsonProtocol {
	implicit val playlistIDFormat = jsonFormat1(PlaylistID.apply)
}