package at.shockbytes.warehouse.realm

import at.shockbytes.warehouse.Mapper
import at.shockbytes.warehouse.box.Box
import at.shockbytes.warehouse.rules.BetaBox
import at.shockbytes.warehouse.util.completableOf
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import java.lang.IllegalStateException

/**
 * Still to do:
 * - Threading
 */
@BetaBox
class RealmBox<I : RealmObject, E> protected constructor(
    private val realm: Realm,
    private val storageClass: Class<I>,
    private val idProperty: String,
    mapper: Mapper<I, E>,
    idSelector: (I) -> String,
) : Box<I, E>(mapper, idSelector) {

    override val name: String = "realm-android"

    override fun getSingleElement(id: String): Single<E> {
        return Single.fromCallable {
            realm.where(storageClass)
                .equalTo(idProperty, id)
                .findFirst()
                ?.let(mapper::mapTo)
                ?: throw IllegalStateException("No value stored in $name for id $id")
        }
    }

    // TODO This is still painful!
    private val scheduler = AndroidSchedulers.mainThread()

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
        return completableOf(subscribeOn = scheduler) {
            realm.executeTransaction { r ->
                r.copyToRealmOrUpdate(mapper.mapFrom(value))
            }
        }
    }

    override fun update(value: E): Completable {
        return completableOf(subscribeOn = scheduler) {
            realm.executeTransaction { r ->
                r.where(storageClass)
                    .equalTo(idProperty, idSelector(mapper.mapFrom(value)))
                    .findFirst()
                    ?.let {
                        // Value exists, safe to overwrite with new data
                        r.copyToRealmOrUpdate(mapper.mapFrom(value))
                    }
            }
        }
    }

    override fun delete(value: E): Completable {
        return completableOf {
            realm.executeTransaction { realm ->
                realm.where(storageClass)
                    .equalTo(idProperty, idSelector(mapper.mapFrom(value)))
                    .findFirst()
                    ?.deleteFromRealm()
            }
        }
    }

    companion object {

        inline fun <reified I : RealmObject, E> fromRealm(
            config: RealmConfiguration = RealmConfiguration.Builder().build(),
            idProperty: String,
            mapper: Mapper<I, E>,
            noinline idSelector: (I) -> String
        ): RealmBox<I, E> {
            return RealmBox(
                Realm.getInstance(config),
                I::class.java,
                idProperty,
                mapper,
                idSelector
            )
        }
    }
}
