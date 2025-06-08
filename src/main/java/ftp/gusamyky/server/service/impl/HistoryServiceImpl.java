package ftp.gusamyky.server.service.impl;

import ftp.gusamyky.server.common.model.OperationHistoryModel;
import ftp.gusamyky.server.common.repository.IHistoryRepository;
import ftp.gusamyky.server.common.service.IHistoryService;
import java.util.List;

public class HistoryServiceImpl implements IHistoryService {
    private final IHistoryRepository historyRepository;

    public HistoryServiceImpl(IHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<OperationHistoryModel> getHistoryByClientId(int clientId) {
        return historyRepository.findByClientId(clientId);
    }

    @Override
    public void addOperation(OperationHistoryModel operation) {
        historyRepository.save(operation);
    }
}