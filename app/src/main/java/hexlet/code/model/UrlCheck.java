package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UrlCheck {
    private int id;
    private int statusCode;
    private String title;
    private String h1;
    private String description;
    private int urlId;
    private Instant createdAt;

    public UrlCheck() {
    }

    public UrlCheck(int statusCode, String title, String h1, String description) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
    }
}
