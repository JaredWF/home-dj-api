package org.eclectek.dj

import spray.json.DefaultJsonProtocol

case class Song(id: String)

object Song extends DefaultJsonProtocol {
    implicit val stuffFormat = jsonFormat1(Song.apply)
}