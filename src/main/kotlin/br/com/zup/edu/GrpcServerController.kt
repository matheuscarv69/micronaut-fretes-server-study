package br.com.zup.edu

import io.micronaut.grpc.server.GrpcEmbeddedServer
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Inject

@Controller("/grpc-server")
class GrpcServerController(@Inject val grpcServer: GrpcEmbeddedServer) {

    @Get("/stop")
    fun stop(): HttpResponse<String> {

        grpcServer.stop()

        return HttpResponse.ok("Is running? ${grpcServer.isRunning}")
    }

}