package countyAccess;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import okio.Buffer;
import server.DatasourceException;

/** Deserializing JSON from the API into a Census object. */
public class CensusAPIUtilities implements CensusDataSource {

  private static HashMap<String, String> stateNameToCode;
  private static HashMap<String, String> countyNameToCode;

  /**
   * Constructor for the CensusAPIUtilities class.
   */
  public CensusAPIUtilities() {
    this.stateNameToCode = new HashMap<>();
    this.countyNameToCode = new HashMap<>();
  }

  /**
   * Accesses and deserializes state codes from the Census API
   * and populates the stateNameToCode map.
   *
   * @throws DatasourceException If an error occurs during data retrieval.
   */
  // should it return anything, probably no?
  // CHANGE THIS
  private static void accessStateCodes() throws DatasourceException {
    if(!stateNameToCode.isEmpty()) {
      return;
    }
    try {
      URL requestURL =
          new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
      HttpURLConnection clientStateConnection = connect(requestURL);

      Moshi moshi = new Moshi.Builder().build();
      Type listOfString = Types.newParameterizedType(List.class, String.class);
      Type listOfList = Types.newParameterizedType(List.class, listOfString);

      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfList);

      List<List<String>> states =
          adapter.fromJson(new Buffer().readFrom(clientStateConnection.getInputStream()));
      clientStateConnection.disconnect();

      for (int i = 1; i < states.size(); i++) {
        stateNameToCode.put(states.get(i).get(0), states.get(i).get(1));
      }

      // check this, put in a message
      if (states == null) {
        throw new DatasourceException("Malformed response from Census API");
      }
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  /**
   * Accesses county codes for a state from the API and populates the countyNameToCode map.
   *
   * @param stateCode The state code.
   * @throws DatasourceException If an error occurs during data retrieval.
   */
  private static void accessCountyCodes(String stateCode) throws DatasourceException {
    //if(!stateNameToCode.containsKey())
    try {

      URL requestURL =
          new URL(
              "https",
              "api.census.gov",
              "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode);
      HttpURLConnection clientStateConnection = connect(requestURL);

      Moshi moshi = new Moshi.Builder().build();
      Type listOfString = Types.newParameterizedType(List.class, String.class);
      Type listOfList = Types.newParameterizedType(List.class, listOfString);

      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfList);

      List<List<String>> counties =
          adapter.fromJson(new Buffer().readFrom(clientStateConnection.getInputStream()));
      clientStateConnection.disconnect();

      for (int i = 1; i < counties.size(); i++) {
        countyNameToCode.put(counties.get(i).get(0), counties.get(i).get(2));
      }
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  /**
   * Establishes an HTTP connection to the specified URL.
   *
   * @param requestURL The URL to connect to.
   * @return The HttpURLConnection object representing the connection.
   * @throws DatasourceException If an error occurs during connection establishment.
   * @throws IOException If an I/O error occurs.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    System.out.println("URL + " + requestURL);
    System.out.println("stateNameToCode: " + stateNameToCode.keySet());
    System.out.println("countyNameToCode: " + countyNameToCode.keySet());
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200)
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    return clientConnection;
  }

  /**
   * Gets broadband subscription data for the location.
   *
   * @param location The location for which broadband subscription data is requested.
   * @return The broadband subscription data for the specified location.
   * @throws DatasourceException If an error occurs during data retrieval.
   */
  @Override
  public AccessData getBroadbandSubscription(LocationData location) throws DatasourceException {
    System.out.println("calling getBroadBandSubscription");
    return getBroadbandSubscriptionHelper(location.state(), location.county());
  }

  /**
   * Helper method to get broadband subscription data for the state and county.
   *
   * @param state The name of the state.
   * @param county The name of the county.
   * @return The broadband subscription data for the specified state and county.
   * @throws DatasourceException If an error occurs during data retrieval.
   */
  public static AccessData getBroadbandSubscriptionHelper(String state, String county)
      throws DatasourceException {
    try {
      accessStateCodes();
      String stateCode = stateNameToCode.get(state);

      accessCountyCodes(stateCode);

      String countyName = county + ", " + state;
      String countyCode = countyNameToCode.get(countyName);

      URL requestURL =
          new URL(
              "https",
              "api.census.gov",
              "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                  + countyCode
                  + "&in=state:"
                  + stateCode);
      HttpURLConnection clientConnection = connect(requestURL);

      Moshi moshi = new Moshi.Builder().build();
      Type listOfString = Types.newParameterizedType(List.class, String.class);
      Type listOfList = Types.newParameterizedType(List.class, listOfString);

      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfList);

      List<List<String>> data =
          adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();

      if (data == null) {
        throw new DatasourceException("Malformed response from Census API");
      }

      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date timestamp = new Date();

      return new AccessData(Double.parseDouble(data.get(1).get(1)), dateFormat.format(timestamp));
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }
}
