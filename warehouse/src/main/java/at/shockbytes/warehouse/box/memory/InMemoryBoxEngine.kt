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
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.lang.IllegalStateException

class InMemoryBoxEngine<I, E, ID> private constructor(
    private val mapper: Mapper<I, E>,
    private val idSelector: (I) -> ID,
    override val id: BoxId = BoxId.of(NAME),
    initialData: List<E> = listOf()
) : BoxEngine<I, E> {

    private val storage: MutableList<I> = mutableListOf()

    private val publisher: BehaviorSubject<List<I>> = BehaviorSubject.create()

    init {
        storage.addAll(mapper.mapListFrom(initialData))
    }

    override fun <ID> getElementForIdType(id: ID): Single<E> {
        return storage
            .find { value -> idSelector(value) == id }
            ?.let { internal -> Single.just(mapper.mapTo(internal)) }
            ?: Single.error(IllegalStateException("No value stored in ${this.id} for id $id"))
    }

    override fun getAll(): Observable<List<E>> {
        return publisher
            .map(mapper::mapListTo)
    }

    override fun store(value: E): Completable {
        return completableOf {
            val internal = mapper.mapFrom(value)
            storage.add(internal)
        }.doOnComplete {
            publisher.onNext(storage)
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
        ): InMemoryBoxEngine<E, E, String> = InMemoryBoxEngine(IdentityMapper(), idSelector)

        fun <I, E, ID> custom(
            name: String,
            mapper: Mapper<I, E>,
            idSelector: (I) -> ID
        ): InMemoryBoxEngine<I, E, ID> = InMemoryBoxEngine(mapper, idSelector, BoxId.of(name))

        internal fun <I, E, ID> withData(
            name: String,
            mapper: Mapper<I, E>,
            idSelector: (I) -> ID,
            data: List<E>
        ): InMemoryBoxEngine<I, E, ID> = InMemoryBoxEngine(mapper, idSelector, BoxId.of(name), data)
    }
}
