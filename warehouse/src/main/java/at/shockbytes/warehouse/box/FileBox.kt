package at.shockbytes.warehouse.box

import android.content.Context
import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.rules.ExperimentalBox
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@ExperimentalBox
class FileBox<I, E>(
    context: Context,
    fileName: String,
    mapper: Mapper<I, E>,
    idSelector: (I) -> String,
    private val fileSerializer: FileSerializer<E>
) : Box<I,E>(mapper, idSelector) {

    override val name: String = "file-android"

    override fun getAll(): Observable<List<E>> {
        TODO("Not yet implemented")
    }

    override fun store(value: E): Completable {
        TODO("Not yet implemented")
    }

    override fun update(value: E): Completable {
        TODO("Not yet implemented")
    }

    override fun delete(value: E): Completable {
        TODO("Not yet implemented")
    }

    interface FileSerializer<T> {

        fun serializeToFile(value: T): String

        fun serializeFromFile(str: String): T
    }

    override fun getSingleElement(id: String): Single<E> {
        TODO("Not yet implemented")
    }
}