package by.abram.astore.dto;

import java.time.LocalDateTime;

public record AsyncTaskStatusResponse(
        String taskId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String message,
        AsyncTaskReportSummary reportSummary
) {
}
