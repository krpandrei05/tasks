package com.example.tasks.importexport;

import com.example.tasks.service.importexport.TaskFileRow;

import java.util.List;

public interface TaskExporter {
    byte[] export(List<TaskFileRow> rows);
    FileFormat supports();
}
