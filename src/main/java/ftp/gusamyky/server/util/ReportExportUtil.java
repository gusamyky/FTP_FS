package ftp.gusamyky.server.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import ftp.gusamyky.server.common.model.OperationHistoryModel;

public class ReportExportUtil {
    private static final Logger LOGGER = Logger.getLogger(ReportExportUtil.class.getName());
    private static final String CSV_HEADER = "ID,Client ID,Operation,Timestamp";

    public static void exportToCsv(List<OperationHistoryModel> history, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write(CSV_HEADER + "\n");

            // Write data
            for (OperationHistoryModel record : history) {
                writer.write(String.format("%d,%d,%s,%s\n",
                        record.getId(),
                        record.getClientId(),
                        record.getOperation(),
                        record.getTimestamp()));
            }

            LOGGER.info("Successfully exported report to: " + filePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export report to CSV: " + filePath, e);
            throw new IllegalStateException("Failed to export report to CSV", e);
        }
    }
}