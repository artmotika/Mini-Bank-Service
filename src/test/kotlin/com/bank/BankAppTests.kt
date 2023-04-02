package com.bank

//import org.apache.commons.codec.binary.Base64

//import java.util.*


import org.apache.commons.codec.binary.Base64
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.Executors


@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(OrderAnnotation::class)
class BankAppTests {
    val `quotation mark` = '"'

    @Autowired
    private val mvc: MockMvc? = null

    // No authorized tests
    @Test
    @Order(1)
    @Throws(Exception::class)
    fun `get account unauthorized test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(get("/bank/get/account/artem").header("password", base64password))
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(2)
    @Throws(Exception::class)
    fun `get balance unauthorized test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(get("/bank/get/balance/artem").header("password", base64password))
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(3)
    @Throws(Exception::class)
    fun `update account unauthorized test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        val newPassword = "123456789"
        val base64newPassword = String(Base64.encodeBase64(newPassword.toByteArray()))
        mvc!!.perform(
            put("/bank/update/account/artem/admin")
                .header("password", base64password).header("newPassword", base64newPassword)
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(4)
    @Throws(Exception::class)
    fun `transfer money unauthorized test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(put("/bank/transfer/artem/artem/100").header("password", base64password))
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(5)
    @Throws(Exception::class)
    fun `take credit unauthorized test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(put("/bank/takecredit/artem/100").header("password", base64password))
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(6)
    @Throws(Exception::class)
    fun `withdraw credit unauthorized test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(put("/bank/withdrawcredit/artem/100").header("password", base64password))
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }


    // Creating account tests
    @Test
    @Order(7)
    @Throws(Exception::class)
    fun `creating user test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        val result: MvcResult = mvc!!.perform(
            post("/bank/reg/admin")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val content: String = result.getResponse().getContentAsString()
        val contentArray = content.substring(1, content.length - 1).split(",")
            .map { it.replace("$`quotation mark`", "") }
        assertEquals("login:admin", contentArray[0])
        assertEquals("password:$base64password", contentArray[1])
        assertEquals("balance1:0", contentArray[2])
    }

    @Test
    @Order(8)
    @Throws(Exception::class)
    fun `creating existing user test`() {
        val password = "1234"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(
            post("/bank/reg/admin")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isNotAcceptable)
    }

    // Get account tests
    @Test
    @Order(12)
    @Throws(Exception::class)
    fun `get user account test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        val result: MvcResult = mvc!!.perform(
            get("/bank/get/account/admin")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
        val content: String = result.getResponse().getContentAsString()
        val contentArray = content.substring(1, content.length - 1).split(",")
            .map { it.replace("$`quotation mark`", "") }
        assertEquals("login:admin", contentArray[0])
        assertEquals("password:$base64password", contentArray[1])
        assertEquals("balance1:0", contentArray[2])
    }

    // Get balance tests
    @Test
    @Order(13)
    @Throws(Exception::class)
    fun `get user balance test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        val result: MvcResult = mvc!!.perform(
            get("/bank/get/balance/admin")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
        val content: String = result.getResponse().getContentAsString()
        assertEquals("0", content)
    }

    // Update account tests
    @Test
    @Order(14)
    @Throws(Exception::class)
    fun `update user account test`() {
        val password = "123"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        val newPassword = "pass"
        val base64newPassword = String(Base64.encodeBase64(newPassword.toByteArray()))
        val result: MvcResult = mvc!!.perform(
            put("/bank/update/account/admin/nikita")
                .header("password", base64password).header("newPassword", base64newPassword)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
        val content: String = result.getResponse().getContentAsString()
        val contentArray = content.substring(1, content.length - 1).split(",")
            .map { it.replace("$`quotation mark`", "") }
        assertEquals("login:nikita", contentArray[0])
        assertEquals("password:$base64newPassword", contentArray[1])
        assertEquals("balance1:0", contentArray[2])
    }

    // Take credit tests
    @Test
    @Order(15)
    @Throws(Exception::class)
    fun `take credit test`() {
        val password = "pass"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        val result: MvcResult = mvc!!.perform(
            put("/bank/takecredit/nikita/10000")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
        val content: String = result.getResponse().getContentAsString()
        assertEquals("10000", content)
    }

    // Withdraw credit tests
    @Test
    @Order(16)
    @Throws(Exception::class)
    fun `withdraw credit more than the user has test`() {
        val password = "pass"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(
            put("/bank/withdrawcredit/nikita/11000")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isNotAcceptable)
    }

    @Test
    @Order(17)
    @Throws(Exception::class)
    fun `withdraw credit test`() {
        val password = "pass"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        val result: MvcResult = mvc!!.perform(
            put("/bank/withdrawcredit/nikita/1000")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
        val content: String = result.getResponse().getContentAsString()
        assertEquals("9000", content)
    }

    // Transfer money tests
    @Test
    @Order(18)
    @Throws(Exception::class)
    fun `transfer money non-existent user test`() {
        val password = "pass"
        val base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(
            put("/bank/transfer/nikita/artem/1000")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isNotAcceptable)
    }

    @Test
    @Order(19)
    @Throws(Exception::class)
    fun `transfer money more than the user has test`() {
        var password = "qwerty"
        var base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc!!.perform(
            post("/bank/reg/artem")
                .header("password", base64password)
        )
            .andDo(print())
        password = "pass"
        base64password = String(Base64.encodeBase64(password.toByteArray()))
        mvc.perform(
            put("/bank/transfer/nikita/artem/10000")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isNotAcceptable)
    }

    @Test
    @Order(20)
    @Throws(Exception::class)
    fun `transfer money test`() {
        var password = "pass"
        var base64password = String(Base64.encodeBase64(password.toByteArray()))
        var result: MvcResult = mvc!!.perform(
            put("/bank/transfer/nikita/artem/1000")
                .header("password", base64password)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
        var content: String = result.getResponse().getContentAsString()
        assertEquals("8000", content)

        password = "qwerty"
        base64password = String(Base64.encodeBase64(password.toByteArray()))
        result = mvc.perform(
            get("/bank/get/balance/artem")
                .header("password", base64password)
        )
            .andExpect(status().isOk)
            .andReturn()
        content = result.getResponse().getContentAsString()
        assertEquals("1000", content)
    }

    //Parallel tests
    @Test
    @Order(21)
    @Throws(Exception::class)
    fun `parallel test 1`() {
        val threadPool = Executors.newFixedThreadPool(4)
        val task1 = Runnable {
            val password = "qwerty123"
            val base64password = String(Base64.encodeBase64(password.toByteArray()))
            mvc!!.perform(post("/bank/reg/anton").header("password", base64password))
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/takecredit/anton/10000")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/transfer/anton/yana/1000")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            mvc.perform(put("/bank/withdrawcredit/anton/11000").header("password", base64password))
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/withdrawcredit/anton/2000")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/transfer/anton/ivan/99")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
        }
        val task2 = Runnable {
            val password = "qwerty123"
            val base64password = String(Base64.encodeBase64(password.toByteArray()))
            mvc!!.perform(post("/bank/reg/anton").header("password", base64password))
        }
        val task3 = Runnable {
            val password = "qwerty123"
            val base64password = String(Base64.encodeBase64(password.toByteArray()))
            do {
                val result: MvcResult = mvc!!.perform(
                    post("/bank/reg/yana")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/takecredit/yana/90")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            mvc.perform(put("/bank/transfer/yana/anton/10000").header("password", base64password))
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/transfer/yana/anton/10")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            mvc.perform(put("/bank/withdrawcredit/yana/11000").header("password", base64password))
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/withdrawcredit/yana/80")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
        }
        val task4 = Runnable {
            val password = "123456789"
            val base64password = String(Base64.encodeBase64(password.toByteArray()))
            do {
                val result: MvcResult = mvc!!.perform(
                    post("/bank/reg/ivan")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/takecredit/ivan/10")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/withdrawcredit/ivan/2")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.getResponse().status != 200)
            mvc.perform(put("/bank/transfer/ivan/anton/1000").header("password", base64password))
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/transfer/ivan/anton/1")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.response.status != 200)
            do {
                val result: MvcResult = mvc.perform(
                    put("/bank/transfer/ivan/yana/2")
                        .header("password", base64password)
                )
                    .andReturn()
            } while (result.response.status != 200)
        }
        threadPool.execute(task1)
        threadPool.execute(task2)
        threadPool.execute(task3)
        threadPool.execute(task4)
        threadPool.shutdown()
        while (!threadPool.isTerminated) {
        }

        var password = "qwerty123"
        var base64password = String(Base64.encodeBase64(password.toByteArray()))
        var result = mvc!!.perform(get("/bank/get/balance/anton").header("password", base64password))
            .andExpect(status().isOk)
            .andReturn()
        var content = result.getResponse().getContentAsString()
        assertEquals("6912", content)

        result = mvc.perform(get("/bank/get/balance/yana").header("password", base64password))
            .andExpect(status().isOk)
            .andReturn()
        content = result.getResponse().getContentAsString()
        assertEquals("1002", content)

        password = "123456789"
        base64password = String(Base64.encodeBase64(password.toByteArray()))
        result = mvc.perform(get("/bank/get/balance/ivan").header("password", base64password))
            .andExpect(status().isOk)
            .andReturn()
        content = result.getResponse().getContentAsString()
        assertEquals("104", content)
    }
}
