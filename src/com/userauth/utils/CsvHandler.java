package com.userauth.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvHandler {
    private final String path;

    public CsvHandler(String path) {
        this.path = path;
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to create file: " + path, e);
            }
        }
    }

    public List<List<String>> readCSV() {
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> rawData = reader.readAll();
            List<List<String>> result = new ArrayList<>();
            for (String[] row : rawData) {
                result.add(Arrays.asList(row));
            }
            return result;
        } catch (IOException | CsvException e) {
            System.out.println("Error occurred while reading the CSV file.");
            AuditLogger.logActivity("SYSTEM", "CSV_READ", "FAILURE", "Error occurred while reading the CSV file.");
            throw new RuntimeException(e);
        }
    }

    public void writeCSV(List<List<String>> data) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            List<String[]> dataToWrite = new ArrayList<>();
            for (List<String> row : data) {
                dataToWrite.add(row.toArray(new String[0]));
            }
            writer.writeAll(dataToWrite);
        } catch (IOException e) {
            System.out.println("Error occurred while writing to the CSV file.");
            AuditLogger.logActivity("SYSTEM", "CSV_WRITE", "FAILURE", "Error occurred while writing to the CSV file.");
            throw new RuntimeException(e);
        }
    }

}
