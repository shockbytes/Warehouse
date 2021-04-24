package at.shockbytes.warehouse.ledger

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.util.asCompletable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.concatAll
import kotlinx.serialization.Serializable

@Serializable
sealed class BoxOperation<E> {

    abstract val name: String

    abstract fun perform(box: Box<E>): Completable

    @Serializable
    data class InitOperation<E>(
        override val name: String = "init",
    ) : BoxOperation<E>() {
        // Do nothing here
        override fun perform(box: Box<E>): Completable = Completable.complete()
    }

    @Serializable
    data class StoreOperation<E>(
        val value: E,
        override val name: String = "store",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.store(value).asCompletable()
    }

    @Serializable
    data class UpdateOperation<E>(
        val value: E,
        override val name: String = "update",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.update(value)
    }

    @Serializable
    data class DeleteOperation<E>(
        val value: E,
        override val name: String = "delete",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.delete(value)
    }

    @Serializable
    data class ResetOperation<E>(
        override val name: String = "reset"
    ): BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.reset()
    }

    @Serializable
    data class MigrateOperation<E>(
        val values: List<E>,
        override val name: String = "migrate",
    ) : BoxOperation<E>() {
        override fun perform(box: Box<E>): Completable = box.storeBatch(values)
    }
}
