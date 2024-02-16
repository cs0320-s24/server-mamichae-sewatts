package TestCSVHandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import CSV.AccessCSV;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * This class contains unit tests for the ViewCSVHandler class.
 */
public class TestViewHandler {
  private final JsonAdapter<Map<String, Object>> adapter;
  private final Moshi moshi;

  /**
   * Constructor to initialize the JSON adapter and Moshi instance.
   */
  public TestViewHandler() {
    this.moshi = new Moshi.Builder().build();
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
   * Setup method to initialize resources before each test.
   */
  @BeforeEach
  public void setupBeforeEach() {
    AccessCSV accessCSV = new AccessCSV();
    Spark.get("/loadcsv", new LoadCSVHandler(accessCSV));
    Spark.get("/searchcsv", new SearchCSVHandler(accessCSV));
    Spark.get("/viewcsv", new ViewCSVHandler(accessCSV));
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
   * Test method to check successful view of CSV data.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/census/income_by_race.csv&headers=true");
    assertEquals(200, loadConnection.getResponseCode());
    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    assertEquals("success", response.get("result"));
  }

  /**
   * Test method to check successful view of CSV data with specific content.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testSuccessDataOutput() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/edge/simple.csv&headers=false");
    assertEquals(200, loadConnection.getResponseCode());
    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));

    List<List<String>> responseData = this.moshi.adapter(List.class).fromJson((String) response.get("data"));

    List<List<String>> expectedData = new ArrayList<>();
    expectedData.add(List.of("hello"));

    assertEquals(expectedData, responseData);
    assertEquals("success", response.get("result"));
  }

  /**
   * Test method to check failure in case of bad file path.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFailureBadPath() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?file=data/census/income_by_race.csv&headers=true");
    assertEquals(200, loadConnection.getResponseCode());
    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    assertEquals("error", response.get("result"));
  }

  /**
   * Test method to check failure in case of empty file.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFailureEmptyFile() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/edge/empty.csv&headers=false");
    assertEquals(200, loadConnection.getResponseCode());
    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    assertEquals("success - file is empty", response.get("result"));
  }
}
