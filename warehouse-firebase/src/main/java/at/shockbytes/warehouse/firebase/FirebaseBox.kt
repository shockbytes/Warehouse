package at.shockbytes.warehouse.firebase

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.rules.BetaBox
import at.shockbytes.warehouse.util.completableOf
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

@BetaBox
class FirebaseBox<I, E> protected constructor(
    private val database: FirebaseDatabase,
    private val reference: String,
    mapper: Mapper<I, E>,
    clazz: Class<I>,
    idSelector: (I) -> String,
    cancelHandler: ((DatabaseError) -> Unit)?
) : Box<I, E>(mapper, idSelector) {

    private val subject = BehaviorSubject.create<List<I>>()

    init {
        subject.fromFirebase(
            database.getReference(reference),
            clazz,
            idSelector,
            cancelHandler
        )
    }

    override val name: String = "firebase"

    override fun getSingleElement(id: String): Single<E> {
        return subject
            .map { values ->
                values
                    .find { v -> id == idSelector(v) }
                    ?.let(mapper::mapTo)
                    ?: error("There are no values in $name with id: $id")
            }
            .singleOrError()
    }

    override fun getAll(): Observable<List<E>> = subject.map(mapper::mapListTo)

    override fun store(value: E): Completable {
        return completableOf {
            database.insertValue(reference, value)
        }
    }

    override fun update(value: E): Completable {
        return completableOf {
            database.updateValue(reference, idSelector(mapper.mapFrom(value)), value)
        }
    }

    override fun delete(value: E): Completable {
        return completableOf {
            database.removeChildValue(reference, idSelector(mapper.mapFrom(value)))
        }
    }

    companion object {

        inline fun <reified I, reified E> fromDatabase(
            database: FirebaseDatabase,
            reference: String,
            mapper: Mapper<I, E>,
            noinline idSelector: (I) -> String,
            noinline cancelHandler: ((DatabaseError) -> Unit)? = null
        ): FirebaseBox<I, E> {
            return FirebaseBox(
                database,
                reference,
                mapper,
                I::class.java,
                idSelector,
                cancelHandler
            )
        }
    }

}
