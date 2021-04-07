package at.shockbytes.warehouse.firebase

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.rules.BetaBox
import at.shockbytes.warehouse.util.completableOf
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

@BetaBox
class FirebaseBoxEngine<I, E> protected constructor(
    private val database: FirebaseDatabase,
    private val reference: String,
    private val mapper: Mapper<I, E>,
    clazz: Class<I>,
    private val idSelector: (I) -> String,
    cancelHandler: ((DatabaseError) -> Unit)?
) : BoxEngine<I, E> {

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
        ): FirebaseBoxEngine<I, E> {
            return FirebaseBoxEngine(
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
