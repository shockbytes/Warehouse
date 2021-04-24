package at.shockbytes.warehouse.firebase

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.rules.BetaBox
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

@BetaBox
class FirebaseBoxEngine<I : FirebaseStorable, E> protected constructor(
    private val config: FirebaseBoxEngineConfiguration,
    private val mapper: Mapper<I, E>,
    clazz: Class<I>,
    private val idSelector: (I) -> String,
    cancelHandler: ((DatabaseError) -> Unit)?
) : BoxEngine<I, E> {

    private val database: FirebaseDatabase = config.database
    private val reference: String = config.reference

    private val subject = BehaviorSubject.create<List<I>>()

    init {
        subject.fromFirebase(
            database.getReference(reference),
            clazz,
            idSelector,
            cancelHandler
        )
    }

    override val id: BoxId = config.id

    override fun <ID> getElementForIdType(internalId: ID): Single<E> {
        return subject.value
            .find { v ->
                internalId == idSelector(v)
            }
            ?.let { internal ->
                Single.just(mapper.mapTo(internal))
            }
            ?: error("There are no values in ${this.id} with id: $internalId")
    }

    override fun getAll(): Observable<List<E>> = subject.map(mapper::mapListTo)

    override fun store(value: E): Single<E> {

        val internalRepresentation = mapper.mapFrom(value)
        val id = idSelector(internalRepresentation)

        val storeAction = if (config.useDefaultFirebaseId) {
            database.insertValueWithDefaultId(reference, internalRepresentation)

        } else {
            database.insertValueWithId(reference, internalRepresentation, id)
        }

        return storeAction
            .map(mapper::mapTo)
    }

    override fun update(value: E): Completable {
        return database.updateValue(reference, idSelector(mapper.mapFrom(value)), value)
    }

    override fun delete(value: E): Completable {
        return database.removeChildValue(reference, idSelector(mapper.mapFrom(value)))
    }

    override fun reset(): Completable {
        return database.removeReference(reference)
    }

    companion object {

        inline fun <reified I : FirebaseStorable, reified E> fromDatabase(
            mapper: Mapper<I, E>,
            noinline idSelector: (I) -> String,
            config: FirebaseBoxEngineConfiguration,
            noinline cancelHandler: ((DatabaseError) -> Unit)? = null
        ): FirebaseBoxEngine<I, E> {
            return FirebaseBoxEngine(
                config,
                mapper,
                I::class.java,
                idSelector,
                cancelHandler
            )
        }
    }
}
