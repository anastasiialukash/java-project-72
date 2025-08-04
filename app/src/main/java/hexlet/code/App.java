package hexlet.code;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        getApp().start(getPort());
    }

    public static Javalin getApp() {
        return Javalin.create(config -> config.bundledPlugins.enableDevLogging())
                .get("/", ctx -> ctx.result("Hello World"));
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        return Integer.parseInt(port);
    }
}
