import akka.actor.Actor

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
                var averageHops: Double = totalHops.toDouble/requestsReceived
                println("Total hops received is " + totalHops)

                println("Average hops for each request is " + averageHops)
            }
        }
    }
}