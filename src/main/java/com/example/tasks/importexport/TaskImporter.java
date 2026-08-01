package com.example.tasks.importexport;

import com.example.tasks.service.importexport.TaskFileRow;

import java.util.List;

public interface TaskImporter {
    List<TaskFileRow> parse(byte[] fileContent);
    FileFormat supports();
}
