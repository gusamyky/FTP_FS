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
        try {
            fileRepository.delete(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void saveFile(ServerFileModel file) {
        fileRepository.save(file);
    }

    @Override
    public List<ServerFileModel> getFilesByOwnerId(int ownerId) {
        return fileRepository.findByOwnerId(ownerId);
    }

    @Override
    public ServerFileModel getFileByName(String filename) {
        return fileRepository.findByFilename(filename);
    }
}