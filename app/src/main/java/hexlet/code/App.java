package hexlet.code;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.BaseRepository;
import hexlet.model.Url;
import hexlet.repository.UrlRepository;
import io.javalin.Javalin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.http.NotFoundResponse;
import io.javalin.rendering.template.JavalinJte;

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

    public static Javalin getApp() throws IOException {
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

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.get("/", ctx -> {
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Main");
            model.put("flash", ctx.sessionAttribute("flash"));
            ctx.sessionAttribute("flash", null);
            ctx.render("index.jte", model);
        });

        app.post("/urls", ctx -> {
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
                        ctx.sessionAttribute("flash", "The url already exists");
                        ctx.redirect("/urls/" + existingUrl.get().getId());
                        return;
                    }

                    var urlEntity = new Url();
                    urlEntity.setName(normalizedUrl);
                    urlEntity.setCreatedAt(new Date());

                    UrlRepository.save(urlEntity);

                    ctx.sessionAttribute("flash", "The url is added successfully");
                    ctx.redirect("/urls/" + urlEntity.getId());
                } catch (SQLException e) {
                    ctx.sessionAttribute("flash", "The DB error");
                    ctx.redirect("/");
                }
            } catch (URISyntaxException | IllegalArgumentException e) {
                ctx.sessionAttribute("flash", "Invalid url");
                ctx.redirect("/");
            }
        });

        app.get("/urls", ctx -> {
            try {
                var urls = UrlRepository.getAll();
                Map<String, Object> model;
                
                if (ctx.sessionAttribute("flash") != null) {
                    model = Map.of(
                            "urls", urls,
                            "flash", ctx.sessionAttribute("flash")
                    );
                } else {
                    model = Map.of(
                            "urls", urls
                    );
                }
                
                ctx.sessionAttribute("flash", null);
                ctx.render("urls/index.jte", model);
            } catch (SQLException e) {
                ctx.sessionAttribute("flash", "DB error");
                ctx.redirect("/");
            }
        });

        app.get("/urls/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                var urlOptional = UrlRepository.findById(id);

                if (urlOptional.isEmpty()) {
                    throw new NotFoundResponse("URL not found");
                }

                Map<String, Object> model = new HashMap<>();
                model.put("url", urlOptional.get());
                
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
        });

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
