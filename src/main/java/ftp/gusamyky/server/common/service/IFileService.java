package ftp.gusamyky.server.common.service;

import ftp.gusamyky.server.common.model.ServerFileModel;
import java.util.List;

public interface IFileService {
    ServerFileModel getFileById(int id);

    List<ServerFileModel> listFilesByOwner(int ownerId);

    boolean deleteFile(int id);
}