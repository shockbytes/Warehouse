package at.shockbytes.warehouse.datastore

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.Serializer
import androidx.datastore.createDataStore
import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.rules.ExperimentalBox
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@ExperimentalBox
class ProtoDataStoreBoxEngine<I, E>(
    context: Context,
    dataStoreName: String,
    serializer: Serializer<I>,
    private val mapper: Mapper<I, E>,
    private val idSelector: (I) -> String
) : BoxEngine<I, E> {

    private val dataStore: DataStore<I> = createDataStore(context, dataStoreName, serializer)

    private fun createDataStore(
        context: Context,
        dataStoreName: String,
        serializer: Serializer<I>
    ): DataStore<I> {
        return context.createDataStore(dataStoreName, serializer)
    }

    override val name: String = "preferences-data-store-android"

    override fun getSingleElement(id: String): Single<E> {
        TODO("Not yet implemented")
    }

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
}