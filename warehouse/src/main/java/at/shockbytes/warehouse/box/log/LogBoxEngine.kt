package at.shockbytes.warehouse.box.log

import android.util.Log
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.util.completableOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class LogBoxEngine<I, E> private constructor(
    private val tag: String
) : BoxEngine<I, E> {

    override val id: BoxId = BoxId.of(NAME)

    override fun <ID> getElementForIdType(internalId: ID): Single<E> {
        Log.d(tag, "Trying to get resource with id: $internalId")
        return Single.error(IllegalStateException("Cannot invoke this here..."))
    }

    override fun getAll(): Observable<List<E>> {
        Log.d(tag, "Loading all messages from $id")
        return Observable.empty()
    }

    override fun store(value: E): Single<E> {
        Log.e(tag, "Store $value in $id")
        Log.d(DEFAULT_TAG, "Storing new value: $value")
        return Single.just(value)
    }

    override fun update(value: E): Completable {
        return completableOf {
            Log.d(tag, "Updating value: $value")
        }
    }

    override fun delete(value: E): Completable {
        return completableOf {
            Log.d(tag, "Deleting value: $value")
        }
    }

    override fun reset(): Completable {
        return Completable.complete()
    }

    companion object {

        fun <E> default(): LogBoxEngine<E, E> = LogBoxEngine(DEFAULT_TAG)
        fun <E> withTag(tag: String): LogBoxEngine<E, E> = LogBoxEngine(tag)

        private const val DEFAULT_TAG = "LogBox"

        const val NAME = "log-android"
    }
}
