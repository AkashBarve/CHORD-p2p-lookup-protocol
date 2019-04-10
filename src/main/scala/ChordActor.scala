import SingleNode.join
import akka.actor.{Actor, Props}

import scala.collection.mutable
case class closeProgram()

class ChordActor(numNodes: Int, numReq: Int) extends Actor {
  val nodes = new mutable.HashSet[Int]()
  var nodeId = 0
  for (i <- 1 to numNodes) {
    do {
      nodeId = ConsistentHashing.getRandomId(10);
    } while (nodes.contains(nodeId))
    nodes.add(nodeId)
  }
  println(nodes)
  val firstNode = nodes.head
  var node0 = context.actorOf(Props(new SingleNode(firstNode, 10)), "node:" + firstNode)
  node0 ! join(null)


  override def receive: Receive = {
    case closeProgram() =>
      context.system.shutdown()
  }
}

