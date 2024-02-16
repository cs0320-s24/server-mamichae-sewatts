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

public class TestSearchHandler {
  private final JsonAdapter<Map<String, Object>> adapter;

  public TestSearchHandler() {
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

  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestMethod("GET");
    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void testSearchCSVHandlerSuccess() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/dol_ri_earnings_disparity.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=White&columnID=1");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("success", response.get("result"));
  }

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

  @Test
  public void testEmptyFile() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/empty.csv&headers=false");
    assertEquals(200, loadCSVConnection.getResponseCode());

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=White&columnID=1");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("csv file is empty", response.get("error"));

  }

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

  @Test
  public void testBadColumnIDQuery() throws IOException {
    HttpURLConnection loadCSVConnection =
        tryRequest("loadcsv?filepath=data/census/dol_ri_earnings_disparity.csv&headers=true");
    assertEquals(200, loadCSVConnection.getResponseCode());

    // columnID out of scope
    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Black&columnID=8");
    assertEquals(200, searchConnection.getResponseCode());

    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    assertEquals("error", response.get("result"));
    assertEquals("columnID not found", response.get("error"));
  }





  //////////////////////

// with headers no column id
// with no headers no column id
// column id
// column name
// load improper file
// empty returns proper return message

}
