package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

    public static void handleUrlCreation(Context ctx) throws MalformedURLException {
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
                // Creation date will be set in the repository

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

    public static void handleUrlsListing(Context ctx) {
        try {
            var urls = UrlRepository.getAll();
            Map<String, Object> model = new HashMap<>();
            model.put("urls", urls);

            // Use the optimized method to get all latest checks in one query
            var urlChecksMap = UrlCheckRepository.findLatestChecks();
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

    public static void handleSingleUrlView(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var urlEntity = UrlRepository.findById(id)
                    .orElseThrow(() -> new NotFoundResponse("URL not found"));

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

    public static void handleUrlCheck(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            var urlEntity = UrlRepository.findById(id)
                    .orElseThrow(() -> new NotFoundResponse("URL not found"));

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

                var urlCheck = new UrlCheck(statusCode, title, h1, description);
                urlCheck.setUrlId(urlEntity.getId());
                // Creation date will be set in the repository

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
