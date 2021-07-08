package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesServiceGrpc::class.java)

    override fun calculaFrete(
        request: CalculaFreteRequest?,
        responseObserver: StreamObserver<CalculaFreteResponse>?
    ) {
        logger.info("Calculando frete para o cep: $request")

        val cep = request?.cep

        if (cep == null || cep.isBlank()) {
            val statusException =
                Status.INVALID_ARGUMENT
                    .withDescription("CEP deve ser informado")
                    .asRuntimeException()


            // lancando erro como response para o client
            responseObserver?.onError(statusException)
        }

        // verifica se o formato do CEP esta valido
        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val statusException = Status.INVALID_ARGUMENT
                .withDescription("CEP inválido")
                .augmentDescription("Formato informado deve ser CEP: 12345-789") // dica para o client
                .asRuntimeException()

            responseObserver?.onError(statusException)
        }

        // simulando uma validacao de seguranca
        if (cep.endsWith("333")) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED_VALUE)
                .setMessage("Usuário não pode acessar esse recurso")
                .addDetails(
                    Any.pack(
                        ErrorDetails.newBuilder()
                            .setCode(401)
                            .setMessage("token expirado")
                            .build()
                    )
                )
                .build()

            val statusException = StatusProto.toStatusRuntimeException(statusProto)

            responseObserver?.onError(statusException)
        }

        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.0) // logica complexa
            if (valor > 100.0) {
                throw IllegalStateException("Erro inesperado ao executar lógica de negócio!")
            }

        } catch (exception: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(exception.message)
                    .withCause(exception) // anexado ao Status de erro, mas nao eh enviado ao Client
                    .asRuntimeException()
            )
        }

        val response =
            CalculaFreteResponse.newBuilder()
                .setCep(request?.cep)
                .setValor(valor)
                .build()

        logger.info("Frete calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()

    }
}