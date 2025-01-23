package com.baseLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelDataOperations {

    private Workbook workbook;

    // Initialize the .xlsx file
    public ExcelDataOperations(String excelFilePath) throws IOException, InvalidFormatException {
        FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath));
        workbook = new XSSFWorkbook(fileInputStream); // Supports .xlsx format
    }

    
    // Fetch data dynamically from any sheet, filtering rows where the Flag column is marked as 'X'
    public List<Map<String, String>> getTestDataWithFlag(String sheetName, String flagValue) throws IOException {
        Sheet sheet = workbook.getSheet(sheetName);
        List<Map<String, String>> data = new ArrayList<>();
        if (sheet == null) {
            System.out.println("Sheet with name " + sheetName + " does not exist.");
            return data;
        }

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            System.out.println("Header row is missing in sheet " + sheetName);
            return data;
        }

        // Get index of Flag column (it is the first column)
        int flagColumnIndex = getColumnIndex(headerRow, "Flag");
        if (flagColumnIndex == -1) {
            System.out.println("Flag column is missing in sheet " + sheetName);
            return data;
        }

        // Loop through all rows in the sheet and filter based on Flag column
        for (int i = 1; i <= sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue; // Skip empty rows
            }

            String flag = getCellData(row, flagColumnIndex);
            if (flag != null && flag.equalsIgnoreCase(flagValue)) {
                Map<String, String> rowData = new HashMap<>();
                for (Cell cell : row) {
                    String key = headerRow.getCell(cell.getColumnIndex()).getStringCellValue();
                    rowData.put(key, getCellData(cell));
                }
                data.add(rowData);
            }
        }
        return data;
    }

    // Fetch data for a specific cell
    private String getCellData(Cell cell) {
        if (cell == null) {
            return "";
        }
        return cell.toString().trim();
    }

    // Fetch data for a specific row and column index
    private String getCellData(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        return getCellData(cell);
    }

    // Helper method to get the column index of a specific column name
    private int getColumnIndex(Row headerRow, String columnName) {
        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                return cell.getColumnIndex();
            }
        }
        return -1; // Return -1 if the column is not found
    }

    // Close the workbook after usage
    public void close() throws IOException {
        workbook.close();
    }
    
    
//    // Fetch data dynamically from any sheet, handling nulls and missing values,
//    // with a specified range of rows and columns
//    public List<Map<String, String>> getTestDataInRange(String sheetName, int startRow, int endRow, int startCol,
//            int endCol) throws IOException {
//        Sheet sheet = workbook.getSheet(sheetName);
//        List<Map<String, String>> data = new ArrayList<>();
//        if (sheet == null) {
//            System.out.println("Sheet with name " + sheetName + " does not exist.");
//            return data;
//        }
//
//        Row headerRow = sheet.getRow(0);
//        if (headerRow == null) {
//            System.out.println("Header row is missing in sheet " + sheetName);
//            return data;
//        }
//
//        // Loop through the specified row range
//        for (int i = startRow; i <= endRow; i++) {
//            Row row = sheet.getRow(i);
//            if (row == null) {
//                continue; // Skip if the row is null
//            }
//
//            Map<String, String> rowData = new HashMap<>();
//            boolean rowIsValid = true; // Flag to track if a row is valid
//
//            // Loop through the specified column range
//            for (int j = startCol; j <= endCol; j++) {
//                String key = headerRow.getCell(j).getStringCellValue();
//                Cell cell = row.getCell(j);
//
//                if (cell == null || cell.toString().trim().isEmpty()) {
//                    // If the cell is null or empty, mark it as invalid
//                    rowData.put(key, "Cell is empty or missing.");
//                    rowIsValid = false; // Flag the row as invalid
//                } else {
//                    // Fetch the cell data and store it
//                    rowData.put(key, cell.toString().trim());
//                }
//            }
//
//            if (rowIsValid) {
//                data.add(rowData); // Only add valid rows
//            } else {
//                System.out.println("Row " + (i + 1) + " contains empty or invalid cells.");
//            }
//        }
//        return data;
//    }
//
//    // fetch data from a specific cell within the .xlsx file
//    public String getCellData(String sheetName, int rowNum, int colNum) {
//        Sheet sheet = workbook.getSheet(sheetName); // Get sheet by name
//        Row row = sheet.getRow(rowNum); // Get row by index
//        Cell cell = row.getCell(colNum); // Get cell by column index
//
//        if (cell != null) {
//            return cell.toString();
//        }
//        return "";
//    }
//
//    // Method to get row count for a particular sheet
//    public int getRowCount(String sheetName) {
//        Sheet sheet = workbook.getSheet(sheetName);
//        return sheet.getPhysicalNumberOfRows();
//    }
//
//    // Method to get Flag value for a specific row and determine whether the scenario should run
//    public boolean shouldRunScenario(String sheetName, int rowNum) {
//        String flagValue = getCellData(sheetName, rowNum, 0); // Flag is in the first column
//        return flagValue == null || flagValue.trim().isEmpty(); // If flag is empty, run the scenario
//    }
//
//    // Close the workbook after usage
//    public void close() throws IOException {
//        workbook.close();
//    }

}
