package Cartridge

import akka.actor.Actor
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

object CartridgeResponse {
  final case class getCartridge(cartridgeObj: Cartridge.models.Cartridge)
}

class Manager extends Actor{
  def receive: Receive = {
    case getCartridge =>
  }
}
