package countyAccess;

import server.DatasourceException;
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



/**
 * Deserializing JSON from the API into a Census object.
 */

public class CensusAPIUtilities implements CensusDataSource {

  private static HashMap<String, String> stateNametoCode;
  private static HashMap<String, String> countyNameToCode;
  public CensusAPIUtilities(){
    this.stateNametoCode = new HashMap<>();
    this.countyNameToCode = new HashMap<>();
  }
  /**
   * Deserializes JSON from the API into a Census object
   *
   * @param
   * @return
   */
  //should it return anything, probably no?
  //CHANGE THIS
  private static void accessStateCodes() throws DatasourceException {
    try {

      URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
      HttpURLConnection clientStateConnection = connect(requestURL);

      Moshi moshi = new Moshi.Builder().build();
      Type listOfString = Types.newParameterizedType(List.class, String.class);
      Type listOfList = Types.newParameterizedType(List.class, listOfString);

      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfList);

      List<List<String>> states = adapter.fromJson(new Buffer().readFrom(clientStateConnection.getInputStream()));
      clientStateConnection.disconnect();

      for (int i = 1; i < states.size(); i++) {
        stateNametoCode.put(states.get(i).get(0), states.get(i).get(1));
      }

      //check this, put in a message
      if (states == null) {
        throw new DatasourceException("Malformed response from Census API");
      }
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  private static void accessCountyCodes(String stateCode) throws DatasourceException {
    try {

      URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode);
      HttpURLConnection clientStateConnection = connect(requestURL);

      Moshi moshi = new Moshi.Builder().build();
      Type listOfString = Types.newParameterizedType(List.class, String.class);
      Type listOfList = Types.newParameterizedType(List.class, listOfString);

      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfList);

      List<List<String>> counties = adapter.fromJson(new Buffer().readFrom(clientStateConnection.getInputStream()));
      clientStateConnection.disconnect();

      for (int i = 1; i < counties.size(); i++) {
        stateNametoCode.put(counties.get(i).get(0), counties.get(i).get(2));
      }
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

    /**
   * Private helper method; throws IOException so different callers
   * can handle differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if(! (urlConnection instanceof HttpURLConnection))
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DatasourceException("unexpected: API connection not success status "+clientConnection.getResponseMessage());
    return clientConnection;
  }

  @Override
  public AccessData getBroadbandSubscription(LocationData location) throws DatasourceException {
    return getBroadbandSubscriptionHelper(location.state(), location.county());
  }

  public static AccessData getBroadbandSubscriptionHelper(String state, String county) throws DatasourceException {
    try {
      accessStateCodes();
      String stateCode = stateNametoCode.get(state);

      accessCountyCodes(stateCode);
      String countyCode = countyNameToCode.get(county + ", " + state);

      URL requestURL = new URL("https", "api.census.gov", "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:" + countyCode + "&in=state:" + stateCode);
      HttpURLConnection clientConnection = connect(requestURL);

      Moshi moshi = new Moshi.Builder().build();
      Type listOfString = Types.newParameterizedType(List.class, String.class);
      Type listOfList = Types.newParameterizedType(List.class, listOfString);

      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfList);

      List<List<String>> data = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();

      //CHECK THIS:
      if (data == null) {
        throw new DatasourceException("Malformed response from Census API");
      }

      //TODO: CHECK THIS WHERE IS DATE FORMAT REQUIRED TO BE LIKE THIS
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date timestamp = new Date();

      return new AccessData(Double.parseDouble(data.get(1).get(1)), dateFormat.format(timestamp));
    } catch (IOException e) {
      throw new DatasourceException(e.getMessage());
    }
  }
}