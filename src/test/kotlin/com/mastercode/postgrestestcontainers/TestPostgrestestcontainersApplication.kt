package com.mastercode.postgrestestcontainers

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<PostgrestestcontainersApplication>().with(TestcontainersConfiguration::class).run(*args)
}
