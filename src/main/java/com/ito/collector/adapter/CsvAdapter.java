package com.ito.collector.adapter;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvAdapter {
    public List<String[]> readCsv(String path) {
        List<String[]> result = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                result.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
