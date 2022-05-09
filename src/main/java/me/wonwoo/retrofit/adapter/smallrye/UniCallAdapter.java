package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.Uni;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;

import java.lang.reflect.Type;

public class UniCallAdapter<R> implements CallAdapter<R, Object> {

    private final Type responseType;
    private final boolean isResult;
    private final boolean isBody;

    UniCallAdapter(Type responseType, boolean isResult, boolean isBody) {
        this.responseType = responseType;
        this.isBody = isBody;
        this.isResult = isResult;
    }

    @Override
    public Type responseType() {
        return this.responseType;
    }

    @Override
    public Object adapt(Call<R> call) {
        Uni<Response<R>> emitter = Uni.createFrom().emitter(new EnqueueEmitterConsumer<>(call));

        Uni<?> uni;
        if (isResult) {
            uni = new ResultUni<>(emitter);
        } else if (isBody) {
            uni = new BodyUni<>(emitter);
        } else {
            uni = emitter;
        }

        return uni;
    }
}
