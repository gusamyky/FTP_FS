package ftp.gusamyky.server.database_handler.repository;

import ftp.gusamyky.server.common.model.ServerFileModel;
import ftp.gusamyky.server.common.repository.IFileRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileRepositoryImpl implements IFileRepository {
    private final ftp.gusamyky.server.config.DatabaseConfig config;

    public FileRepositoryImpl(ftp.gusamyky.server.config.DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public ServerFileModel findById(int id) {
        File dir = new File(config.getFilesDirectory());
        File[] files = dir.listFiles();
        if (files == null)
            return null;
        for (File f : files) {
            if (f.isFile() && f.getName().hashCode() == id) {
                try {
                    return new ServerFileModel(
                            id,
                            f.getName(),
                            f.length(),
                            0,
                            LocalDateTime.ofInstant(
                                    Files.exists(f.toPath()) ? Files.getLastModifiedTime(f.toPath()).toInstant()
                                            : java.time.Instant.now(),
                                    java.time.ZoneId.systemDefault()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public List<ServerFileModel> findByOwnerId(int ownerId) {
        List<ServerFileModel> result = new ArrayList<>();
        File dir = new File(config.getFilesDirectory());
        File[] files = dir.listFiles();
        if (files == null)
            return result;
        for (File f : files) {
            if (f.isFile()) {
                try {
                    result.add(new ServerFileModel(
                            f.getName().hashCode(),
                            f.getName(),
                            f.length(),
                            0,
                            LocalDateTime.ofInstant(
                                    Files.exists(f.toPath()) ? Files.getLastModifiedTime(f.toPath()).toInstant()
                                            : java.time.Instant.now(),
                                    java.time.ZoneId.systemDefault())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public void save(ServerFileModel file) {
        Path path = Paths.get(config.getFilesDirectory(), file.getFilename());
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(int id) {
        File dir = new File(config.getFilesDirectory());
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (File f : files) {
            if (f.isFile() && f.getName().hashCode() == id) {
                f.delete();
                break;
            }
        }
    }
}