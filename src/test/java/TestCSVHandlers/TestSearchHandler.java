package TestCSVHandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import CSV.AccessCSV;
import CSV.CSVParser;
import CSV.FactoryFailureException;
import CSV.InconsistentRowException;
import CSV.StringListCreateFromRow;
import com.beust.ah.A;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.FileReader;
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
 * This class contains unit tests for the SearchCSVHandler class.
 */
public class TestSearchHandler {
  private final JsonAdapter<Map<String, Object>> adapter;

  /**
   * Constructor to initialize the JSON adapter.
   */
  public TestSearchHandler() {
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
   * Test method to check successful search by column ID.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testSuccessColumnID() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/dol_ri_earnings_disparity.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=White&columnID=1");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));

    HashMap<String, Object> expectedMap = new HashMap<String, Object>();
    List<List<String>> expectedList = new ArrayList<>();
    List<String> innerList = List.of("RI", "White", "\" $1,058.47 \"", "395773.6521", " $1.00 ", "75%");
    expectedList.add(innerList);

    expectedMap.put("data", expectedList);
    assertEquals(expectedMap.get("data"), response.get("data"));
    assertEquals("success", response.get("result"));
  }

  /**
   * Test method to check successful search by column name.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testSuccessColumnName() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/postsecondary_education.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=0.069233258&columnID=share");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    HashMap<String, Object> expectedMap = new HashMap<String, Object>();
    List<List<String>> expectedList = new ArrayList<>();
    List<String> innerList = List.of("Asian","2020","2020","217156","Brown University","214","brown-university","0.069233258","Men","1");
    expectedList.add(innerList);

    expectedMap.put("data", expectedList);
    assertEquals(expectedMap.get("data"), response.get("data"));
    assertEquals("success", response.get("result"));
  }

  /**
   * Test method to check failure in case of invalid path.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFailureBadPath() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?path=data/census/dol_ri_earnings_disparity.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=White&columnID=1");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
  }

  /**
   * Test method to check failure in case of empty path.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testEmptyFile() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/edge/empty.csv&headers=false");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=White&columnID=1");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("CSV file is empty", response.get("error"));

  }

  /**
   * Test method to check failure in case of file not found.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFileNotFound() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/hello.csv&headers=false");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=hello&columnID=1");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("no CSV loaded", response.get("error"));

  }

  /**
   * Test method to check failure in case of invalid value query.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testFailureBadValueQuery() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/dol_ri_earnings_disparity.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    // value not in csv data
    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Blue&columnID=1");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("success", response.get("result"));
    assertEquals(new ArrayList<>(), response.get("data"));
  }

  /**
   * Test method to check failure in case of invalid columnID query.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testBadColumnIDQuery() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/dol_ri_earnings_disparity.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Black&columnID=8");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("columnID not found", response.get("error"));
  }

  /**
   * Test method to check failure in case of malformed data.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testMalformedData() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/malformed/malformed_signs.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Aries&columnID=Member");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("no CSV loaded", response.get("error"));
  }

  /**
   * Test method to check failure in case of missing value parameter.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testMissingParametersValue() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/dol_ri_earnings_disparity.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?columnID=8");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("missing or invalid search parameters", response.get("error"));
  }

  /**
   * Test method to check failure in case of missing column parameter.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testMissingParametersColumn() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/malformed/malformed_signs.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Black");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("missing or invalid search parameters", response.get("error"));
  }

}
