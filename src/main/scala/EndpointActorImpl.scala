package org.eclectek.dj

import akka.actor._
import scala.concurrent.ExecutionContext

class EndpointActorImpl extends Actor with EndpointActor {
	import context.dispatcher

  def actorRefFactory = context

  def receive = runRoute(route)
}