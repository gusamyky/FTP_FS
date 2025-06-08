package ftp.gusamyky.server.service.impl;

import ftp.gusamyky.server.common.model.ServerFileModel;
import ftp.gusamyky.server.common.repository.IFileRepository;
import ftp.gusamyky.server.common.service.IFileService;
import java.util.List;

public class FileServiceImpl implements IFileService {
    private final IFileRepository fileRepository;

    public FileServiceImpl(IFileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public ServerFileModel getFileById(int id) {
        return fileRepository.findById(id);
    }

    @Override
    public List<ServerFileModel> listFilesByOwner(int ownerId) {
        return fileRepository.findByOwnerId(ownerId);
    }

    @Override
    public boolean deleteFile(int id) {
        ServerFileModel file = fileRepository.findById(id);
        if (file == null)
            return false;
        fileRepository.delete(id);
        return true;
    }
}