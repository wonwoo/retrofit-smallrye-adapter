package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.AbstractUni;
import io.smallrye.mutiny.subscription.UniSubscriber;
import io.smallrye.mutiny.subscription.UniSubscription;
import retrofit2.Response;

public class ResultUni<T> extends AbstractUni<Result<T>> {

    private final Uni<Response<T>> upstream;

    ResultUni(Uni<Response<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    public void subscribe(UniSubscriber<? super Result<T>> subscriber) {
        upstream.subscribe().withSubscriber(new ResultSubscriber<>(subscriber));
    }

    private static class ResultSubscriber<R> implements UniSubscriber<Response<R>> {

        private final UniSubscriber<? super Result<R>> subscriber;

        ResultSubscriber(UniSubscriber<? super Result<R>> observer) {
            this.subscriber = observer;
        }

        @Override
        public void onSubscribe(UniSubscription subscription) {
            subscriber.onSubscribe(subscription);

        }

        @Override
        public void onItem(Response<R> response) {
            subscriber.onItem(Result.response(response));
        }

        @Override
        public void onFailure(Throwable throwable) {
            try {
                subscriber.onItem(Result.error(throwable));
            } catch (Throwable t) {
                try {
                    subscriber.onFailure(t);
                } catch (Throwable inner) {
                    throw new IllegalArgumentException(inner);
                }
            }
        }
    }
}
