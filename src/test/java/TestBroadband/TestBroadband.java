package TestBroadband;

import static org.junit.jupiter.api.Assertions.*;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import countyAccess.AccessData;
import countyAccess.CensusDataSource;
import countyAccess.LocationData;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.CountyAccessHandler;
import spark.Spark;

/**
 * This class contains testing for broadband access functionality.
 */
public class TestBroadband {

  /**
   * Setup method to configure the Spark server before
   * any test case is executed.
   */
  @BeforeAll
  public static void setupOnce() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;
  private JsonAdapter<AccessData> censusDataAdapter;

  /**
   * Setup method to initialize components
   * before each test is executed.
   */
  @BeforeEach
  public void setup() {
    CensusDataSource mockedSource =
        new MockedCensusAPISource(new AccessData(88.5, "2024-02-15 23:52:13"));

    Spark.get("broadband", new CountyAccessHandler(mockedSource));
    Spark.awaitInitialization();

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);

    censusDataAdapter = moshi.adapter(AccessData.class);
  }

  /**
   * Teardown method to clean up resources after each test is executed.
   */
  @AfterEach
  public void tearDown() {
    Spark.unmap("broadband");
    Spark.awaitStop();
  }

  /**
   * Helper method to initiate an HTTP request to the Spark server.
   *
   * @param apiCall The API endpoint to call.
   * @return The HttpURLConnection object representing the connection.
   * @throws IOException If an I/O error occurs.
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestProperty("Content-Type", "application/json");
    clientConnection.setRequestProperty("Accept", "application/json");

    clientConnection.connect();
    return clientConnection;
  }

  final LocationData ny = new LocationData("New+York", "Onondaga+County");

  /**
   * Test method to verify that a broadband search request returns successful response
   * with the expected data.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Test
  public void testCensusRequestSuccess() throws IOException {
    HttpURLConnection loadConnection = tryRequest("broadband?" + ny.toOurServerParams());
    assertEquals(200, loadConnection.getResponseCode());

    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));

    assertEquals(
        censusDataAdapter.toJson(new AccessData(88.5, "2024-02-15 23:52:13")),
        body.get(
            "Percentage of households with broadband access in Onondaga County, New York"));
    loadConnection.disconnect();
  }


  /**
   * Helper method to print error details if the response body contains an error.
   *
   * @param body The response body as a Map.
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body.toString());
    }
  }
}