package at.shockbytes.warehouse.ledger

import at.shockbytes.warehouse.box.Box
import io.reactivex.rxjava3.core.Completable

sealed class BoxOperation<E> {

    abstract val name: String

    abstract fun perform(box: Box<E>): Completable

    data class InitOperation<E>(
        override val name: String = "init",
    ) : BoxOperation<E>() {
        // Do nothing here
        override fun perform(box: Box<E>): Completable = Completable.complete()
    }

    data class StoreOperation<E>(
        val value: E,
        override val name: String = "store",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.store(value)
    }

    data class UpdateOperation<E>(
        val value: E,
        override val name: String = "update",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.update(value)
    }

    data class DeleteOperation<E>(
        val value: E,
        override val name: String = "delete",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.delete(value)
    }

    data class MigrateOperation<E>(
        val value: List<E>,
        override val name: String = "migrate",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable {
            TODO("Not yet implemented")
        }
    }
}
