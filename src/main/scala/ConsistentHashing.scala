import java.security.MessageDigest


import scala.util.Random

object Hashify {

  def getHash(str: String, i: Int): Int = {
    val shaId = MessageDigest.getInstance("SHA-1")
    val hash = shaId.digest(str.getBytes("UTF-8"))
    val hashBytes = hash.slice(0,4)
    var hashCode = 0
    for (i <- hashBytes.indices)
      hashCode = (hashCode << 8) + (hashBytes(i)& 0xff)
    val mask = 0xffffffff >>> 32-i
    hashCode = hashCode & mask
    hashCode
  }

  def getRandomId(i: Int): Int = {
    getHash(Random.alphanumeric.take(32).toString(), i)
//    val random = Random.alphanumeric.take(32).toString
//    var hash = 0
//    var ascii = 0
//    for(i <- random) {
//      ascii = i.toInt
//      hash = hash + ascii
//    }
//    hash % n+10
  }
}
