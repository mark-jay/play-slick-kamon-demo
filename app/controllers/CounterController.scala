package controllers

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


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class CounterController @Inject()(
                                   val controllerComponents: ControllerComponents,
                                   @Named("counter-actor") counterActor: ActorRef,
                                 )(implicit executionContext: ExecutionContext) extends BaseController {

  implicit val timeout: Timeout = 5.seconds

  def count() = Action.async { implicit request: Request[AnyContent] =>
    (counterActor ? "count").mapTo[String].map { message =>
      Ok(s"count: ${message}")
    }
  }
}
