package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.Multi;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;

import java.lang.reflect.Type;

public class MultiCallAdapter<R> implements CallAdapter<R, Object> {

    private final Type responseType;
    private final boolean isResult;
    private final boolean isBody;
    private final boolean isUni;

    MultiCallAdapter(Type responseType, boolean isResult, boolean isBody, boolean isUni) {
        this.responseType = responseType;
        this.isBody = isBody;
        this.isResult = isResult;
        this.isUni = isUni;
    }

    @Override
    public Type responseType() {
        return this.responseType;
    }

    @Override
    public Object adapt(Call<R> call) {
        Multi<Response<R>> emitter = Multi.createFrom().emitter(new EnqueueEmitterConsumer<>(call));

        Multi<?> uni;
        if (isResult) {
            uni = new ResultMulti<>(emitter);
        } else if (isBody) {
            uni = new BodyMulti<>(emitter);
        } else {
            uni = emitter;
        }

        if (isUni) {
            return uni.toUni();
        }

        return uni;
    }
}
