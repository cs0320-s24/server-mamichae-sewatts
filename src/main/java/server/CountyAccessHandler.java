package server;


import CSV.AccessCSV;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import countyAccess.AccessData;
import countyAccess.CensusDataSource;
import countyAccess.LocationData;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Route handler for handling requests related to county access API data.
 */
public class CountyAccessHandler implements Route {
    private CensusDataSource censusData;

    /**
     * Constructor for initializing the CountyAccessHandler with a CensusDataSource.
     *
     * @param censusData The data source for accessing census data.
     */
    public CountyAccessHandler(CensusDataSource censusData) {
        this.censusData = censusData;
    }

    /**
     * Method to handle HTTP requests related to county access API data.
     *
     * @param request  The HTTP request object.
     * @param response The HTTP response object.
     * @return The response data in JSON format.
     */
    @Override
    public Object handle(Request request, Response response) {
        // Retrieve 'state' and 'county' query parameters
        String state = request.queryParams("state");
        String county = request.queryParams("county");

        Moshi moshi = new Moshi.Builder().build();

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);

        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        JsonAdapter<AccessData> accessDataAdapter = moshi.adapter(AccessData.class);

        Map<String, Object> responseMap = new HashMap<>();

        try {
            LocationData location = new LocationData(state, county);

            AccessData accessData = this.censusData.getBroadbandSubscription(location);

            responseMap.put("result", "success");
            responseMap.put("Percentage of households with broadband access in " + county + ", " + state, accessDataAdapter.toJson(accessData));

            return adapter.toJson(responseMap);

        } catch (DatasourceException e) {
            responseMap.put("result", "error");
            responseMap.put("error_type", "datasource");
            responseMap.put("details", e.getMessage());

            return adapter.toJson(responseMap);
        }

    }
}