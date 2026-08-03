package com.example.tasks.service.importexport;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ExcelTaskExporter implements TaskExporter {
    private static final String[] HEADERS = {
            "taskName", "statusName", "username", "dueDate", "createdBy", "creationDate"
    };

    @Override
    public byte[] export(List<TaskFileRow> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tasks");
            CellStyle dateStyle = createDateStyle(workbook);

            writeHeader(sheet);

            for (int i = 0; i < rows.size(); i++) {
                writeRow(sheet, i + 1, rows.get(i), dateStyle);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return dateStyle;
    }

    private void writeHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < HEADERS.length; col++) {
            headerRow.createCell(col).setCellValue(HEADERS[col]);
        }
    }

    private void writeRow(Sheet sheet, int rowIndex, TaskFileRow rowData, CellStyle dateStyle) {
        Row row = sheet.createRow(rowIndex);

        row.createCell(0).setCellValue(rowData.getTaskName());
        row.createCell(1).setCellValue(rowData.getStatusName());
        row.createCell(2).setCellValue(rowData.getUsername());

        Cell dueDaateCell = row.createCell(3);
        if (rowData.getCreationDate() != null) {
            dueDaateCell.setCellValue(rowData.getDueDate());
            dueDaateCell.setCellStyle(dateStyle);
        }

        row.createCell(4).setCellValue(rowData.getCreatedBy() != null ? rowData.getCreatedBy() : "");

        Cell creationDateCell = row.createCell(5);
        if (rowData.getCreationDate() != null) {
            creationDateCell.setCellValue(rowData.getCreationDate());
            creationDateCell.setCellStyle(dateStyle);
        }
    }

    @Override
    public FileFormat supports() {
        return FileFormat.EXCEL;
    }
}
