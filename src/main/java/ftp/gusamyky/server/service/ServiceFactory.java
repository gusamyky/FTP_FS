package ftp.gusamyky.server.service;

import ftp.gusamyky.server.common.repository.IClientRepository;
import ftp.gusamyky.server.common.repository.IFileRepository;
import ftp.gusamyky.server.common.repository.IHistoryRepository;
import ftp.gusamyky.server.common.service.IUserService;
import ftp.gusamyky.server.common.service.IFileService;
import ftp.gusamyky.server.common.service.IHistoryService;
import ftp.gusamyky.server.common.repository.impl.ClientRepositoryImpl;
import ftp.gusamyky.server.common.repository.impl.FileRepositoryImpl;
import ftp.gusamyky.server.common.repository.impl.HistoryRepositoryImpl;
import ftp.gusamyky.server.service.impl.UserServiceImpl;
import ftp.gusamyky.server.service.impl.FileServiceImpl;
import ftp.gusamyky.server.service.impl.HistoryServiceImpl;
import ftp.gusamyky.server.config.DatabaseConfig;
import ftp.gusamyky.server.config.ServerConfig;

/**
 * Factory class for creating service instances.
 * This class is responsible for creating and managing service instances.
 */
public class ServiceFactory {
    private final IClientRepository clientRepository;
    private final IFileRepository fileRepository;
    private final IHistoryRepository historyRepository;
    private final IUserService userService;
    private final IFileService fileService;
    private final IHistoryService historyService;
    private final ServerConfig serverConfig;

    public ServiceFactory(DatabaseConfig dbConfig, ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.clientRepository = new ClientRepositoryImpl(dbConfig);
        this.fileRepository = new FileRepositoryImpl(dbConfig);
        this.historyRepository = new HistoryRepositoryImpl(dbConfig);
        this.userService = new UserServiceImpl(clientRepository);
        this.fileService = new FileServiceImpl(fileRepository);
        this.historyService = new HistoryServiceImpl(historyRepository);
    }

    public IUserService getUserService() {
        return userService;
    }

    public IFileService getFileService() {
        return fileService;
    }

    public IHistoryService getHistoryService() {
        return historyService;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }
}