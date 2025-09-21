package hexlet.repository;

import hexlet.BaseRepository;
import hexlet.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlCheckRepository extends BaseRepository {

    public static void save(UrlCheck urlCheck) throws SQLException {
        var sql = "INSERT INTO url_checks (url_id, status_code, title, h1, description, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, urlCheck.getUrlId());
            preparedStatement.setInt(2, urlCheck.getStatusCode());
            preparedStatement.setString(3, urlCheck.getTitle());
            preparedStatement.setString(4, urlCheck.getH1());
            preparedStatement.setString(5, urlCheck.getDescription());
            preparedStatement.setTimestamp(6, new Timestamp(urlCheck.getCreatedAt().getTime()));
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Failed to save URL check, no ID obtained.");
            }
        }
    }

    public static Optional<UrlCheck> findById(int id) throws SQLException {
        var sql = "SELECT * FROM url_checks WHERE id = ?";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var urlCheck = new UrlCheck();
                urlCheck.setId(resultSet.getInt("id"));
                urlCheck.setUrlId(resultSet.getInt("url_id"));
                urlCheck.setStatusCode(resultSet.getInt("status_code"));
                urlCheck.setTitle(resultSet.getString("title"));
                urlCheck.setH1(resultSet.getString("h1"));
                urlCheck.setDescription(resultSet.getString("description"));
                urlCheck.setCreatedAt(resultSet.getTimestamp("created_at"));
                return Optional.of(urlCheck);
            }
            return Optional.empty();
        }
    }

    public static List<UrlCheck> findByUrlId(int urlId) throws SQLException {
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY id DESC";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, urlId);
            var resultSet = preparedStatement.executeQuery();
            var result = new ArrayList<UrlCheck>();
            while (resultSet.next()) {
                var urlCheck = new UrlCheck();
                urlCheck.setId(resultSet.getInt("id"));
                urlCheck.setUrlId(resultSet.getInt("url_id"));
                urlCheck.setStatusCode(resultSet.getInt("status_code"));
                urlCheck.setTitle(resultSet.getString("title"));
                urlCheck.setH1(resultSet.getString("h1"));
                urlCheck.setDescription(resultSet.getString("description"));
                urlCheck.setCreatedAt(resultSet.getTimestamp("created_at"));
                result.add(urlCheck);
            }
            return result;
        }
    }

    public static Optional<UrlCheck> findLatestByUrlId(int urlId) throws SQLException {
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY id DESC LIMIT 1";
        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, urlId);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var urlCheck = new UrlCheck();
                urlCheck.setId(resultSet.getInt("id"));
                urlCheck.setUrlId(resultSet.getInt("url_id"));
                urlCheck.setStatusCode(resultSet.getInt("status_code"));
                urlCheck.setTitle(resultSet.getString("title"));
                urlCheck.setH1(resultSet.getString("h1"));
                urlCheck.setDescription(resultSet.getString("description"));
                urlCheck.setCreatedAt(resultSet.getTimestamp("created_at"));
                return Optional.of(urlCheck);
            }
            return Optional.empty();
        }
    }
}
