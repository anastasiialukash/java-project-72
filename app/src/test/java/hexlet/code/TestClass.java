package hexlet.code;

import hexlet.model.Url;
import hexlet.repository.UrlRepository;
import io.javalin.Javalin;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestClass {
    private static Javalin app;
    private static String baseUrl;
    private static OkHttpClient client;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        client = new OkHttpClient.Builder()
                .followRedirects(false)
                .build();
    }

    @AfterAll
    public static void afterAll() {
        if (app != null) {
            app.stop();
        }
    }

    @BeforeEach
    public void beforeEach() throws SQLException {
        try (var conn = UrlRepository.dataSource.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("DELETE FROM urls");
        }
    }

    @Test
    public void testMainPage() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("Check website"));
        }
    }

    @Test
    public void testAddUrl() throws IOException, SQLException {
        FormBody formBody = new FormBody.Builder()
                .add("url", "https://example.com/path/to/resource")
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/urls")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(302, response.code()); // Redirect status code
        }

        List<Url> urls = UrlRepository.getAll();
        assertEquals(1, urls.size());
        assertEquals("https://example.com", urls.get(0).getName());
    }

    @Test
    public void testAddInvalidUrl() throws IOException, SQLException {
        FormBody formBody = new FormBody.Builder()
                .add("url", "invalid-url")
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/urls")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(302, response.code()); // Redirect status code
        }

        List<Url> urls = UrlRepository.getAll();
        assertEquals(0, urls.size()); // No URL should be added
    }

    @Test
    public void testUrlsPage() throws IOException, SQLException {
        // Add a URL to the database
        var url = new Url();
        url.setName("https://example.com");
        url.setCreatedAt(new java.util.Date());
        UrlRepository.save(url);

        Request request = new Request.Builder()
                .url(baseUrl + "/urls")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("https://example.com"));
        }
    }

    @Test
    public void testSpecificUrlPage() throws IOException, SQLException {
        var url = new Url();
        url.setName("https://example.com");
        url.setCreatedAt(new java.util.Date());
        UrlRepository.save(url);

        Request request = new Request.Builder()
                .url(baseUrl + "/urls/" + url.getId())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            String body = response.body().string();
            assertTrue(body.contains("https://example.com"));
        }
    }
}
