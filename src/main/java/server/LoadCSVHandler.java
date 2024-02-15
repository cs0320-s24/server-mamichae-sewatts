package server;

import CSV.*;
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

public class LoadCSVHandler implements Route {

  private AccessCSV csv;

  public LoadCSVHandler(AccessCSV csv) {
    this.csv = csv;
  }

  @Override
  public Object handle(Request request, Response response) throws IOException, InconsistentRowException, FactoryFailureException {
    String filepath = request.queryParams("filepath");
    Boolean hasHeaders = Boolean.valueOf(request.queryParams("headers"));
    this.csv.setHasHeaders(hasHeaders);

    Map<String, String> responseMap = new HashMap<>();


    // need buffered? or just file reader
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
      return responseMap;

    } catch (FileNotFoundException e) {
      System.out.println("File not found");
      responseMap.put("error", "File not found");
      return responseMap;
    }

  }

}
