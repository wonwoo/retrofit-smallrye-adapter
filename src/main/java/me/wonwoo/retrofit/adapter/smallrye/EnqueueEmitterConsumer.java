package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.subscription.MultiEmitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.function.Consumer;

final class EnqueueEmitterConsumer<T> implements Consumer<MultiEmitter<? super Response<T>>> {

    private final Call<T> originalCall;

    EnqueueEmitterConsumer(Call<T> originalCall) {
        this.originalCall = originalCall;
    }

    @Override
    public void accept(MultiEmitter<? super Response<T>> multiEmitter) {
        Call<T> call = originalCall.clone();
        CallCallback<T> callback = new CallCallback<>(call, multiEmitter);
        multiEmitter.onTermination(callback);
        if (!callback.isDisposed()) {
            call.enqueue(callback);
        }
    }


    private static final class CallCallback<T> implements Callback<T>, Runnable {
        private final Call<?> call;
        private final MultiEmitter<? super Response<T>> observer;
        private volatile boolean disposed;
        boolean terminated = false;

        CallCallback(Call<?> call, MultiEmitter<? super Response<T>> observer) {
            this.call = call;
            this.observer = observer;
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (disposed) return;

            try {
                observer.emit(response);
                observer.complete();
                if (!disposed) {
                    terminated = true;
                }
            } catch (Throwable t) {
                if (terminated) {
                    throw new IllegalArgumentException(t);
                } else if (!disposed) {
                    try {
                        observer.fail(t);
                    } catch (Throwable inner) {
                        throw new IllegalArgumentException(inner);
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (call.isCanceled()) return;

            try {
                observer.fail(t);
            } catch (Throwable inner) {
                throw new IllegalArgumentException(inner);
            }
        }

        public boolean isDisposed() {
            return disposed || call.isCanceled();
        }

        @Override
        public void run() {
            disposed = true;
            call.cancel();
        }
    }
}
