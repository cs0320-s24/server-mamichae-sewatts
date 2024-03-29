package server;

import CSV.*;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Route handler for loading CSV files.
 */
public class LoadCSVHandler implements Route {

  private AccessCSV csv;

  /**
   * Constructor for initializing the LoadCSVHandler with an AccessCSV instance.
   *
   * @param csv The AccessCSV instance to use for loading CSV files.
   */
  public LoadCSVHandler(AccessCSV csv) {
    this.csv = csv;
  }

  /**
   * Method to handle HTTP requests for loading CSV files.
   *
   * @param request  The HTTP request object.
   * @param response The HTTP response object.
   * @return The response data in JSON format.
   * @throws IOException              If an I/O error occurs.
   * @throws InconsistentRowException If the rows in the CSV are inconsistent.
   * @throws FactoryFailureException  If there's a failure in creating objects.
   */
  @Override
  public Object handle(Request request, Response response) throws IOException, InconsistentRowException, FactoryFailureException {
    String filepath = request.queryParams("filepath");
    String headers = request.queryParams("headers");
    Boolean hasHeaders = Boolean.valueOf(request.queryParams("headers"));
    Map<String, String> responseMap = new HashMap<>();

    if (filepath == null || headers == null){
      responseMap.put("error", "bad parameter");
      responseMap.put("result", "error");
      return toJson(responseMap);
    }
    this.csv.setHasHeaders(hasHeaders);

    try {
      BufferedReader reader = new BufferedReader(new FileReader(filepath));
      CSVParser parser = new CSVParser(reader, new StringListCreateFromRow(), hasHeaders);
      List<List<String>> parsedData = parser.parse();

      this.csv.setParsedText(parsedData);
      if (hasHeaders) {

        this.csv.setHeaders(parser.getHeaderList());
      }
      this.csv.setLoaded(true);

      responseMap.put("result", "success");
      responseMap.put("filepath", filepath);
      return toJson(responseMap);

    } catch (FileNotFoundException e) {
      System.out.println("File not found");
      responseMap.put("error", "file not found");
      return toJson(responseMap);
    } catch (IllegalArgumentException e){
      responseMap.put("result", "error");
      responseMap.put("error", "bad parameter");
      return toJson(responseMap);
    } catch (InconsistentRowException e){
        responseMap.put("result", "error");
        responseMap.put("error", "malformed CSV data");
        return toJson(responseMap);
    }

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
