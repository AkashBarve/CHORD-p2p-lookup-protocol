import akka.actor.{Actor, ActorRef}
import akka.actor.Props
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
case class closeProgram()

class ChordActor(numNodes: Int, numReq: Int) extends Actor {
  //var nodes = new ArrayBuffer[ActorRef]()
  val M = Math.ceil(Math.log(numNodes) / Math.log(2.0)).toInt
  var nodes = Array.ofDim[ActorRef](math.pow(2, M).toInt)

  override def receive: Receive = {
    case "startNetwork" =>
      val nodeIds = new mutable.HashSet[Int]()
      val joiningNodeIds = new mutable.HashSet[Int]()
      var nodeId = 0
      for (i <- 1 to numNodes) {
        do {
          nodeId = Hashify.getRandomId(M)
          //nodeId = Hashify.getRandomId(100)
        } while (nodeIds.contains(nodeId))
        nodeIds.add(nodeId)
      }
      println("Initial Node Ids: "+nodeIds)
      val sortedNodeIds = nodeIds.toSeq.sorted
      for (x <- sortedNodeIds) {
        var idx = sortedNodeIds.indexOf(x)
        nodes(sortedNodeIds(idx)) = context.system.actorOf(Props(new ChordNode(x, sortedNodeIds, M)))
        var predecessor = 0
        var successor = 0
        var fingertable = Array.ofDim[String](M)
        if(idx != 0 && idx != sortedNodeIds.size - 1
        ) {
          predecessor = sortedNodeIds(idx - 1)
          successor = sortedNodeIds(idx + 1)
        }
        else if(idx == 0) {
          successor = sortedNodeIds(idx + 1)
          predecessor = sortedNodeIds(sortedNodeIds.size - 1)
        }
        else {
          predecessor = sortedNodeIds(idx - 1)
          successor = sortedNodeIds(0)
        }

        for (i <- 0 to M-1) {
          var entry = (sortedNodeIds(idx) + math.pow(2, i).toInt) % math.pow(2, M).toInt
          var correspondingNode = 0
          if (sortedNodeIds.contains(entry)) {
            correspondingNode = entry
          } else if (entry > sortedNodeIds(sortedNodeIds.length - 1) || entry < sortedNodeIds(0)) {
            correspondingNode = sortedNodeIds(0)
          }
          else {
            for (c <- 0 to sortedNodeIds.size - 1) {
              if (entry > sortedNodeIds(c) && entry < sortedNodeIds(c + 1)) {
                correspondingNode = sortedNodeIds(c+1)
              }

            }

          }

          fingertable(i) = (entry + "," + correspondingNode)
          //println("fingerval " + correspondingNode)

        }


        println("creating node " + x)
        println("successor " + successor)
        println("predecessor " + predecessor)
        println("fingertable :")
        for(i <- 0 to fingertable.size - 1) {
          var printnode =  fingertable(i).split(",")
          println(x + " + " + math.pow(2,i) + ", " + printnode(1))
        }
        //nodes += context.actorOf(Props(new ChordNode(x, sortedNodeIds, M)), x.toString)
        //nodes ! InitNode(x, sortedNodeIds)
      }

      //Nodes to Join
      for (i <- 1 to 4) {
        do {
          nodeId = Hashify.getRandomId(M)
        } while (nodeIds.contains(nodeId))
        joiningNodeIds.add(nodeId)
      }
      println("Joining Node Ids : "+joiningNodeIds)
      val sortedJoiningNodeIds = joiningNodeIds.toSeq.sorted
      for (x <- sortedJoiningNodeIds) {
        var nIdx = sortedJoiningNodeIds.indexOf(x)
        nodes(sortedJoiningNodeIds(nIdx)) = context.system.actorOf(Props(new ChordNode(x, sortedJoiningNodeIds, M)))
        nodes(sortedJoiningNodeIds(nIdx)) ! join(sortedJoiningNodeIds(nIdx), nodes(sortedJoiningNodeIds(0)), nodes, numReq)
      }



    case closeProgram() =>
      context.system.shutdown()
  }
}

