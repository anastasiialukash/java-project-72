package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Url {
    private int id;
    private String name;
    private Instant createdAt;
}
