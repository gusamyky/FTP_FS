package ftp.gusamyky.server.common.repository;

import ftp.gusamyky.server.common.model.OperationHistoryModel;
import java.util.List;

public interface IHistoryRepository {
    List<OperationHistoryModel> findByClientId(int clientId);

    void save(OperationHistoryModel operation);
}