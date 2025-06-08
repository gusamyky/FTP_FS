package ftp.gusamyky.server.common.service;

import ftp.gusamyky.server.common.model.OperationHistoryModel;
import java.util.List;

public interface IHistoryService {
    List<OperationHistoryModel> getHistoryByClientId(int clientId);

    void addOperation(OperationHistoryModel operation);
}