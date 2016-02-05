package org.eclectek.dj

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService
import spray.http.StatusCodes._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import com.mashape.unirest.http.exceptions._
import spray.httpx.SprayJsonSupport._

class EndpointActorSpec extends Specification with EndpointActor with Specs2RouteTest {
  def actorRefFactory = system

  override def getAccessToken(accessCode: String): Try[String] = Failure(new UnirestException("Parsing failed"))
  override def getUserID(accessToken: String): Try[String] = Failure(new UnirestException("Parsing failed"))
  override def getPlaylistID(accessToken: String): Try[String] = Failure(new UnirestException("Parsing failed"))
  override def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String] = Failure(new UnirestException("Parsing failed"))

	"EndpointActor" should {
    "return a 'pong!' response for GET requests to /ping" in {
      Get("/ping") ~> route ~> check {
        responseAs[String] === "pong!"
      }
    }

    "fail to add song" in {
      Post("/add", Song("spotify:track:3n3foZAJ9FO7uo0lodxWkp")) ~> route ~> check {
        responseAs[String] must contain("Please login before adding songs")
      }
    }

    "fail to get token" in {
      Get("/finishAuthorize?code=12345") ~> route ~> check {
        responseAs[String] must contain("Error getting token")
      }
    }

    "return good search page" in {
      Get("/") ~> route ~> check {
        responseAs[String] must contain("html")
      }
    }
  }
}

class EndpointActorSpec2 extends Specification with EndpointActor with Specs2RouteTest {
  def actorRefFactory = system

  override def getAccessToken(accessCode: String): Try[String] = Success("0000")
  override def getUserID(accessToken: String): Try[String] = Failure(new UnirestException("Parsing failed"))
  override def getPlaylistID(accessToken: String): Try[String] = Failure(new UnirestException("Parsing failed"))
  override def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String] = Failure(new UnirestException("Parsing failed"))

  "EndpointActor" should {
    "fail to get user id" in {
      Get("/finishAuthorize?code=12345") ~> route ~> check {
        responseAs[String] must contain("Error getting user id")
      }
    }
  }
}

class EndpointActorSpec3 extends Specification with EndpointActor with Specs2RouteTest {
  def actorRefFactory = system

  override def getAccessToken(accessCode: String): Try[String] = Success("0000")
  override def getUserID(accessToken: String): Try[String] = Success("1234")
  override def getPlaylistID(accessToken: String): Try[String] = Failure(new UnirestException("Parsing failed"))
  override def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String] = Failure(new UnirestException("Parsing failed"))

  "EndpointActor" should {
    "fail to get user id" in {
      Get("/finishAuthorize?code=12345") ~> route ~> check {
        responseAs[String] must contain("Error getting first playlist")
      }
    }
  }
}

class EndpointActorSpec4 extends Specification with EndpointActor with Specs2RouteTest {
  def actorRefFactory = system

  override def getAccessToken(accessCode: String): Try[String] = Success("0000")
  override def getUserID(accessToken: String): Try[String] = Success("1234")
  override def getPlaylistID(accessToken: String): Try[String] = Success("5678")
  override def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String] = Failure(new UnirestException("Parsing failed"))

  "EndpointActor" should {
    "Successfully authenticate" in {
      Get("/finishAuthorize?code=12345") ~> route ~> check {
        responseAs[String] must contain("Login successful")
      }
    }

    "Authenticate but fail to add song" in {
      Post("/add", Song("spotify:track:3n3foZAJ9FO7uo0lodxWkp")) ~> route ~> check {
        responseAs[String] must contain("Failed to add song")
      }
    }
  }
}

class EndpointActorSpec5 extends Specification with EndpointActor with Specs2RouteTest {
  def actorRefFactory = system

  override def getAccessToken(accessCode: String): Try[String] = Success("0000")
  override def getUserID(accessToken: String): Try[String] = Success("1234")
  override def getPlaylistID(accessToken: String): Try[String] = Success("5678")
  override def addSong(accessToken: String, userID: String, playlistID: String, songID: String): Try[String] = Success("9999")

  "EndpointActor" should {

    "Successfully authenticate" in {
      Get("/finishAuthorize?code=12345") ~> route ~> check {
        responseAs[String] must contain("Login successful")
      }
    }
    
    "Successfully add song" in {
      Post("/add", Song("spotify:track:3n3foZAJ9FO7uo0lodxWkp")) ~> route ~> check {
        responseAs[String] must contain("9999")
      }
    }
  }
}