package hexlet.code.repository;

import hexlet.code.BaseRepository;
import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
        var sql = "SELECT * FROM urls WHERE id = ?";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var url = new Url();
                url.setId(resultSet.getInt("id"));
                url.setName(resultSet.getString("name"));
                url.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        var sql = "SELECT * FROM urls WHERE name = ?";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var url = new Url();
                url.setId(resultSet.getInt("id"));
                url.setName(resultSet.getString("name"));
                url.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public static List<Url> getAll() throws SQLException {
        var sql = "SELECT * FROM urls ORDER BY id";
        try (var conn = dataSource.getConnection();
             var statement = conn.createStatement()) {
            var resultSet = statement.executeQuery(sql);
            var result = new ArrayList<Url>();
            while (resultSet.next()) {
                var url = new Url();
                url.setId(resultSet.getInt("id"));
                url.setName(resultSet.getString("name"));
                url.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                result.add(url);
            }
            return result;
        }
    }
}
