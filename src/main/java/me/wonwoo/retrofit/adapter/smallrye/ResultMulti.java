package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.AbstractMulti;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import retrofit2.Response;

final class ResultMulti<T> extends AbstractMulti<Result<T>> {

    private final Multi<Response<T>> upstream;

    ResultMulti(Multi<Response<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    public void subscribe(Subscriber<? super Result<T>> subscriber) {
        upstream.subscribe().withSubscriber(new ResultSubscriber<>(subscriber));
    }

    private static class ResultSubscriber<R> implements Subscriber<Response<R>> {

        private final Subscriber<? super Result<R>> subscriber;

        ResultSubscriber(Subscriber<? super Result<R>> observer) {
            this.subscriber = observer;
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            subscriber.onSubscribe(subscription);
        }

        @Override
        public void onNext(Response<R> response) {
            subscriber.onNext(Result.response(response));
        }

        @Override
        public void onError(Throwable throwable) {
            try {
                subscriber.onNext(Result.error(throwable));
            } catch (Throwable t) {
                try {
                    subscriber.onError(t);
                } catch (Throwable inner) {
                    throw new IllegalArgumentException(inner);
                }
            }
        }
    }
}
