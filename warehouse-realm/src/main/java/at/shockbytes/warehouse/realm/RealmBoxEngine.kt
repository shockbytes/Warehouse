package at.shockbytes.warehouse.realm

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.BoxEngine
import at.shockbytes.warehouse.box.BoxId
import at.shockbytes.warehouse.rules.BetaBox
import at.shockbytes.warehouse.util.completableOf
import at.shockbytes.warehouse.util.completableOnDefaultThread
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import io.realm.RealmQuery
import java.lang.IllegalStateException

/**
 * Still to do:
 * - Threading
 */
@Suppress("PROTECTED_CONSTRUCTOR_CALL_FROM_PUBLIC_INLINE")
@BetaBox
class RealmBoxEngine<I : RealmObject, E, ID> protected constructor(
    private val realm: Realm,
    private val storageClass: Class<I>,
    private val realmIdSelector: RealmIdSelector<I, ID>,
    private val mapper: Mapper<I, E>
) : BoxEngine<I, E> {

    override val id: BoxId = BoxId.of(NAME)

    override fun <ID> getElementForIdType(id: ID): Single<E> {
        return Single.fromCallable {
            realm.where(storageClass)
                .findValueById(id)
                .findFirst()
                ?.let(mapper::mapTo)
                ?: throw IllegalStateException("No value stored in ${this.id} for id $id")
        }
    }

    override fun getAll(): Observable<List<E>> {
        return realm.where(storageClass)
            .findAllAsync()
            .asFlowable()
            .map(mapper::mapListTo)
            .toObservable()
            // Required as long as Realm has no built-in RxJava3 support
            .`as`(RxJavaBridge.toV3Observable())
    }

    override fun store(value: E): Completable {
        return completableOnDefaultThread {
            realm.executeTransactionAsync { r ->
                r.copyToRealmOrUpdate(mapper.mapFrom(value))
            }
        }
    }

    override fun update(value: E): Completable {
        return completableOnDefaultThread {
            realm.executeTransaction { r ->
                r.where(storageClass)
                    .findValue(value)
                    .findFirst()
                    ?.let {
                        // Value exists, safe to overwrite with new data
                        r.copyToRealmOrUpdate(mapper.mapFrom(value))
                    }
            }
        }
    }

    override fun delete(value: E): Completable {
        return completableOnDefaultThread {
            realm.executeTransaction { realm ->
                realm.where(storageClass)
                    .findValue(value)
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

    private fun RealmQuery<I>.findValue(value: E): RealmQuery<I> {
        return realmIdSelector.equalToByValue(this, mapper.mapFrom(value))
    }

    private fun <ID> RealmQuery<I>.findValueById(id: ID): RealmQuery<I> {
        return realmIdSelector.equalToById(this, id)
    }

    override fun reset(): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.deleteAll()
            }
        }
    }

    companion object {

        const val NAME = "realm-android"

        inline fun <reified I : RealmObject, E, ID> fromRealm(
            config: RealmConfiguration = RealmConfiguration.Builder().build(),
            realmIdSelector: RealmIdSelector<I, ID>,
            mapper: Mapper<I, E>,
        ): RealmBoxEngine<I, E, ID> {
            return RealmBoxEngine(
                Realm.getInstance(config).apply { refresh() },
                I::class.java,
                realmIdSelector,
                mapper
            )
        }
    }
}