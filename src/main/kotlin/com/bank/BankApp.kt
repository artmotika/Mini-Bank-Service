package com.bank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BankApp

fun main(args: Array<String>) {
    runApplication<BankApp>(*args)
}