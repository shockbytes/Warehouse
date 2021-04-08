package at.shockbytes.warehouse.util

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T> T.asObservable(): Observable<T> = Observable.just(this)

fun <T> List<T>.toObservableFromIterable(): Observable<T> = Observable.fromIterable(this)

fun <T> Observable<T>.asCompletable(): Completable = Completable.fromObservable(this)

fun <T> Single<T>.asCompletable(): Completable = Completable.fromSingle(this)

fun <T> singleOf(
    action: () -> T
): Single<T> {
    return Single.fromCallable(action)
}

fun completableOf(
    subscribeOn: Scheduler = Schedulers.io(),
    action: () -> Unit
): Completable {
    return Completable
        .fromAction(action)
        .subscribeOn(subscribeOn)
}

fun completableEmitterOf(
    action: () -> Unit
): Completable {
    return Completable.create { source ->

        try {
            action()
            source.onComplete()
        } catch (e: Throwable) {
            source.onError(e)
        }
    }
}

fun Iterable<Completable>.merge(): Completable {
    return Completable.merge(this)
}

fun <T> List<Observable<T>>.merge(): Observable<T> {
    return Observable.merge(this)
}

fun <T> observableEmitterOf(
    action: () -> T
): Observable<T> {
    return Observable.create { source ->
        try {
            source.onNext(action())
        } catch (e: Throwable) {
            source.onError(e)
        }
    }
}
