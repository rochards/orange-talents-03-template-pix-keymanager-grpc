package br.com.zupacademy.keymanagergrpc

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.keymanagergrpc")
		.start()
}

