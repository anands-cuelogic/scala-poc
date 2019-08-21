package scala

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import scala.io.StdIn
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.collection.mutable.ListBuffer

case class Cartridge(name: String, id: Int)
case class CartridgeList(response: List[Cartridge])

/*trait CartridgeObj extends SprayJsonSupport {
  implicit val cartridge = jsonFormat1(Cartridge)
  implicit val cartridgeList = jsonFormat1(CartridgeList)
}
*/

case class Address(pincode: Int, Street: String)
case class Person(name: String, age: Int)
//case class PersonList(response: List[Person])
case class PersonNested(name: String, age: Int, address: Address)

trait PersonMapper extends SprayJsonSupport {
  implicit val addressFormat = jsonFormat2(Address)
  implicit val personFormat = jsonFormat2(Person)
  implicit val personNestedFormat = jsonFormat3(PersonNested)
//  implicit val personList = jsonFormat1(PersonList)
}

class MyRoutes extends PersonMapper with SprayJsonSupport {
  def getRoutes  = {

    val route: Route = concat(
      path("user" ) {
        concat(
          get {
            println("In get Request");
            val person = new Person("A", 20)
            var personList = List[Person]()
            personList ::= Person("A", 2)
            personList ::= Person("D", 5)
            personList ::= Person("B", 3)
            personList ::= Person("C", 4)

            val connection = DBConnection.getConnection
            val cartridgeDetails = DBConnection.executeQuery(connection, "select * from Cartridge")
            var cartridgeList: List[Cartridge] = List[Cartridge]()

            println("Cartridge List")

            onSuccess(cartridgeDetails) { response =>
              complete (personList)
            }
          },
          post {
            entity(as[Person]) { request =>
              println("In POst "+ request)
              complete("From Post" + request.name)
            }
          }
        )
      },
      path("user" / "list") {
        get {
          var personNestedList = List[PersonNested]();

          personNestedList ::= PersonNested("Anand", 12, Address(123, "Pune"));
          personNestedList ::= PersonNested("Azad", 22, Address(123, "Nashik"));
          personNestedList ::= PersonNested("Ajay", 32, Address(123, "Mumbai"));

          // complete(personNestedList)
          complete("FROM USER LIST")
        }
      },
      path("order" / "get") {
        get {
          parameterMap { params =>
            println("Param of order "+ params)
            complete("This is from order")
          }
        }
      }
    )
    route
  }

}

object WebServer extends App {
//  def main1(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route = new MyRoutes().getRoutes
    /*val route =  concat( path("user") {
      concat(
        get {

        val connection = DBConnection.getConnection
        val cartridgeDetails = DBConnection.executeQuery(connection, "select * from Cartridge")
        var cartridgeList: List[Cartridge] = List[Cartridge]()
        cartridgeDetails onComplete {
          case Success(data) => {
            while(data.next) {
              println("Cartridge Name "+data.getString("CartridgeName"))
              cartridgeList ::= Cartridge(data.getString("CartridgeName"), data.getInt("idCartridge"))
            }

            for ( c <- cartridgeList) {
              println("Cartridge :: "+ c.name + " " + c.id)
            }
          }
          case Failure(t) => println("An error has occurred: "+ t.getMessage)
        }
        println(">>>>>>>>>>>>>>>");

        // complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http<h1>"))

        onSuccess(cartridgeDetails) { response =>
          complete {
            cartridgeList
            }
        }
      },
        post {
          entity(as[CartridgeList]) { Cartridge =>
            println("In POst")
            println("Request is ")
            complete("Hey")
          }
        }
      )
    }
    )

     */

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080\nPress RETURN to stop...")
    StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

//  }
}