import static org.junit.jupiter.api.Assertions.assertEquals;

import CSV.AccessCSV;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
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

public class TestHandlers {
  private final JsonAdapter<Map<String, Object>> adapter;

  public TestHandlers() {
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
  public void testViewCSVHandlerSuccess() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?filepath=data/census/income_by_race.csv");
    assertEquals(200, loadConnection.getResponseCode());
    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    assertEquals("success", response.get("result"));
  }

  @Test
  public void testViewCSVHandlerFailure() throws IOException {
    // setup w incorrect parameters
    HttpURLConnection loadConnection =
        tryRequest("loadcsv?file=data/census/income_by_race.csv");
    // (the *connection* works, the *API* provides an error response)
    assertEquals(200, loadConnection.getResponseCode());
    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> response =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    // error is correctly returned from response map
    assertEquals("error", response.get("result"));
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
  public void testSearchCSVHandlerFailure() throws IOException {
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
}
