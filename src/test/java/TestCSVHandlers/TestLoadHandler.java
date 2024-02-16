package TestCSVHandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import CSV.AccessCSV;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.LoadCSVHandler;
import server.SearchCSVHandler;
import server.ViewCSVHandler;
import spark.Spark;

public class TestLoadHandler {
  private final JsonAdapter<Map<String, Object>> adapter;

  public TestLoadHandler() {
    Moshi moshi = new Moshi.Builder().build();
    java.lang.reflect.Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    adapter = moshi.adapter(type);
  }

  @BeforeAll
  public static void setupBeforeAll() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  @BeforeEach
  public void setupBeforeEach() {
    AccessCSV accessCSV = new AccessCSV();
    Spark.get("/loadcsv", new LoadCSVHandler(accessCSV));
    Spark.get("/searchcsv", new SearchCSVHandler(accessCSV));
    Spark.get("/viewcsv", new ViewCSVHandler(accessCSV));
    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void tearDownAfterEach() {
    // Gracefully stop Spark listening on both endpoints after each test
    Spark.unmap("loadcsv");
    Spark.unmap("searchcsv");
    Spark.unmap("viewcsv");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  private Map<String, Object> getResponseMap(HttpURLConnection connection) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(type);
    return adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
  }
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestMethod("GET");
    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void testSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/census/income_by_race.csv&headers=true");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap = getResponseMap(loadConnection);
    assertEquals("error", responseMap.get("result"));
    assertEquals("missing_parameter", responseMap.get("error"));
  }

  @Test
  public void testFailureBadQuery() throws IOException {
    // setup with incorrect parameters
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?file=data/census/income_by_race.csv");
    // the connection works, the API provides an error response
    assertEquals(200, loadConnection.getResponseCode());
  }
}
