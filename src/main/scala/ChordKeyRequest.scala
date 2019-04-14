import akka.actor.Actor
case class findKey(key : Int)
class ChordKeyRequest(searchOrigin: Int) extends Actor{
  override def receive: Receive = {
    case findKey(key) => {
      println("starting to find key" + key)
      println("this is todo!!!!!!!!!")
    }
  }
}
