package grpc

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class EventsStreamObserver<T>(val name: String) : StreamObserver<T> {
    override fun onNext(result: T): Unit = println("[$name] $result")

    override fun onError(t: Throwable): Unit = println("DISCONNECTED")

    override fun onCompleted(): Unit = println("STREAM ENDED")
}

class EventsClient(private val channel: ManagedChannel) : Closeable {

    private val stub = EventsGrpc.newStub(channel).withWaitForReady()

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun callbackCrypto(name: String) = EventsStreamObserver<CryptoResponse>(name)

    fun callbackCrime(name: String) = EventsStreamObserver<CrimeResponse>(name)

    fun crypto(request: CryptoRequest): Unit =
        stub.subscribeCrypto(request, callbackCrypto(request.name))

    fun crime(request: CrimeRequest): Unit =
        stub.subscribeCrime(request, callbackCrime(request.type.toString()))
}

// SUB CRIME ARSON
// SUB CRIME MURDER
// SUB CRYPTO ETH > 0
// SUB CRYPTO ADA < 3000

fun main() {
    val channel = ManagedChannelBuilder.forAddress("localhost", 9090).enableRetry().usePlaintext().build()

    val cryptoSubscriptions = mutableListOf<CryptoRequest>()
    val crimeSubscriptions = mutableListOf<CrimeRequest>()

    val client = EventsClient(channel)

    val thread = object : Thread() {
        override fun run() {
            var prevState = ConnectivityState.READY
            while (true) {
                sleep(1000)
                val state = channel.getState(true)
                if(state == ConnectivityState.READY && prevState != ConnectivityState.READY) {
                    println("CONNECTED")
                    cryptoSubscriptions.forEach {
                        client.crypto(it)
                    }
                    crimeSubscriptions.forEach {
                        client.crime(it)
                    }
                }
                prevState = state
            }
        }
    }
    thread.start()

    while (true) {
        val input = readLine()!!
        val tokens = input.split(' ')

        when(tokens[0]) {
            "SUB" -> handleSubscribe(tokens, input, cryptoSubscriptions, crimeSubscriptions, client)
            "EXIT" -> exitProcess(0)
            else -> handleWrongInput(input)
        }
    }
}

fun handleWrongInput(input: String) {
    println("WRONG INPUT: $input")
    println("Input format should be: SUB TOPIC_TYPE [ARGUMENTS]")
}

fun handleSubscribe(tokens: List<String>, input: String, cryptoSubscriptions: MutableList<CryptoRequest>, crimeSubscriptions: MutableList<CrimeRequest>, client: EventsClient) {
    when(tokens[1]) {
        "CRYPTO" -> {
            if (tokens.size == 5 && tokens[4].toIntOrNull() != null) {
                if(tokens[3] == ">") {
                    val request = CryptoRequest.newBuilder().setName(tokens[2]).setType(CryptoRequest.Type.ABOVE).setValue(tokens[4].toInt()).build()
                    client.crypto(request)
                    cryptoSubscriptions += request
                } else if(tokens[3] == "<") {
                    val request = CryptoRequest.newBuilder().setName(tokens[2]).setType(CryptoRequest.Type.BELOW).setValue(tokens[4].toInt()).build()
                    client.crypto(request)
                    cryptoSubscriptions += request
                } else {
                    handleWrongInput(input)
                }
            }
        }
        "CRIME" -> {
            if (tokens.size == 3) {
                if(tokens[2] == "MURDER") {
                    val request = CrimeRequest.newBuilder().setType(CrimeType.MURDER).build()
                    client.crime(request)
                    crimeSubscriptions += request
                } else if(tokens[2] == "BURGLARY") {
                    val request = CrimeRequest.newBuilder().setType(CrimeType.BURGLARY).build()
                    client.crime(request)
                    crimeSubscriptions += request
                } else if(tokens[2] == "ARSON") {
                    val request = CrimeRequest.newBuilder().setType(CrimeType.ARSON).build()
                    client.crime(request)
                    crimeSubscriptions += request
                } else if(tokens[2] == "FRAUD") {
                    val request = CrimeRequest.newBuilder().setType(CrimeType.FRAUD).build()
                    client.crime(request)
                    crimeSubscriptions += request
                } else {
                    handleWrongInput(input)
                }
            }
        }
        else -> handleWrongInput(input)
    }
}
