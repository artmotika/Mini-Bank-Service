package com.bank.controller

import com.bank.STM.TVar
import com.bank.STM.atomic
import com.bank.exceptions.*
import org.apache.commons.codec.binary.Base64.encodeBase64
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("bank")
class BankController {
    private val users = mutableListOf<TVar<Account>>()

    private fun authorize(login: String, password: String): TVar<Account>? = atomic {
        val user = findUserByLogin(login) ?: return@atomic null
        if (!user.read().getPassword().equals(password)) return@atomic null
        return@atomic user
    }

    private fun findUserByLogin(login: String): TVar<Account>? = atomic {
        users.forEach { user ->
            if (user.read().getLogin().equals(login)) return@atomic user
        }
        return@atomic null
    }

    @PostMapping("/reg/{login}")
    fun createAccount(@PathVariable login: String, @RequestHeader(value = "password") password: String):
            Account = atomic {
        val account = findUserByLogin(login)
        if (account == null) {
            val newAccount = Account(login, password)
            users.add(TVar(newAccount))
            return@atomic newAccount
        }
        throw ExistingNameException()
    }

    //http authorization required
    @GetMapping("/get/account/{login}")
    fun getAccount(@PathVariable login: String, @RequestHeader(value = "password") password: String):
            Account = atomic {
        val account = authorize(login, password) ?: throw NotAuthorizedException()
        return@atomic account.read()
    }

    @GetMapping("/get/balance/{login}")
    fun getBalance(@PathVariable login: String, @RequestHeader(value = "password") password: String):
            BigDecimal = atomic {
        val currentUser = authorize(login, password) ?: throw NotAuthorizedException()
        return@atomic currentUser.read().getBalance()
    }

    @PutMapping("/update/account/{login}/{newLogin}")
    fun updateAccount(
        @PathVariable login: String, @RequestHeader(value = "password") password: String,
        @PathVariable newLogin: String, @RequestHeader(value = "newPassword") newPassword: String
    ):
            Account = atomic {
        val currentUser = authorize(login, password) ?: throw NotAuthorizedException()
        val currentUserRead = currentUser.read()
        currentUserRead.setLogin(newLogin)
        currentUserRead.setPassword(newPassword)
        currentUser.write(currentUserRead)
        return@atomic currentUserRead
    }

    @PutMapping("transfer/{login}/{to}/{amount}")
    fun transfer(
        @PathVariable login: String, @RequestHeader(value = "password") password: String,
        @PathVariable to: String, @PathVariable amount: String
    ): BigDecimal = atomic {
        val currentUser = authorize(login, password) ?: throw NotAuthorizedException()
        val toUser = findUserByLogin(to) ?: throw NotExistingNameException()
        val currentUserRead = currentUser.read()
        val toUserRead = toUser.read()
        val amountBigDecimal = amount.toBigDecimal()
        if (currentUserRead.getBalance() >= amountBigDecimal) {
            currentUserRead.setBalance(currentUserRead.getBalance() - amountBigDecimal)
            toUserRead.setBalance(toUserRead.getBalance() + amountBigDecimal)
            currentUser.write(currentUserRead)
            toUser.write(toUserRead)
            return@atomic currentUserRead.getBalance()
        } else {
            println(
                "money on the balance is low than the amount of the money you want to send !!! \n" +
                        "balance: ${currentUserRead.getBalance()} but amount is $amount"
            )
            throw AnyException()
        }
    }

    @PutMapping("takecredit/{login}/{amount}")
    fun takeCredit(
        @PathVariable login: String, @RequestHeader(value = "password") password: String,
        @PathVariable amount: String
    ): BigDecimal = atomic {
        val currentUser = authorize(login, password) ?: throw NotAuthorizedException()
        val currentUserRead = currentUser.read()
        currentUserRead.setBalance(currentUserRead.getBalance() + amount.toBigDecimal())
        currentUser.write(currentUserRead)
        return@atomic currentUserRead.getBalance()
    }

    @PutMapping("withdrawcredit/{login}/{amount}")
    fun withdrawCredit(
        @PathVariable login: String, @RequestHeader(value = "password") password: String,
        @PathVariable amount: String
    ): BigDecimal = atomic {
        val currentUser = authorize(login, password) ?: throw NotAuthorizedException()
        val currentUserRead = currentUser.read()
        val amountBigDecimal = amount.toBigDecimal()
        if (currentUserRead.getBalance() >= amountBigDecimal) {
            currentUserRead.setBalance(currentUserRead.getBalance() - amountBigDecimal)
            currentUser.write(currentUserRead)
            return@atomic currentUserRead.getBalance()
        } else {
            println(
                "money on the balance is low than the amount of the money you want to withdraw !!! \n" +
                        "balance: ${currentUserRead.getBalance()} but amount is $amount"
            )
            throw AnyException()
        }
    }
}

class Account(private var login: String?, private var password: String?) {
    private var balance = BigDecimal(0)

    fun setBalance(newBalance: BigDecimal) {
        balance = newBalance
    }

    @JvmName("getBalance1")
    fun getBalance(): BigDecimal {
        return balance
    }

    fun setLogin(newLogin: String) {
        login = newLogin
    }

    fun setPassword(newPassword: String) {
        password = newPassword
    }

    fun getLogin(): String? {
        return login
    }

    fun getPassword(): String? {
        return password
    }
}