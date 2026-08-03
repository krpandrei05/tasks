package com.example.tasks.service.importexport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelTaskImporter implements TaskImporter {

    @Override
    public List<TaskFileRow> parse(byte[] fileContent) {
        List<TaskFileRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileContent))) {
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, Integer> columnIndex = readHeader(sheet);

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                rows.add(toTaskFileRow(row, columnIndex));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return rows;
    }

    private Map<String, Integer> readHeader(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new RuntimeException("Excel file is missing a header row");
        }

        Map<String, Integer> columnIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            columnIndex.put(cell.getStringCellValue(), cell.getColumnIndex());
        }
        return columnIndex;
    }

    private TaskFileRow toTaskFileRow(Row row, Map<String, Integer> columnIndex) {
        return TaskFileRow.builder()
                .taskName(readString(row, columnIndex, "taskName"))
                .statusName(readString(row, columnIndex, "statusName"))
                .username(readString(row, columnIndex, "username"))
                .dueDate(readDate(row, columnIndex, "dueDate"))
                .createdBy(readOptionalString(row, columnIndex, "createdBy"))
                .creationDate(readDate(row, columnIndex, "creationDate"))
                .build();
    }

    private Cell getCell(Row row, Map<String, Integer> columnIndex, String column) {
        Integer index = columnIndex.get(column);
        if (index == null) {
            throw new RuntimeException("Missing column '" + column + "' in Excel header");
        }
        return row.getCell(index);
    }

    private String readString(Row row, Map<String, Integer> columnIndex, String column) {
        Cell cell = getCell(row, columnIndex, column);
        if (cell == null || cell.getCellType() != CellType.STRING || cell.getStringCellValue().isBlank()) {
            throw new RuntimeException(
                    "Missing value for '" + column + "' at row " + (row.getRowNum() + 1));
        }
        return cell.getStringCellValue();
    }

    private String readOptionalString(Row row, Map<String, Integer> columnIndex, String column) {
        Cell cell = getCell(row, columnIndex, column);
        if (cell == null || cell.getCellType() != CellType.STRING) {
            return "";
        }
        return cell.getStringCellValue();
    }

    private LocalDateTime readDate(Row row, Map<String, Integer> columnIndex, String column) {
        Cell cell = getCell(row, columnIndex, column);
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            throw new RuntimeException(
                    "Missing or invalid date for '" + column + "' at row " + (row.getRowNum() + 1));
        }
        return cell.getLocalDateTimeCellValue();
    }

    @Override
    public FileFormat supports() {
        return FileFormat.EXCEL;
    }
}