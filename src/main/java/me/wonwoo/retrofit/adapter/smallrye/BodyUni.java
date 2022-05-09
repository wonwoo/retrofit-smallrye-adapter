package me.wonwoo.retrofit.adapter.smallrye;


import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.AbstractUni;
import io.smallrye.mutiny.subscription.UniSubscriber;
import io.smallrye.mutiny.subscription.UniSubscription;
import retrofit2.HttpException;
import retrofit2.Response;

public class BodyUni<T> extends AbstractUni<T> {

    private final Uni<Response<T>> upstream;

    BodyUni(Uni<Response<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    public void subscribe(UniSubscriber<? super T> actual) {
        ResponseSubscriber<T> responseSubscriber = new ResponseSubscriber<>(actual);
        upstream.subscribe().withSubscriber(responseSubscriber);
    }

    private static class ResponseSubscriber<R> implements UniSubscriber<Response<R>> {

        private final UniSubscriber<? super R> subscriber;
        private boolean terminated;

        ResponseSubscriber(UniSubscriber<? super R> observer) {
            this.subscriber = observer;
        }

        @Override
        public void onSubscribe(UniSubscription subscription) {
            subscriber.onSubscribe(subscription);
        }

        @Override
        public void onItem(Response<R> response) {
            if (response.isSuccessful()) {

                if (response.body() != null) {
                    subscriber.onItem(response.body());
                }

            } else {
                terminated = true;
                Throwable t = new HttpException(response);
                try {
                    subscriber.onFailure(t);
                } catch (Throwable inner) {
                    throw new IllegalArgumentException(inner);
                }
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            if (!terminated) {
                subscriber.onFailure(throwable);
            } else {
                Throwable broken = new AssertionError("This should never happen! Report as a bug with the full stacktrace.");
                throw new IllegalArgumentException(broken);
            }
        }
    }
}
