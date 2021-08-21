package controllers

import akka.actor.ActorRef
import akka.util.Timeout
import javax.inject.{Inject, Named, Singleton}
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}


import akka.actor.ActorRef
import javax.inject._
import play.api.mvc._
import play.api.mvc._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


import scala.concurrent.ExecutionContext

@Singleton
class PricesController @Inject()(
                                   val controllerComponents: ControllerComponents,
                                   @Named("prices-actor") pricesActor: ActorRef,
                                 )(implicit executionContext: ExecutionContext) extends BaseController {


  implicit val timeout: Timeout = 5.seconds

  def btc() = Action.async { implicit request: Request[AnyContent] =>
    (pricesActor ? "btc").mapTo[String].map { message =>
      Ok(s"result: ${message}")
    }
  }



}
