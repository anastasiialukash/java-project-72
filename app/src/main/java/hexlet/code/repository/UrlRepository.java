package hexlet.code.repository;

import hexlet.code.BaseRepository;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UrlRepository extends BaseRepository {

    public static void save(Url url) throws SQLException {
        var sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, url.getName());

            Instant now = Instant.now();
            url.setCreatedAt(now);
            preparedStatement.setTimestamp(2, Timestamp.from(now));

            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Failed to save URL, no ID obtained.");
            }
        }
    }

    public static Optional<Url> findById(int id) throws SQLException {
        var sql = "SELECT u.*, c.id as check_id, c.status_code, c.title, c.h1, c.description, c.created_at"
                + " as check_created_at "
                + "FROM urls u "
                + "LEFT JOIN url_checks c ON u.id = c.url_id AND c.id = ("
                + "    SELECT MAX(id) FROM url_checks WHERE url_id = u.id"
                + ") "
                + "WHERE u.id = ?";

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                var url = new Url();
                url.setId(resultSet.getInt("id"));
                url.setName(resultSet.getString("name"));
                url.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());

                if (resultSet.getObject("check_id") != null) {
                    url.setStatusCode(resultSet.getInt("status_code"));
                    url.setTitle(resultSet.getString("title"));
                    url.setH1(resultSet.getString("h1"));
                    url.setDescription(resultSet.getString("description"));
                    url.setLastCheckAt(resultSet.getTimestamp("check_created_at").toInstant());
                }

                loadChecksForUrl(url);

                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        var sql = "SELECT u.*, c.id as check_id, c.status_code, c.title, c.h1, c.description, "
                + "c.created_at as check_created_at "
                + "FROM urls u "
                + "LEFT JOIN url_checks c ON u.id = c.url_id AND c.id = ("
                + "    SELECT MAX(id) FROM url_checks WHERE url_id = u.id"
                + ") "
                + "WHERE u.name = ?";

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                var url = new Url();
                url.setId(resultSet.getInt("id"));
                url.setName(resultSet.getString("name"));
                url.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());

                if (resultSet.getObject("check_id") != null) {
                    url.setStatusCode(resultSet.getInt("status_code"));
                    url.setTitle(resultSet.getString("title"));
                    url.setH1(resultSet.getString("h1"));
                    url.setDescription(resultSet.getString("description"));
                    url.setLastCheckAt(resultSet.getTimestamp("check_created_at").toInstant());
                }

                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public static List<Url> getAll() throws SQLException {
        var sql = "SELECT u.*, c.id as check_id, c.status_code, c.title, c.h1, c.description,"
                + " c.created_at as check_created_at "
                + "FROM urls u "
                + "LEFT JOIN url_checks c ON u.id = c.url_id AND c.id = ("
                + "    SELECT MAX(id) FROM url_checks WHERE url_id = u.id"
                + ") "
                + "ORDER BY u.id";

        try (var conn = dataSource.getConnection();
             var statement = conn.createStatement()) {
            var resultSet = statement.executeQuery(sql);
            var result = new ArrayList<Url>();

            while (resultSet.next()) {
                var url = new Url();
                url.setId(resultSet.getInt("id"));
                url.setName(resultSet.getString("name"));
                url.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());

                if (resultSet.getObject("check_id") != null) {
                    url.setStatusCode(resultSet.getInt("status_code"));
                    url.setTitle(resultSet.getString("title"));
                    url.setH1(resultSet.getString("h1"));
                    url.setDescription(resultSet.getString("description"));
                    url.setLastCheckAt(resultSet.getTimestamp("check_created_at").toInstant());
                }

                result.add(url);
            }
            return result;
        }
    }

    public static void saveCheck(Url url, UrlCheck check) throws SQLException {
        var sql = "INSERT INTO url_checks (url_id, status_code, title, h1, description, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, url.getId());
            preparedStatement.setInt(2, check.getStatusCode());
            preparedStatement.setString(3, check.getTitle());
            preparedStatement.setString(4, check.getH1());
            preparedStatement.setString(5, check.getDescription());

            Instant now = Instant.now();
            check.setCreatedAt(now);
            preparedStatement.setTimestamp(6, Timestamp.from(now));

            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                check.setId(generatedKeys.getInt(1));
                check.setUrlId(url.getId());

                url.addCheck(check);
            } else {
                throw new SQLException("Failed to save URL check, no ID obtained.");
            }
        }
    }

    private static void loadChecksForUrl(Url url) throws SQLException {
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY id DESC";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, url.getId());
            var resultSet = preparedStatement.executeQuery();

            List<UrlCheck> checks = new ArrayList<>();
            while (resultSet.next()) {
                var check = new UrlCheck();
                check.setId(resultSet.getInt("id"));
                check.setUrlId(resultSet.getInt("url_id"));
                check.setStatusCode(resultSet.getInt("status_code"));
                check.setTitle(resultSet.getString("title"));
                check.setH1(resultSet.getString("h1"));
                check.setDescription(resultSet.getString("description"));
                check.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                checks.add(check);
            }

            url.setChecks(checks);
        }
    }

    public static Map<Long, Url> findAllWithLatestChecks() throws SQLException {
        var sql = "SELECT u.id as url_id, u.name, u.created_at, "
                + "c.id as check_id, c.status_code, c.title, c.h1, c.description, c.created_at as check_created_at "
                + "FROM urls u "
                + "LEFT JOIN url_checks c ON u.id = c.url_id AND c.id = ("
                + "    SELECT MAX(id) FROM url_checks WHERE url_id = u.id"
                + ") "
                + "ORDER BY u.id";

        try (var conn = dataSource.getConnection();
             var statement = conn.createStatement()) {
            var resultSet = statement.executeQuery(sql);
            var result = new HashMap<Long, Url>();

            while (resultSet.next()) {
                var urlId = resultSet.getLong("url_id");
                var url = new Url();
                url.setId((int) urlId);
                url.setName(resultSet.getString("name"));
                url.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());

                if (resultSet.getObject("check_id") != null) {
                    url.setStatusCode(resultSet.getInt("status_code"));
                    url.setTitle(resultSet.getString("title"));
                    url.setH1(resultSet.getString("h1"));
                    url.setDescription(resultSet.getString("description"));
                    url.setLastCheckAt(resultSet.getTimestamp("check_created_at").toInstant());
                }

                result.put(urlId, url);
            }
            return result;
        }
    }
}
