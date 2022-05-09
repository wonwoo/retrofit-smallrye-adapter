package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.Uni;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class UinJsonTests {

    public static class Contributor {
        public final String login;
        public final int contributions;

        public Contributor(String login, int contributions) {
            this.login = login;
            this.contributions = contributions;
        }
    }

    interface Service {
        @GET("/")
        Uni<Contributor> body();

        @GET("/")
        Uni<Response<Contributor>> response();

        @GET("/")
        Uni<Result<Contributor>> result();
    }

    private Service service;

    @BeforeEach
    void setUp() {
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(server.url("/"))
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(UniCallAdapterFactory.create())
                        .build();
        service = retrofit.create(Service.class);
    }

    @Test
    void bodySuccess200() {
        server.enqueue(new MockResponse().setBody("{\"login\" : \"wonwoo\", \"contributions\": 1}"));
        Contributor contributor = service.body().await().indefinitely();
        assertThat(contributor.login).isEqualTo("wonwoo");
        assertThat(contributor.contributions).isEqualTo(1);
    }

    @Test
    void responseSuccess200() {
        server.enqueue(new MockResponse().setBody("{\"login\" : \"wonwoo\", \"contributions\": 1}"));
        Response<Contributor> response = service.response().await().indefinitely();
        Contributor contributor = response.body();
        assertThat(response.code()).isEqualTo(200);
        assertThat(contributor.login).isEqualTo("wonwoo");
        assertThat(contributor.contributions).isEqualTo(1);
    }

    @Test
    void resultSuccess200() {
        server.enqueue(new MockResponse().setBody("{\"login\" : \"wonwoo\", \"contributions\": 1}"));
        Result<Contributor> result = service.result().await().indefinitely();
        Response<Contributor> response = result.response();
        Contributor contributor = response.body();
        assertThat(result.isError()).isFalse();
        assertThat(response.code()).isEqualTo(200);
        assertThat(contributor.login).isEqualTo("wonwoo");
        assertThat(contributor.contributions).isEqualTo(1);
    }

    public static MockWebServer server;

    @BeforeAll
    static void start() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }
}
