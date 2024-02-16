package server;

import CSV.AccessCSV;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Route handler for viewing the loaded CSV data.
 */
public class ViewCSVHandler implements Route {
  private final AccessCSV csv;

  /**
   * Constructor for the ViewCSVHandler class.
   *
   * @param csv An instance of AccessCSV representing the loaded CSV data.
   */
  public ViewCSVHandler(AccessCSV csv) {
    this.csv = csv;
  }

  /**
   * Handles the HTTP GET request for viewing the loaded CSV data.
   *
   * @param request  The HTTP request object.
   * @param response The HTTP response object.
   * @return JSON representation of the loaded CSV data.
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();
    try {
      if (!this.csv.getLoaded()) {
        responseMap.put("result", "error");
        responseMap.put("error", "no CSV loaded");
      } else {
        List<List<String>> parsedData = csv.getParsedText();
        if (parsedData.isEmpty()) {
          responseMap.put("result", "success - file is empty");
        } else {
          responseMap.put("result", "success");
          responseMap.put("data", toJson(parsedData));
        }
      }
    } catch (Exception e) {
      responseMap.put("result", "error");
      responseMap.put("error", "error while processing data");
    }
    return toJson(responseMap);
  }

  /**
   * Converts an object to its JSON representation using Moshi.
   *
   * @param object The object to convert to JSON.
   * @return JSON string representing the object.
   */
  private String toJson(Object object) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Object> adapter = moshi.adapter(Object.class);
    return adapter.toJson(object);
  }

  /**
   * Converts a list of lists of strings to its JSON representation using Moshi.
   *
   * @param data The list of lists of strings to convert to JSON.
   * @return JSON string representing the list of lists of strings.
   */
  private String toJson(List<List<String>> data) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<List<List<String>>> listAdapter =
        moshi.adapter(
            Types.newParameterizedType(
                List.class, Types.newParameterizedType(List.class, String.class)));
    return listAdapter.toJson(data);
  }
}
