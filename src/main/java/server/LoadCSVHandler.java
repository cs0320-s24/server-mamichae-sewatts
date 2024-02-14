package server;

import CSV.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCSVHandler implements Route {

    private AccessCSV csv;

    public LoadCSVHandler(AccessCSV csv) {
        this.csv = csv;
    }

    @Override
    public Object handle(Request request, Response response) {
        String filepath = request.queryParams("filepath");
        Boolean hasHeaders = Boolean.valueOf(request.queryParams("headers"));
        List<String> headers = new ArrayList<>();
        Map<String, String> responseMap = new HashMap<>();

        // need buffered? or just file reader
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            CSVParser parser = new CSVParser(reader, new StringListCreateFromRow(), hasHeaders);
            List<List<String>> parsedData = parser.parse();

            this.csv.setParsedText(parsedData);
            if (hasHeaders) {
                this.csv.setHeaders(parser.getHeaderList());
            }
            this.csv.setLoaded(true);

            responseMap.put("result", "success");
            responseMap.put("filepath", filepath);
        } catch (IOException e) {
            responseMap.put("error", "IO error occurred: " + e.getMessage());
        } catch (FactoryFailureException e) {
            responseMap.put("error", "Failed to create CSV parser: " + e.getMessage());
        } catch (InconsistentRowException e) {
          throw new RuntimeException(e);
        }

        return responseMap;
    }

}
