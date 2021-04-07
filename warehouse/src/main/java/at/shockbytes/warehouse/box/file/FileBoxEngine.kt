package at.shockbytes.warehouse.box.file

import android.content.Context
import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.rules.ExperimentalBox
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@ExperimentalBox
class FileBoxEngine<I, E> protected constructor(
    context: Context,
    fileName: String,
    private val mapper: Mapper<I, E>,
    private val idSelector: (I) -> String,
    private val fileSerializer: FileSerializer<E>
) : BoxEngine<I, E> {

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

    override fun getSingleElement(id: String): Single<E> {
        TODO("Not yet implemented")
    }

    companion object {

        inline fun <reified I, E> fromContext(
            context: Context,
            fileName: String,
            mapper: Mapper<I, E>,
            fileSerializer: FileSerializer<E>,
            noinline idSelector: (I) -> String,
        ): FileBoxEngine<I, E> {
            return FileBoxEngine(
                context,
                fileName,
                mapper,
                idSelector,
                fileSerializer
            )
        }
    }
}