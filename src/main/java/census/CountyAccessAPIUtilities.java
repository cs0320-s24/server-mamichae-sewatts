package census;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;

/**
 * Deserializing JSON from the API into a Census object.
 */

public class CensusAPIUtilities {
  /**
   * Deserializes JSON from the BoredAPI into an Activity object.
   *
   * @param jsonCensus
   * @return
   */
  public static Census deserializeCensus(String jsonCensus){
    try {
      // Initializes Moshi
      Moshi moshi = new Moshi.Builder().build();

      // Initializes an adapter to a CensusData class then uses it to parse the JSON.
      JsonAdapter<Census> adapter = moshi.adapter(Census.class);

      Census censusData = adapter.fromJson(jsonCensus);

      return censusData;
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error deserializing JSON into Activity: " + e.getMessage());
      // Throw custom error? What is the best way to handle error?
      return null;
    }
  }
}
