package at.shockbytes.warehouse.box.log

import android.util.Log
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.util.completableOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class LogBoxEngine<I, E> private constructor(
    private val tag: String
) : BoxEngine<I, E> {

    override val name: String = NAME

    override fun getSingleElement(id: String): Single<E> {
        Log.d(tag, "Trying to get resource with id: $id")
        return Single.error(IllegalStateException("Cannot invoke this here..."))
    }

    override fun getAll(): Observable<List<E>> {
        Log.d(tag, "Loading all messages from $name")
        return Observable.empty()
    }

    override fun store(value: E): Completable {
        Log.e(tag, "Store $value in $name")
        return Completable.fromCallable {
            Log.d(DEFAULT_TAG, "Storing new value: $value")
        }
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

    companion object {

        fun <E> default(): LogBoxEngine<E, E> = LogBoxEngine(DEFAULT_TAG)
        fun <E> withTag(tag: String): LogBoxEngine<E, E> = LogBoxEngine(tag)

        private const val DEFAULT_TAG = "LogBox"

        const val NAME = "log-android"
    }
}
