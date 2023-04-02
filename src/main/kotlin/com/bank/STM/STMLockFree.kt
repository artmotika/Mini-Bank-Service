package com.bank.STM

import java.awt.print.PrinterAbortException
import java.util.concurrent.atomic.*

enum class TxStatus { ACTIVE, COMMITTED, ABORTED }

class Transaction {
    private val _status = AtomicReference(TxStatus.ACTIVE)
    val status: TxStatus get() = _status.get()

    fun <T> TVar<T>.read(): T = readIn(this@Transaction)
    fun <T> TVar<T>.write(x: T) = writeIn(this@Transaction, x)

    fun commit() {
        _status.compareAndSet(TxStatus.ACTIVE, TxStatus.COMMITTED)
    }

    fun abort() {
        _status.compareAndSet(TxStatus.ACTIVE, TxStatus.ABORTED)
    }
}

private val rootTx = Transaction().apply { commit() }

private class Loc<T>(
    val oldValue: T,
    val newValue: T,
    val owner: Transaction
) {
    fun valueIn(tx: Transaction, onActive: (Transaction) -> Unit): Any? =
        if (owner === tx) newValue
        else when (owner.status) {
            TxStatus.ABORTED -> oldValue
            TxStatus.COMMITTED -> newValue
            TxStatus.ACTIVE -> {
                onActive(owner)
                TxStatus.ACTIVE
            }
        }
}

class TVar<T>(initial: T) {
    private val loc = AtomicReference(Loc<T>(initial, initial, rootTx))

    fun openIn(tx: Transaction, update: (T) -> T): T {
        while (true) {
            val curLoc = loc.get()
            val curValue = curLoc.valueIn(tx) { owner -> contentionPolicy(tx, owner) }

            if (curValue === TxStatus.ACTIVE) continue

            val updValue = update(curValue as T)

            if (loc.compareAndSet(curLoc, Loc(curValue, updValue, tx))) {
                if (tx.status == TxStatus.ABORTED) throw PrinterAbortException()
                return updValue
            }
        }
    }

    fun readIn(tx: Transaction): T = openIn(tx) { it }

    fun writeIn(tx: Transaction, x: T) = openIn(tx) { x }
}

fun <T> atomic(block: Transaction.() -> T): T {
    while (true) {
        val transaction = Transaction()
        try {
            val result = block(transaction)
            transaction.commit()
            return result
        } catch (e: PrinterAbortException) {
            transaction.abort()
        }
    }
}

private fun contentionPolicy(tx: Transaction, owner: Transaction) {
    owner.abort()
}