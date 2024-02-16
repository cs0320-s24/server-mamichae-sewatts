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

public class ViewCSVHandler implements Route {
  private final AccessCSV csv;

  public ViewCSVHandler(AccessCSV csv) {
    this.csv = csv;
  }

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

  private String toJson(Object object) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Object> adapter = moshi.adapter(Object.class);
    return adapter.toJson(object);
  }

  private String toJson(List<List<String>> data) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<List<List<String>>> listAdapter =
        moshi.adapter(
            Types.newParameterizedType(
                List.class, Types.newParameterizedType(List.class, String.class)));
    return listAdapter.toJson(data);
  }
}
