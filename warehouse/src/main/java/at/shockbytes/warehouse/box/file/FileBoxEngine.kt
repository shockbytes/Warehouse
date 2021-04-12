package at.shockbytes.warehouse.box.file

import android.content.Context
import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.rules.ExperimentalBox
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@ExperimentalBox
class FileBoxEngine<I, E, ID> protected constructor(
    context: Context,
    fileName: String,
    private val mapper: Mapper<I, E>,
    private val idSelector: (I) -> ID,
    private val fileSerializer: FileSerializer<E>
) : BoxEngine<I, E> {

    override val id: BoxId = BoxId.of("file-android")

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

    override fun <ID> getElementForIdType(id: ID): Single<E> {
        TODO("Not yet implemented")
    }

    override fun reset(): Completable {
        TODO("Not yet implemented")
    }

    companion object {

        inline fun <reified I, E, ID> fromContext(
            context: Context,
            fileName: String,
            mapper: Mapper<I, E>,
            fileSerializer: FileSerializer<E>,
            noinline idSelector: (I) -> ID,
        ): FileBoxEngine<I, E, ID> {
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