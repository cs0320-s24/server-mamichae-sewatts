package server;

import CSV.CSVSearcher;
import CSV.FactoryFailureException;
import CSV.InconsistentRowException;
import CSV.AccessCSV;
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

/**
 * Route handler for searching data within a loaded CSV file.
 */
public class SearchCSVHandler implements Route {
  private final AccessCSV csv;

  /**
   * Constructor for initializing the SearchCSVHandler with an AccessCSV instance.
   *
   * @param csv The AccessCSV instance to use for searching data.
   */
  public SearchCSVHandler(AccessCSV csv) {
    this.csv = csv;
  }

  /**
   * Method to handle HTTP requests for searching data within a loaded CSV file.
   *
   * @param request  The HTTP request object.
   * @param response The HTTP response object.
   * @return The response data in JSON format.
   * @throws DatasourceException If an error occurs while processing the data.
   */
  @Override
  public Object handle(Request request, Response response) throws DatasourceException {
    String searchValue = request.queryParams("value");
    String columnIdentifier = request.queryParams("columnID");


    List<List<String>> searchResult = null;
    Map<String, Object> responseMap = new HashMap<>();

    if (searchValue == null || searchValue.isEmpty() || columnIdentifier == null || columnIdentifier.isEmpty()) {
      // Respond with an error if either searchValue or columnIdentifier is missing
      responseMap.put("result", "error");
      responseMap.put("error", "missing or invalid search parameters");
      return toJson(responseMap);
    }

    try {
      if (this.csv.getLoaded()) {
        List<List<String>> parsedText = this.csv.getParsedText();
        if (parsedText.isEmpty()) {
          responseMap.put("result", "error");
          responseMap.put("error", "CSV file is empty");
          return toJson(responseMap);
        }
        CSVSearcher searcher = new CSVSearcher(this.csv.getParsedText(), this.csv.getHeader(), this.csv.getHasHeaders());
        if (columnIdentifier != null && !columnIdentifier.isEmpty()) {
          try {
            int columnIndex = Integer.parseInt(columnIdentifier);
            searchResult = searcher.search(searchValue, columnIndex);
          } catch (NumberFormatException e) {
            searchResult = searcher.search(searchValue, columnIdentifier);
          } catch (NotFoundException e) {
            responseMap.put("result", "error");
            responseMap.put("error", "columnID not found");
            return toJson(responseMap);
          }
        } else {
          // Search the entire CSV
          searchResult = searcher.search(searchValue);
        }

        responseMap.put("result", "success");
        responseMap.put("data", searchResult);
      } else {
        responseMap.put("result", "error");
        responseMap.put("error", "no CSV loaded");
      }
    } catch (IOException | FactoryFailureException | InconsistentRowException | NotFoundException e) {
      responseMap.put("result", "error");
      responseMap.put("error", "error while processing data: " + e.getMessage());
    }

    return toJson(responseMap);
  }

  /**
   * Helper method to serialize an object to JSON format.
   *
   * @param object The object to serialize.
   * @return The JSON representation of the object.
   */
  private String toJson(Object object) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Object> adapter = moshi.adapter(Object.class);
    return adapter.toJson(object);
  }

}
