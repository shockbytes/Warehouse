package at.shockbytes.warehouse

import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.ledger.Ledger
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface Warehouse<E> {

    fun store(
        value: E,
        writePredicate: (Box<E>) -> Boolean = { true }
    ): Completable

    fun update(
        value: E,
        writePredicate: (Box<E>) -> Boolean = { true },
    ): Completable

    fun delete(
        value: E,
        writePredicate: (Box<E>) -> Boolean = { true },
    ): Completable

    operator fun get(
        id: String,
        readPredicate: (Box<E>) -> Boolean = { true },
    ): Observable<List<E>>

    fun getAll(): Observable<List<E>>

    fun resetBox(id: BoxId): Completable

    fun setBoxEnabled(id: BoxId, isEnabled: Boolean): Completable

    fun forceBoxSynchronization(): Completable

    companion object {

        fun <E> new(
            boxes: List<Box<E>>,
            ledger: Ledger<E>,
            config: WarehouseConfiguration
        ): Warehouse<E> {
            return WarehouseImplementation(boxes, ledger, config)
        }
    }
}