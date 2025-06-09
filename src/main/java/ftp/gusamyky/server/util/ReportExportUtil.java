package ftp.gusamyky.server.util;

import ftp.gusamyky.server.common.model.OperationHistoryModel;
import java.util.List;

public class ReportExportUtil {
    public static void exportToCsv(List<OperationHistoryModel> history, String path) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(path))) {
            writer.println("id,clientId,operation,timestamp");
            for (OperationHistoryModel op : history) {
                writer.printf("%d,%d,\"%s\",%s%n",
                        op.getId(),
                        op.getClientId(),
                        op.getOperation().replace("\"", "'"),
                        op.getTimestamp().toString());
            }
        } catch (Exception e) {
            System.err.println("[ReportExportUtil] Error exporting to CSV: " + e.getMessage());
        }
    }
}