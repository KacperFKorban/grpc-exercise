package grpc

import rx.lang.scala.Observable

import scala.util.Random
import scala.collection.JavaConversions._

class CrimeGenerator {
  private val random = new Random()

  private val names = List(
    "Kacper Korban",
    "Andrzej Ratajczak",
    "Filip Zybala",
    "Michal Szkarlat",
    "Marek Moryl"
  )

  private val generators: Map[Int, Observable[CrimeResponse]] =
    List(CrimeType.MURDER, CrimeType.ARSON, CrimeType.FRAUD, CrimeType.BURGLARY).map(_.getNumber).map(
      _ -> Observable.from(new Iterator[CrimeResponse] {
        override def hasNext: Boolean = true
        override def next(): CrimeResponse = {
          Thread.sleep(random.nextInt(5) * 1000 + 5000)
          val number = random.nextInt(250) + 1
          val suspects = (1 to 2).map(_ => Person.newBuilder().setAge(random.nextInt(20)+20).setName(names(random.nextInt(names.length))).build())
          CrimeResponse.newBuilder().setNumber(number).addAllSuspects(suspects).build()
        }
      }.toIterable)
    ).toMap

  def get(crimeType: CrimeType): Observable[CrimeResponse] = {
    generators(crimeType.getNumber)
  }
}
