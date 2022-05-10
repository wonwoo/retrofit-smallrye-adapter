package me.wonwoo.retrofit.adapter.smallrye;

import com.google.gson.reflect.TypeToken;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SmallryeCallAdapterFactoryTest {

    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];

    private final CallAdapter.Factory factory = MultiCallAdapterFactory.create();
    private Retrofit retrofit;

    @BeforeEach
    public void setUp() {
        retrofit =
                new Retrofit.Builder()
                        .baseUrl("http://localhost:1")
                        .addConverterFactory(new StringConverterFactory())
                        .addCallAdapterFactory(factory)
                        .build();
    }

    @Test
    public void nonRxJavaTypeReturnsNull() {
        CallAdapter<?, ?> adapter = factory.get(String.class, NO_ANNOTATIONS, retrofit);
        assertThat(adapter).isNull();
    }

    @Test
    public void responseTypes() {
        Type oBodyClass = new TypeToken<Uni<String>>() {
        }.getType();
        assertThat(factory.get(oBodyClass, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(String.class);


        Type oBodyWildcard = new TypeToken<Uni<? extends String>>() {
        }.getType();
        assertThat(factory.get(oBodyWildcard, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(String.class);

        Type oBodyGeneric = new TypeToken<Uni<List<String>>>() {
        }.getType();
        assertThat(factory.get(oBodyGeneric, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(new TypeToken<List<String>>() {
                }.getType());

        Type oResponseClass = new TypeToken<Uni<Response<String>>>() {
        }.getType();
        assertThat(factory.get(oResponseClass, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(String.class);

        Type oResponseWildcard = new TypeToken<Uni<Response<? extends String>>>() {
        }.getType();
        assertThat(factory.get(oResponseWildcard, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(String.class);


        Type oResultClass = new TypeToken<Uni<Result<String>>>() {
        }.getType();
        assertThat(factory.get(oResultClass, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(String.class);


        Type oResultWildcard = new TypeToken<Uni<Result<? extends String>>>() {
        }.getType();
        assertThat(factory.get(oResultWildcard, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(String.class);
    }

    @Test
    public void rawBodyTypeThrows() {
        Type observableType = new TypeToken<Uni>() {
        }.getType();
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> factory.get(observableType, NO_ANNOTATIONS, retrofit))
                .withMessage("Uni return type must be parameterized as Uni <Foo> or Uni <? extends Foo>");

    }

    @Test
    public void rawResponseTypeThrows() {
        Type observableType = new TypeToken<Uni<Response>>() {
        }.getType();
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> factory.get(observableType, NO_ANNOTATIONS, retrofit))
                .withMessage("Response must be parameterized as Response<Foo> or Response<? extends Foo>");
    }

    @Test
    public void rawResultTypeThrows() {
        Type observableType = new TypeToken<Uni<Result>>() {
        }.getType();
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> factory.get(observableType, NO_ANNOTATIONS, retrofit))
                .withMessage("Result must be parameterized as Result<Foo> or Result<? extends Foo>");
    }
}