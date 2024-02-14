package server;

import CSV.AccessCSV;
import com.squareup.moshi.Moshi;
import countyAccess.CensusDataSource;
import spark.Request;
import spark.Response;
import spark.Route;

public class CountyAccessHandler implements Route {
    private CensusDataSource censusData;

    public CountyAccessHandler(CensusDataSource censusData) {
        this.censusData = censusData;
    }
    @Override
    public Object handle(Request request, Response response) {
        // Retrieve 'state' and 'county' query parameters from the HTTP request
        String state = request.queryParams("state");
        String county = request.queryParams("county");

        // Create a Moshi instance for JSON serialization/deserialization
        Moshi moshi = new Moshi.Builder().build();

        //TODO: FINISH THIS
        //MAKE SURE TO USE CACHING HERE
        return null;
    }
}
