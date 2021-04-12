package at.shockbytes.warehouse.box.memory

import at.shockbytes.warehouse.IdentityMapper
import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.util.asObservable
import at.shockbytes.warehouse.util.completableOf
import at.shockbytes.warehouse.util.indexOfFirstOrNull
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.lang.IllegalStateException

class InMemoryBoxEngine<I, E> private constructor(
    private val mapper: Mapper<I, E>,
    private val idSelector: (I) -> String,
    override val id: BoxId = BoxId.of(NAME)
) : BoxEngine<I, E> {

    private val storage: MutableList<I> = mutableListOf()

    override fun getSingleElement(id: String): Single<E> {
        return storage
            .find { value -> idSelector(value) == id }
            ?.let { internal -> Single.just(mapper.mapTo(internal)) }
            ?: Single.error(IllegalStateException("No value stored in ${this.id} for id $id"))
    }

    override fun getAll(): Observable<List<E>> {
        return storage
            .asObservable()
            .map(mapper::mapListTo)
    }

    override fun store(value: E): Completable {
        return completableOf {
            val internal = mapper.mapFrom(value)
            storage.add(internal)
        }
    }

    override fun update(value: E): Completable {
        return completableOf {
            storage
                .indexOfFirstOrNull { v ->
                    idSelector(mapper.mapFrom(value)) == idSelector(v)
                }
                ?.let { index ->
                    storage[index] = mapper.mapFrom(value)
                }
                ?: throw IllegalStateException(
                    "No value stored in $id for updating id ${idSelector(mapper.mapFrom(value))}"
                )
        }
    }

    override fun delete(value: E): Completable {
        return completableOf {
            storage
                .indexOfFirstOrNull { v ->
                    idSelector(mapper.mapFrom(value)) == idSelector(v)
                }
                ?.let { index ->
                    storage.removeAt(index)
                }
                ?: throw IllegalStateException(
                    "No value stored in $id for deleting id ${idSelector(mapper.mapFrom(value))}"
                )
        }
    }

    override fun reset(): Completable {
        return completableOf {
            storage.clear()
        }
    }

    companion object {

        const val NAME = "memory"

        fun <E> default(
            idSelector: (E) -> String = { "" }
        ): InMemoryBoxEngine<E, E> = InMemoryBoxEngine(IdentityMapper(), idSelector)

        fun <I, E> custom(
            name: String,
            mapper: Mapper<I, E>,
            idSelector: (I) -> String
        ): InMemoryBoxEngine<I, E> = InMemoryBoxEngine(mapper, idSelector, BoxId.of(name))
    }
}
