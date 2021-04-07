package at.shockbytes.warehouse.ledger

import at.shockbytes.warehouse.box.Box

sealed class BoxOperation<E> {

    abstract val name: String

    abstract fun perform(box: Box<E>)

    data class InitOperation<E>(
        override val name: String = "init",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>) = Unit
    }


    data class StoreOperation<E>(
        val value: E,
        override val name: String = "store",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>) {
            TODO("Not yet implemented")
        }
    }

    data class UpdateOperation<E>(
        val value: E,
        override val name: String = "update",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>) {
            TODO("Not yet implemented")
        }
    }

    data class DeleteOperation<E>(
        val value: E,
        override val name: String = "delete",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>) {
            TODO("Not yet implemented")
        }
    }

    data class MigrateOperation<E>(
        val value: List<E>,
        override val name: String = "migrate",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>) {
            TODO("Not yet implemented")
        }
    }
}
