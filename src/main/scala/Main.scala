package org.eclectek.dj

import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinRouter
import akka.io.IO
import spray.can.Http
import scala.util.Properties
import scala.concurrent._
import ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("on-spray-can")
    
    val service = system.actorOf(Props[EndpointActor], name = "RESTEndpoint")
    
    val p = Properties.envOrElse("PORT", "8080").toInt
    IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = p)
  }
}