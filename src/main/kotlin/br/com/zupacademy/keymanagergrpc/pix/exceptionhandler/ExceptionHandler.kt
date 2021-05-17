package br.com.zupacademy.keymanagergrpc.pix.exceptionhandler

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Around
@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, FUNCTION)
annotation class ExceptionHandler
