package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UrlController {

    public static void handleRootRoute(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Main");
        model.put("flash", ctx.sessionAttribute("flash"));
        ctx.sessionAttribute("flash", null);
        ctx.render("index.jte", model);
    }

    public static void handleUrlCreation(Context ctx) throws SQLException {
        var inputUrl = ctx.formParam("url");
        URI parsedUrl;
        try {
            parsedUrl = new URI(inputUrl);
            if (parsedUrl.getScheme() == null || parsedUrl.getHost() == null) {
                throw new URISyntaxException(inputUrl, "Missing scheme or host");
            }
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String normalizedUrl = String
            .format(
                "%s://%s%s",
                parsedUrl.getScheme(),
                parsedUrl.getHost(),
                parsedUrl.getPort() == -1 ? "" : ":" + parsedUrl.getPort()
            )
            .toLowerCase();

        Url url = UrlRepository.findByName(normalizedUrl).orElse(null);

        if (url != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls/" + url.getId());
        } else {
            Url newUrl = new Url(normalizedUrl);
            UrlRepository.save(newUrl);
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
            ctx.redirect("/urls/" + newUrl.getId());
        }
    }

    public static void handleUrlsListing(Context ctx) throws SQLException {
        var urls = UrlRepository.getAll();
        Map<String, Object> model = new HashMap<>();
        model.put("urls", urls);

        if (ctx.sessionAttribute("flash") != null) {
            model.put("flash", ctx.sessionAttribute("flash"));
        }

        ctx.sessionAttribute("flash", null);
        ctx.render("urls/index.jte", model);
    }

    public static void handleSingleUrlView(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));

        var urlEntity = UrlRepository.findById(id)
                .orElseThrow(() -> new NotFoundResponse("URL not found"));

        Map<String, Object> model = new HashMap<>();
        model.put("url", urlEntity);

        if (ctx.sessionAttribute("flash") != null) {
            model.put("flash", ctx.sessionAttribute("flash"));
        }

        ctx.sessionAttribute("flash", null);
        ctx.render("urls/show.jte", model);
    }

    public static void handleUrlCheck(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var urlEntity = UrlRepository.findById(id)
                .orElseThrow(() -> new NotFoundResponse("URL not found"));

        try {
            HttpResponse<String> response = Unirest.get(urlEntity.getName()).asString();
            processCheckResponse(response, urlEntity);
            ctx.sessionAttribute("flash", "Страница успешно проверена");
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] URL check failed: " + e.getMessage());
            String errorMessage = "Failed to check the page";
            System.out.println("[DEBUG_LOG] Setting flash message: " + errorMessage);
            ctx.sessionAttribute("flash", errorMessage);
        }

        ctx.redirect("/urls/" + id);
    }

    private static void processCheckResponse(HttpResponse<String> response, Url urlEntity) throws SQLException {
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

        var urlCheck = new UrlCheck(statusCode, title, h1, description);
        UrlRepository.saveCheck(urlEntity, urlCheck);
    }
}
