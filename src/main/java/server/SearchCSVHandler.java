package server;

import CSV.CSVParser;
import CSV.CSVSearcher;
import CSV.FactoryFailureException;
import CSV.InconsistentRowException;
import CSV.InformationOnCSV;
import CSV.NotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

public class SearchCSVHandler implements Route {
    private final InformationOnCSV csv;

    public SearchCSVHandler(InformationOnCSV csv) {
        this.csv = csv;
    }

    @Override
    public Object handle(Request request, Response response) throws DatasourceException {
        String searchValue = request.queryParams("value");
        String columnIdentifier = request.queryParams("columnID");

        List<List<String>> searchResult = null;
        Map<String, Object> responseMap = new HashMap<>();

        try {
            if (this.csv.getLoaded()) {
                // how to change to either input parser object
                // or change parameters
                // no casting
                CSVSearcher searcher = new CSVSearcher(this.csv.getParsedText());
                if (columnIdentifier != null && !columnIdentifier.isEmpty()) {
                    try {
                        int columnIndex = Integer.parseInt(columnIdentifier);
                        searchResult = searcher.search(searchValue, columnIndex);
                    } catch (NumberFormatException e) {
                        searchResult = searcher.search(searchValue, columnIdentifier);
                    }
                } else {
                    // Search the entire CSV
                    searchResult = searcher.search(searchValue);
                }

                responseMap.put("result", "success");
                responseMap.put("data", searchResult);
            } else {
                responseMap.put("error", "no CSV loaded");
            }
        } catch (IOException e) {
            responseMap.put("error", "error while processing data: " + e.getMessage());
        }

        return toJson(responseMap);
    }
    private List<List<String>> performSearch(List<List<String>> parsedData, List<String> headers, String value, String columnIdentifier)
        throws IOException, NotFoundException, InconsistentRowException, FactoryFailureException {
        // requires parsed object
        CSVSearcher searcher = new CSVSearcher(parsedData);

        if (columnIdentifier.equalsIgnoreCase("none")) {
            return searcher.search(value);
        } else {
            try {
                return searcher.search(value, columnIdentifier);
            } catch (NumberFormatException | NotFoundException e) {
                return searcher.search(value, columnIdentifier);
            }
        }
    }

    private String toJson(Object object) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Object> adapter = moshi.adapter(Object.class);
        return adapter.toJson(object);
    }

}
