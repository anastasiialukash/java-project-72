package hexlet.code;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.BaseRepository;
import hexlet.model.Url;
import hexlet.model.UrlCheck;
import hexlet.repository.UrlCheckRepository;
import hexlet.repository.UrlRepository;
import io.javalin.Javalin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.http.NotFoundResponse;
import io.javalin.rendering.template.JavalinJte;
import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) throws IOException {
        getApp().start(getPort());
    }

    private static void initializeDatabase() throws IOException {
        var hikariConfig = new HikariConfig();
        String url = System.getenv("JDBC_DATABASE_URL");

        if (url == null || url.isEmpty()) {
            url = "jdbc:h2:mem:project";
            hikariConfig.setJdbcUrl(url);
        } else {
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setDriverClassName("org.postgresql.Driver");

            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setIdleTimeout(60000);
            hikariConfig.setConnectionTimeout(30000);
        }

        var dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile("schema.sql");

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        BaseRepository.dataSource = dataSource;
    }

    public static Javalin getApp() throws IOException {
        initializeDatabase();

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.get("/", App::handleRootRoute);

        app.post("/urls", App::handleUrlCreation);

        app.get("/urls", App::handleUrlsListing);

        app.get("/urls/{id}", App::handleSingleUrlView);

        app.post("/urls/{id}/checks", App::handleUrlCheck);

        return app;
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.parseInt(port);
    }

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    private static void handleRootRoute(io.javalin.http.Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Main");
        model.put("flash", ctx.sessionAttribute("flash"));
        ctx.sessionAttribute("flash", null);
        ctx.render("index.jte", model);
    }

    private static void handleUrlCreation(io.javalin.http.Context ctx) throws java.net.MalformedURLException {
        String inputUrl = ctx.formParam("url");

        try {
            URI uri = new URI(inputUrl);
            URL parsedUrl = uri.toURL();

            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost();
            int port = parsedUrl.getPort();

            String normalizedUrl = protocol + "://" + host;
            if (port != -1) {
                normalizedUrl += ":" + port;
            }

            try {
                var existingUrl = UrlRepository.findByName(normalizedUrl);
                if (existingUrl.isPresent()) {
                    ctx.sessionAttribute("flash", "Страница уже существует");
                    ctx.redirect("/urls/" + existingUrl.get().getId());
                    return;
                }

                var urlEntity = new Url();
                urlEntity.setName(normalizedUrl);
                urlEntity.setCreatedAt(new Date());

                UrlRepository.save(urlEntity);

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.redirect("/urls/" + urlEntity.getId());
            } catch (SQLException e) {
                ctx.sessionAttribute("flash", "The DB error");
                ctx.redirect("/");
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            ctx.sessionAttribute("flash", "Invalid url");
            ctx.redirect("/");
        }
    }

    private static void handleUrlsListing(io.javalin.http.Context ctx) {
        try {
            var urls = UrlRepository.getAll();
            Map<String, Object> model = new HashMap<>();
            model.put("urls", urls);

            var urlChecksMap = new HashMap<Integer, UrlCheck>();
            for (var urlEntity : urls) {
                var latestCheck = UrlCheckRepository.findLatestByUrlId(urlEntity.getId());
                latestCheck.ifPresent(check -> urlChecksMap.put(urlEntity.getId(), check));
            }
            model.put("urlChecksMap", urlChecksMap);

            if (ctx.sessionAttribute("flash") != null) {
                model.put("flash", ctx.sessionAttribute("flash"));
            }

            ctx.sessionAttribute("flash", null);
            ctx.render("urls/index.jte", model);
        } catch (SQLException e) {
            ctx.sessionAttribute("flash", "DB error");
            ctx.redirect("/");
        }
    }

    private static void handleSingleUrlView(io.javalin.http.Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var urlOptional = UrlRepository.findById(id);

            if (urlOptional.isEmpty()) {
                throw new NotFoundResponse("URL not found");
            }

            var urlEntity = urlOptional.get();
            var checks = UrlCheckRepository.findByUrlId(urlEntity.getId());

            Map<String, Object> model = new HashMap<>();
            model.put("url", urlEntity);
            model.put("checks", checks);

            if (ctx.sessionAttribute("flash") != null) {
                model.put("flash", ctx.sessionAttribute("flash"));
            }

            ctx.sessionAttribute("flash", null);
            ctx.render("urls/show.jte", model);
        } catch (SQLException e) {
            ctx.sessionAttribute("flash", "DB error");
            ctx.redirect("/");
        } catch (NumberFormatException e) {
            throw new NotFoundResponse("Invalid URL ID");
        }
    }

    private static void handleUrlCheck(io.javalin.http.Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var urlOptional = UrlRepository.findById(id);

            if (urlOptional.isEmpty()) {
                throw new NotFoundResponse("URL not found");
            }

            var urlEntity = urlOptional.get();

            try {
                HttpResponse<String> response = Unirest.get(urlEntity.getName()).asString();
                int statusCode = response.getStatus();
                String body = response.getBody();

                Document document = Jsoup.parse(body);
                String title = document.title();

                String h1 = "";
                var h1Element = document.selectFirst("h1");
                if (h1Element != null) {
                    h1 = h1Element.text();
                }

                String description = "";
                var metaDescription = document.selectFirst("meta[name=description]");
                if (metaDescription != null) {
                    description = metaDescription.attr("content");
                }

                var urlCheck = new UrlCheck();
                urlCheck.setUrlId(urlEntity.getId());
                urlCheck.setStatusCode(statusCode);
                urlCheck.setTitle(title);
                urlCheck.setH1(h1);
                urlCheck.setDescription(description);
                urlCheck.setCreatedAt(new Date());

                UrlCheckRepository.save(urlCheck);

                ctx.sessionAttribute("flash", "Страница успешно проверена");
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] URL check failed: " + e.getMessage());
                String errorMessage = "Failed to check the page";
                System.out.println("[DEBUG_LOG] Setting flash message: " + errorMessage);
                ctx.sessionAttribute("flash", errorMessage);
            }

            ctx.redirect("/urls/" + id);
        } catch (SQLException e) {
            ctx.sessionAttribute("flash", "DB error");
            ctx.redirect("/");
        } catch (NumberFormatException e) {
            throw new NotFoundResponse("Invalid URL ID");
        }
    }
}
