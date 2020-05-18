package grpc

import rx.lang.scala.Observable

import scala.util.Random

class CryptoGenerator {
  private val random = new Random()

  private val generators: Map[String, Observable[CryptoResponse]] =
    List("ETH", "BTC", "ADA", "LTC").map{
      _ -> Observable.from(new Iterator[CryptoResponse] {
        override def hasNext: Boolean = true

        override def next(): CryptoResponse = {
          Thread.sleep(random.nextInt(5) * 1000 + 5000)
          val value = random.nextInt(2500) + 1000
          CryptoResponse.newBuilder().setValue(value).build()
        }
      }.toIterable)
    }.toMap

  def get(name: String): Observable[CryptoResponse] = {
    generators(name)
  }
}
