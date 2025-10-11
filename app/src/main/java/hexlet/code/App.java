package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.UrlController;
import io.javalin.Javalin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.http.HttpStatus;
import io.javalin.rendering.template.JavalinJte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
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

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", "Internal server error", "message", e.getMessage()));
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", "Bad request", "message", e.getMessage()));
        });

        app.exception(io.javalin.http.NotFoundResponse.class, (e, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(Map.of("error", "Not found", "message", e.getMessage()));
        });

        app.get("/", UrlController::handleRootRoute);
        app.post("/urls", UrlController::handleUrlCreation);
        app.get("/urls", UrlController::handleUrlsListing);
        app.get("/urls/{id}", UrlController::handleSingleUrlView);
        app.post("/urls/{id}/checks", UrlController::handleUrlCheck);

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
}
