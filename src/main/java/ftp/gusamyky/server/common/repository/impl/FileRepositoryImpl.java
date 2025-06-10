package ftp.gusamyky.server.common.repository.impl;

import ftp.gusamyky.server.common.model.ServerFileModel;
import ftp.gusamyky.server.common.repository.IFileRepository;
import ftp.gusamyky.server.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FileRepositoryImpl implements IFileRepository {
    private static final Logger LOGGER = Logger.getLogger(FileRepositoryImpl.class.getName());
    private final DatabaseConfig config;

    public FileRepositoryImpl(DatabaseConfig config) {
        this.config = config;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    @Override
    public List<ServerFileModel> findByOwnerId(int ownerId) {
        String sql = "SELECT id, filename, size, owner_id, created_at FROM Pliki WHERE owner_id = ?";
        List<ServerFileModel> files = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(new ServerFileModel(
                            rs.getInt("id"),
                            rs.getString("filename"),
                            rs.getLong("size"),
                            rs.getInt("owner_id"),
                            rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding files for owner ID: " + ownerId, e);
        }
        return files;
    }

    @Override
    public ServerFileModel findById(int id) {
        String sql = "SELECT id, filename, size, owner_id, created_at FROM Pliki WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ServerFileModel(
                            rs.getInt("id"),
                            rs.getString("filename"),
                            rs.getLong("size"),
                            rs.getInt("owner_id"),
                            rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding file by ID: " + id, e);
        }
        return null;
    }

    @Override
    public void save(ServerFileModel file) {
        if (file == null) {
            LOGGER.warning("Attempted to save null file");
            return;
        }

        String sql = file.getId() == 0
                ? "INSERT INTO Pliki (filename, size, owner_id, created_at) VALUES (?, ?, ?, ?)"
                : "UPDATE Pliki SET filename = ?, size = ?, owner_id = ?, created_at = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, file.getFilename());
            stmt.setLong(2, file.getSize());
            stmt.setInt(3, file.getOwnerId());
            stmt.setObject(4, file.getCreatedAt());
            if (file.getId() != 0) {
                stmt.setInt(5, file.getId());
            }
            stmt.executeUpdate();
            LOGGER.info("Successfully saved file: " + file.getFilename());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving file: " + file.getFilename(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Pliki WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                LOGGER.info("Successfully deleted file with ID: " + id);
            } else {
                LOGGER.warning("No file found with ID: " + id);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting file with ID: " + id, e);
        }
    }

    @Override
    public ServerFileModel findByFilename(String filename) {
        String sql = "SELECT id, filename, size, owner_id, created_at FROM Pliki WHERE filename = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filename);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ServerFileModel(
                            rs.getInt("id"),
                            rs.getString("filename"),
                            rs.getLong("size"),
                            rs.getInt("owner_id"),
                            rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding file by name: " + filename, e);
        }
        return null;
    }
}