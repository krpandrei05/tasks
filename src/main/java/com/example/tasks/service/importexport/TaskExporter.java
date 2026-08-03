package com.example.tasks.service.importexport;

import java.util.List;

public interface TaskExporter {
    byte[] export(List<TaskFileRow> rows);
    FileFormat supports();
}
