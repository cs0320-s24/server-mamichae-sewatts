package countyAccess;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


/**
 * Deserializing JSON from the API into a Census object.
 */

public class CensusAPIUtilities {
  //private final Moshi moshi;
//  private final HashMap<String, String> stateCodes;
//  private final HashMap<String, String> countyCodes;

  /**
   * Deserializes JSON from the API into a Census object
   *
   * @param jsonCensus
   * @return
   */
  public static CountyAccess deserializeCensus(String jsonCensus) {
    try {
      // Initializes Moshi
      Moshi moshi = new Moshi.Builder().build();

      // Initializes an adapter to a CensusData class then uses it to parse the JSON.
      JsonAdapter<CountyAccess> adapter = moshi.adapter(CountyAccess.class);

      CountyAccess censusData = adapter.fromJson(jsonCensus);

      return censusData;
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error deserializing JSON into Activity: " + e.getMessage());
      // Throw custom error? What is the best way to handle error?
      return null;
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

}