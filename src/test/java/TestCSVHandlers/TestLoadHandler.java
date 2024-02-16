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

/**
 * This class contains unit tests for the LoadCSVHandler class.
 */
public class TestLoadHandler {
  private final JsonAdapter<Map<String, Object>> adapter;
  AccessCSV accessCSV;

  /**
   * Constructor to initialize the JSON adapter.
   */
  public TestLoadHandler() {
    Moshi moshi = new Moshi.Builder().build();
    java.lang.reflect.Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    adapter = moshi.adapter(type);
  }

  /**
   * Setup method to configure Spark server before all tests.
   */
  @BeforeAll
  public static void setupBeforeAll() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  /**
   * Setup method to initialize components before each test.
   */
  @BeforeEach
  public void setupBeforeEach() {
    this.accessCSV = new AccessCSV();
    Spark.get("/loadcsv", new LoadCSVHandler(this.accessCSV));
    Spark.get("/searchcsv", new SearchCSVHandler(this.accessCSV));
    Spark.get("/viewcsv", new ViewCSVHandler(this.accessCSV));
    Spark.init();
    Spark.awaitInitialization();
  }

  /**
   * Teardown method to clean up resources after each test.
   */
  @AfterEach
  public void tearDownAfterEach() {
    Spark.unmap("loadcsv");
    Spark.unmap("searchcsv");
    Spark.unmap("viewcsv");
    Spark.awaitStop();
  }

  /**
   * Helper method to parse the response body into a map.
   *
   * @param connection The HttpURLConnection object representing the connection.
   * @return A map containing response data.
   * @throws IOException If an I/O error occurs.
   */
  private Map<String, Object> getResponseMap(HttpURLConnection connection) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(type);
    return adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
  }

  /**
   * Helper method to send an HTTP request to the Spark server.
   *
   * @param apiCall The API endpoint to call.
   * @return The HttpURLConnection object representing the connection.
   * @throws IOException If an I/O error occurs.
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestMethod("GET");
    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Test method to check a successful load of a CSV file.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/census/income_by_race.csv&headers=true");
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals(true, this.accessCSV.getLoaded());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", response.get("result"));
    assertEquals("data/census/income_by_race.csv", response.get("filepath"));
  }

  /**
   * Test method to check a failed load of a CSV file.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFailureBadQuery() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?file=data/census/income_by_race.csv&headers=true");
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals(false, this.accessCSV.getLoaded());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("bad parameter", response.get("error"));
  }

  /**
   * Test method to check failure in case of missing query parameters.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFailureMissingParameter() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/census/income_by_race.csv");
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals(false, this.accessCSV.getLoaded());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("bad parameter", response.get("error"));
  }

  /**
   * Test method to check failure in case of malformed data.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFailureMalformedData() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/malformed/malformed_signs.csv&headers=true");
    assertEquals(200, loadConnection.getResponseCode());
    assertEquals(false, this.accessCSV.getLoaded());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("malformed CSV data", response.get("error"));
  }
}
