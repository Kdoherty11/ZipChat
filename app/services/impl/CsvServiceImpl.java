package services.impl;

import com.google.common.collect.ImmutableSet;
import play.Logger;
import services.CsvService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by kdoherty on 8/10/15.
 */
public class CsvServiceImpl implements CsvService {

    @Override
    public ImmutableSet<String> readToImmutableSet(String fileName) {
        ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<>();
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(fileName));

            while ((line = br.readLine()) != null) {
                builder.add(line.split(cvsSplitBy));
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem reading csv file " + fileName);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Logger.error("Problem closing BufferedReader", e);
                }
            }
        }

        return builder.build();
    }
}
