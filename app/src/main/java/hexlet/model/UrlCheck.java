package hexlet.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UrlCheck {
    private int id;
    private int statusCode;
    private String title;
    private String h1;
    private String description;
    private int urlId;
    private Date createdAt;
}
