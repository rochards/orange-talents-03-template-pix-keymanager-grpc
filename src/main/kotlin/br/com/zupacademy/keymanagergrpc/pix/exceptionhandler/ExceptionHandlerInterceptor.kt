package br.com.zupacademy.keymanagergrpc.pix.exceptionhandler

import br.com.zupacademy.keymanagergrpc.pix.registra.ChavePixExistenteException
import io.grpc.BindableService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

/* solução de https://github.com/rafaelpontezup/checkout-x-propostas-grpc/blob/master/src/main/kotlin/br/com/zup/edu/shared/grpc/ExceptionHandlerInterceptor.kt*/
@Singleton
@InterceptorBean(ExceptionHandler::class)
class ExceptionHandlerInterceptor : MethodInterceptor<BindableService, Any?> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        try {
            return context.proceed() // continue com a cadeia de chamada, se for lançada exceções, trate-as abaixo
        } catch (e: Exception) {

            logger.error("Exceção '${e.javaClass.name}' lançada enquanto processava: ${context.targetMethod}")

            val statusError = when(e) {
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is ChavePixExistenteException -> Status.ALREADY_EXISTS.withDescription(e.message).asRuntimeException()
                else -> Status.UNKNOWN.withDescription("algo inesperado aconteceu").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            /*
            * Esse context tem acesso aos dois parâmetros do nosso método registraChavePix definido na classe
            * RegistraChavePix e podemos acessá-los como posições de um array
            * */
            responseObserver.onError(statusError)

            return null
        }
    }
}

/*
* - Pra criarmos um interceptor a classe deve implementar uma MethodInterceptor<TipoClasseASerInterceptada,
* TipoDoRetorno>;
* - @InterceptorBean(ExceptionHandler::class) -> está indicando qual anotação está associada a esse interceptor;
* - @Singleton é opcional aqui, mas lembre-se que se vc removê-la uma nova instância dessa classe será criada para
* injeção que pedirá um ExceptionHandlerInterceptor;
* - BindableService -> Nossa classe RegistraChavePixEndpoint extends de KeyManagerServiceGrpc.KeyManagerServiceImplBase.
* Essa classe implementa a interface BindableService, por isso passamos tal interface para o MethodInterceptor.
* */