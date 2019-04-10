import SingleNode.join
import akka.actor.{Actor, ActorRef}

object SingleNode {
  case class join(someNode: ActorRef)
}
case class SingleNode(Idx: Int, i: Int) extends Actor{
  override def receive: Receive = {
    case join(someNode) => {
      println(someNode) //to be continued
    }
  }
}
