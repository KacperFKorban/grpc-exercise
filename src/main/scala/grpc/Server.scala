package grpc

import grpc.CryptoRequest.Type
import io.grpc._
import io.grpc.stub.StreamObserver

object Server extends App {
  val builder = ServerBuilder.forPort(9090)
  builder.addService(new EventsService)
  val server = builder.build()
  server.start()
  println("Server started")
  server.awaitTermination()
}

class EventsService extends EventsGrpc.EventsImplBase {

  private val cryptoGenerator = new CryptoGenerator
  private val crimeGenerator = new CrimeGenerator

  override def subscribeCrypto(request: CryptoRequest, responseObserver: StreamObserver[CryptoResponse]): Unit = {
    val filterAction = { r: CryptoResponse =>
      if(request.getType == Type.ABOVE) {
        r.getValue > request.getValue
      } else {
        r.getValue < request.getValue
      }
    }
    cryptoGenerator.get(request.getName).subscribe { response: CryptoResponse =>
      if(filterAction(response)) {
        responseObserver.onNext(response)
      }
    }
  }

  override def subscribeCrime(request: CrimeRequest, responseObserver: StreamObserver[CrimeResponse]): Unit = {
    crimeGenerator.get(request.getType).subscribe { response: CrimeResponse =>
      responseObserver.onNext(response)
    }
  }
}