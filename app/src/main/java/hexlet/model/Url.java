package hexlet.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Url {
    private int id;
    private String name;
    private Date createdAt;
}
