import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

case class Address(pincode: Int, Street: String)
case class Person(name: String, age: Int)
case class PersonNested(name: String, age: Int, address: Address)

trait PersonMapper extends SprayJsonSupport {
  implicit val addressFormat = jsonFormat2(Address)
  implicit val personFormat = jsonFormat2(Person)
  implicit val personNestedFormat = jsonFormat3(PersonNested)
}

object RoutingDSL extends PersonMapper {
  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future map/flatmap in the end
    implicit val executionContext = system.dispatcher

    val requestHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) => {

        val person = Person("Anand", 20)
        HttpResponse(entity = person)
      }

      case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
        HttpResponse(entity = "PONG!")

      case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
        sys.error("BOOM!")

      case r: HttpRequest =>
        r.discardEntityBytes() // important to drain incoming HTTP Entity stream
        HttpResponse(404, entity = "Unknown resource!")
    }

    val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8081)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
}
