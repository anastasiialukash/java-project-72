package hexlet.code;

import hexlet.model.Url;
import hexlet.model.UrlCheck;
import hexlet.repository.UrlCheckRepository;
import hexlet.repository.UrlRepository;
import io.javalin.Javalin;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestClass {
    private static Javalin app;
    private static String baseUrl;
    private static OkHttpClient client;
    private static MockWebServer mockWebServer;

    @BeforeAll
    static void beforeAll() throws IOException {
        System.clearProperty("JDBC_DATABASE_URL");

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        client = new OkHttpClient.Builder()
                .followRedirects(false)
                .cookieJar(new okhttp3.CookieJar() {
                    private final java.util.HashMap<String, java.util.List<okhttp3.Cookie>> cookieStore =
                            new java.util.HashMap<>();

                    @Override
                    public void saveFromResponse(okhttp3.HttpUrl url, java.util.List<okhttp3.Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public java.util.List<okhttp3.Cookie> loadForRequest(okhttp3.HttpUrl url) {
                        java.util.List<okhttp3.Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new java.util.ArrayList<>();
                    }
                })
                .build();
    }

    @AfterAll
    static void afterAll() throws IOException {
        if (app != null) {
            app.stop();
        }

        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @BeforeEach
    void beforeEach() throws SQLException {
        try (var conn = UrlRepository.dataSource.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("DELETE FROM url_checks");
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
            assertEquals(302, response.code());

            String redirectLocation = response.header("Location");
            assertNotNull(redirectLocation);
            assertTrue(redirectLocation.startsWith("/urls/"));

            String idStr = redirectLocation.substring("/urls/".length());
            int urlId = Integer.parseInt(idStr);

            List<Url> urls = UrlRepository.getAll();
            assertEquals(1, urls.size());
            assertEquals("https://example.com", urls.get(0).getName());
            assertEquals(urlId, urls.get(0).getId());

            Request getRequest = new Request.Builder()
                    .url(baseUrl + redirectLocation)
                    .build();

            try (Response getResponse = client.newCall(getRequest).execute()) {
                assertEquals(200, getResponse.code());
                String body = getResponse.body().string();
                assertTrue(body.contains("https://example.com"));
                assertTrue(body.contains("URL Details"));
            }
        }
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
            assertEquals(302, response.code());
            assertEquals("/", response.header("Location"));
            List<Url> urls = UrlRepository.getAll();
            assertEquals(0, urls.size());

            Request getRequest = new Request.Builder()
                .url(baseUrl + "/")
                .build();

            try (Response getResponse = client.newCall(getRequest).execute()) {
                assertEquals(200, getResponse.code());
                String body = getResponse.body().string();
                assertTrue(body.contains("Check website"));
            }
        }
    }

    @Test
    public void testAddDuplicateUrl() throws IOException, SQLException {
        var url = new Url();
        url.setName("https://example.com");
        url.setCreatedAt(new java.util.Date());
        UrlRepository.save(url);

        FormBody formBody = new FormBody.Builder()
                .add("url", "https://example.com")
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/urls")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(302, response.code());
            String redirectLocation = response.header("Location");
            assertEquals("/urls/" + url.getId(), redirectLocation);

            List<Url> urls = UrlRepository.getAll();
            assertEquals(1, urls.size());

            Request getRequest = new Request.Builder()
                    .url(baseUrl + redirectLocation)
                    .build();

            try (Response getResponse = client.newCall(getRequest).execute()) {
                assertEquals(200, getResponse.code());
                String body = getResponse.body().string();
                assertTrue(body.contains("URL Details"));
                assertTrue(body.contains("https://example.com"));
            }
        }
    }

    @Test
    public void testUrlsPage() throws IOException, SQLException {
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

    @Test
    public void testNonExistentUrlPage() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/urls/9999")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(404, response.code());
        }
    }

    @Test
    public void testInvalidUrlIdFormat() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/urls/invalid")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(404, response.code());
        }
    }

    @Test
    public void testUrlCheck() throws IOException, SQLException {
        var url = new Url();
        url.setName(mockWebServer.url("/").toString().replaceAll("/$", ""));
        url.setCreatedAt(new java.util.Date());
        UrlRepository.save(url);

        String mockHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Test Page</title>
                    <meta name="description" content="Test description">
                </head>
                <body>
                    <h1>Test Header</h1>
                    <p>Test paragraph</p>
                </body>
                </html>
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockHtml)
                .addHeader("Content-Type", "text/html"));

        Request request = new Request.Builder()
                .url(baseUrl + "/urls/" + url.getId() + "/checks")
                .post(new FormBody.Builder().build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(302, response.code());
            assertEquals("/urls/" + url.getId(), response.header("Location"));

            List<UrlCheck> checks = UrlCheckRepository.findByUrlId(url.getId());
            assertEquals(1, checks.size());

            UrlCheck check = checks.get(0);
            assertEquals(url.getId(), check.getUrlId());
            assertEquals(200, check.getStatusCode());
            assertEquals("Test Page", check.getTitle());
            assertEquals("Test Header", check.getH1());
            assertEquals("Test description", check.getDescription());

            Request getRequest = new Request.Builder()
                    .url(baseUrl + "/urls/" + url.getId())
                    .build();

            try (Response getResponse = client.newCall(getRequest).execute()) {
                assertEquals(200, getResponse.code());
                String body = getResponse.body().string();
                assertTrue(body.contains("Test Page"));
                assertTrue(body.contains("Test Header"));
                assertTrue(body.contains("Test description"));
                assertTrue(body.contains("200"));
            }
        }
    }

    @Test
    public void testFailedUrlCheck() throws IOException, SQLException {
        var url = new Url();
        url.setName("https://non-existent-domain-12345.com");
        url.setCreatedAt(new java.util.Date());
        UrlRepository.save(url);

        // Send request to check URL
        Request request = new Request.Builder()
                .url(baseUrl + "/urls/" + url.getId() + "/checks")
                .post(new FormBody.Builder().build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(302, response.code());
            assertEquals("/urls/" + url.getId(), response.header("Location"));

            List<UrlCheck> checks = UrlCheckRepository.findByUrlId(url.getId());
            assertEquals(0, checks.size());

            Request getRequest = new Request.Builder()
                    .url(baseUrl + "/urls/" + url.getId())
                    .build();

            try (Response getResponse = client.newCall(getRequest).execute()) {
                assertEquals(200, getResponse.code());
                String body = getResponse.body().string();
                assertTrue(body.contains("Failed to check the page"));
            }
        }
    }
}
