package actors

import java.net.InetSocketAddress
import java.text.DecimalFormat

import actors.PricesActor.PriceRequest
import akka.Done
import akka.actor._
import akka.http.impl.util.RichHttpRequest
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.settings.{ClientConnectionSettings, ConnectionPoolSettings}
import akka.stream.OverflowStrategy
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import javax.inject._
import services.SlickSessionHolder

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.functional.syntax._


class BinancePricesActor @Inject()() extends PricesActor("www.binance.com", 443, true)

class PricesActor(
                   host: String,
                   port: Int,
                   secure: Boolean,
                 ) extends Actor {

  implicit val executionContext = context.dispatcher
  implicit val system = context.system

  private val settings = ConnectionPoolSettings(context.system)
    .withMaxConnections(20)
    .withMinConnections(5)
    .withMaxOpenRequests(512)
    .withMaxConnectionLifetime(10.seconds)
    .withConnectionSettings(ClientConnectionSettings(context.system))

  private val connection: Flow[(HttpRequest, PriceRequest), (Try[HttpResponse], PriceRequest), Http.HostConnectionPool] = {
    if (secure) {
      Http().cachedHostConnectionPoolHttps[PriceRequest](
        host,
        port,
        settings = settings,
        //    connectionContext = HttpsContextProvider.createContext(configConnectionConfig)
      )
    } else {
      Http().cachedHostConnectionPool[PriceRequest](
        host,
        port,
        settings = settings,
        //    connectionContext = HttpsContextProvider.createContext(configConnectionConfig)
      )
    }
  }

  def makeRequest(request: PriceRequest) = {
    HttpRequest(HttpMethods.GET, uri = "/api/v3/depth?symbol=BTCUSDT&limit=5")
  }

  private val replyPriceSink: Sink[(Try[HttpResponse], PriceRequest), Future[Done]] = Sink.foreach(item => {
    val (maybeResponse, request) = item
    maybeResponse match {
      case Success(httpResponse) => {
        if (httpResponse.status == StatusCodes.OK) {
          httpResponse.entity.toStrict(10.seconds).onComplete {
            case Success(result) => {
              val body = result.getData().utf8String
              val formatted: String = getPrice(body)
              request.respondTo ! s"price = ${formatted}"
            }
            case Failure(exception) => {
              exception.printStackTrace()
              request.respondTo ! s"failed to read response entity ${exception.getMessage}"
            }
          }
        } else {
          request.respondTo ! s"status != 200(${httpResponse.status})"
        }
      }
      case Failure(exception) => {
        exception.printStackTrace()
        request.respondTo ! s"failed to read response entity ${exception.getMessage}"
      }
    }
  })

  private val ((down, _connectionPool), closed) =
    Source.actorRef[PriceRequest](100000, OverflowStrategy.fail)
      .map(request => (makeRequest(request), request))
      .viaMat(connection)(Keep.both)
      .toMat(replyPriceSink)(Keep.both)
      .run()

  // body -> price
  private def getPrice(body: String) = {
    val parsed = Json.parse(body)
    // {"lastUpdateId":13151736580,"bids":[["48858.38000000","1.61188800"],["48858.36000000","0.25192500"],["48858.34000000","0.23588300"],["48854.84000000","0.22309400"],["48854.83000000","0.31426200"]],"asks":[["48858.39000000","2.89640600"],["48860.01000000","0.47333500"],["48862.52000000","0.24247200"],["48864.55000000","0.00136800"],["48864.60000000","0.01000000"]]}
    val price = (parsed \ "asks" \ 0 \ 0).get.as[String]
    val formatted = new DecimalFormat("#############.##").format(BigDecimal(price))
    formatted
  }

  override def receive: Receive = {
    case "btc" => {
      down ! PriceRequest("btc", sender())
      // https://github.com/binance/binance-spot-api-docs/blob/master/rest-api.md#order-book
      // https://binance.com/api/v3/depth?symbol=BTCUSDT&limit=5
    }
  }

}

object PricesActor {
  case class PriceRequest(asset: String, respondTo: ActorRef)
}
