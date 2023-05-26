package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Url extends Model {
    @Id
    private long id;
    private String name;
    @WhenCreated
    private Instant createdAt;
    @OneToMany
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
        this.urlChecks = new ArrayList<>();
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public List<UrlCheck> getUrlChecks() {
        return this.urlChecks;
    }
}
