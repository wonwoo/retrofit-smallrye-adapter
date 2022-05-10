package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.AbstractMulti;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import retrofit2.HttpException;
import retrofit2.Response;

final class BodyMulti<T> extends AbstractMulti<T> {

    private final Multi<Response<T>> upstream;

    BodyMulti(Multi<Response<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        ResponseSubscriber<T> responseSubscriber = new ResponseSubscriber<>(subscriber);
        upstream.subscribe().withSubscriber(responseSubscriber);
    }

    private static class ResponseSubscriber<R> implements Subscriber<Response<R>> {

        private final Subscriber<? super R> subscriber;
        private boolean terminated;

        ResponseSubscriber(Subscriber<? super R> observer) {
            this.subscriber = observer;
        }

        @Override
        public void onComplete() {
            if (!terminated) {
                subscriber.onComplete();
            }
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            subscriber.onSubscribe(subscription);
        }

        @Override
        public void onNext(Response<R> response) {
            if (response.isSuccessful()) {

                if (response.body() != null) {
                    subscriber.onNext(response.body());
                }

            } else {
                terminated = true;
                Throwable t = new HttpException(response);
                try {
                    subscriber.onError(t);
                } catch (Throwable inner) {
                    throw new IllegalArgumentException(inner);
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (!terminated) {
                subscriber.onError(throwable);
            } else {
                Throwable broken = new AssertionError("This should never happen! Report as a bug with the full stacktrace.");
                throw new IllegalArgumentException(broken);
            }
        }
    }
}
