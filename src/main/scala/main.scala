import akka.actor.{ActorSystem, Props}

object Main extends App {
  // Assert to check if we have exactly two arguments
  if (args.length != 3) {
    println("Three arguments needed")
  }
  else {
    try {
      //First argument is number of nodes, second is number of requests and third is wait time.
      val numNodes = args(0).toInt
      val numReq = args(1).toInt
      val waitTime = args(2).toInt
      val system = ActorSystem("ChordSystem")
      // default Actor constructor
      val chordActor = system.actorOf(Props(new ChordActor(numNodes, numReq, waitTime)), name = "chordactor")
      //Send message to start Chord network
      chordActor ! "startNetwork"
    }
    catch {
      case e: NumberFormatException => {
        println("Number of nodes and requests should be in integer format")
        System.exit(1)
      }
    }



  }

}

