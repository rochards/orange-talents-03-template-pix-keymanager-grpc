package br.com.zupacademy.keymanagergrpc.pix.exception.handler

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Around
@MustBeDocumented
@Retention(RUNTIME) // para @Around, deve ser RUNTIME
@Target(CLASS, FUNCTION)
annotation class ExceptionHandler

/*
* Aqui criamos uma anotação que vai disparar um MethodInterceptor.
* @Around -> indica ao Micronaut que a anotação @ExceptionHandler é Around Advice (advice that decorates a method or
* class).
* */