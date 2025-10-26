package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Url {
    private int id;
    private String name;
    private Instant createdAt;

    private Integer statusCode;
    private String title;
    private String h1;
    private String description;
    private Instant lastCheckAt;

    private List<UrlCheck> checks = new ArrayList<>();

    public Url() {
    }

    public Url(String name) {
        this.name = name;
    }

    public void addCheck(UrlCheck check) {
        this.checks.add(check);
        this.statusCode = check.getStatusCode();
        this.title = check.getTitle();
        this.h1 = check.getH1();
        this.description = check.getDescription();
        this.lastCheckAt = check.getCreatedAt();
    }

    public boolean hasBeenChecked() {
        return statusCode != null;
    }
}
