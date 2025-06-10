package ftp.gusamyky.server.common.repository;

import ftp.gusamyky.server.common.model.ServerFileModel;
import java.util.List;

public interface IFileRepository {
    ServerFileModel findById(int id);

    List<ServerFileModel> findByOwnerId(int ownerId);

    void save(ServerFileModel file);

    void delete(int id);

    ServerFileModel findByFilename(String filename);
}