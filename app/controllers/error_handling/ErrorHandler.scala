package controllers.error_handling

import controllers.error_handling.exceptions.{ApplicationException, AuthorizationException}
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.Singleton
import play.api.libs.json.{JsObject, JsString}

@Singleton
class ErrorHandler extends HttpErrorHandler {
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)(JsObject(Map(
        "code" -> JsString(s"clientError_${statusCode}"),
        "description" -> JsString(s"${message}"),
      )))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    val res = exception match {
      case e: ApplicationException => {
        InternalServerError(JsObject(Map(
          "code" -> JsString(e.code()),
          "description" -> JsString(e.description()),
        )))
      }
      case e: Exception => {
        InternalServerError(JsObject(Map(
          "code" -> JsString("unknown"),
          "description" -> JsString("A server error occurred"),
        )))
      }
    }
    Future.successful(res)
  }
}
