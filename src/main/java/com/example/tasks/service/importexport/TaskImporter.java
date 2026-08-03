package com.example.tasks.service.importexport;

import java.util.List;

public interface TaskImporter {
    List<TaskFileRow> parse(byte[] fileContent);
    FileFormat supports();
}
