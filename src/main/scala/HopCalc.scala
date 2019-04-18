import akka.actor.{Actor, ActorRef, Props}

case class getHops(hopCount: Int)

class HopCalc(totalRequests: Int) extends Actor {
    var totalHops: Int = 0
    var requestsReceived: Int = 0
    
    def receive = {
        case getHops(hopCount: Int) => {
            totalHops += hopCount
            requestsReceived += 1

            if (requestsReceived == totalRequests) {
                //println(totalHops)
                //println(requestsReceived)
                var averageHops: Double = Math.floor(totalHops/requestsReceived)
                println("Average hops for each request is " + averageHops)
            }
        }
    }
}