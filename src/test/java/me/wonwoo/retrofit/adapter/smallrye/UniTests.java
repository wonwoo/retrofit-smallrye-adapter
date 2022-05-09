package me.wonwoo.retrofit.adapter.smallrye;

import io.smallrye.mutiny.Uni;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class UniTests {

    interface Service {
        @GET("/")
        Uni<String> body();

        @GET("/")
        Uni<String> response();
    }

    private Service service;

    @BeforeEach
    void setUp() {
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(server.url("/"))
                        .addConverterFactory(new StringConverterFactory())
                        .addCallAdapterFactory(UniCallAdapterFactory.create())
                        .build();
        service = retrofit.create(Service.class);
    }

    @Test
    void responseSuccess200() {
        server.enqueue(new MockResponse().setBody("Hi"));
        assertThat(service.response().await().indefinitely()).isEqualTo("Hi");
    }

    @Test
    void bodySuccess200() {
        server.enqueue(new MockResponse().setBody("Hi"));
        assertThat(service.body().await().indefinitely()).isEqualTo("Hi");
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
