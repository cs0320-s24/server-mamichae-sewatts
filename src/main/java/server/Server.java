package server;

import static spark.Spark.after;


import CSV.AccessCSV;
import countyAccess.CachingCensusDataSource;
import countyAccess.CensusAPIUtilities;
import countyAccess.CensusDataSource;
import spark.Spark;

/**
 * Class that takes in user's requests and directs them to the proper handlers to carry out the
 * searches.
 */
public class Server {
    static final int port = 3232;

    /**
     *
     * @param
     * @param
     */
    public Server(AccessCSV accessCSV, CensusDataSource census) {

        Spark.port(port);

        after(
                (request, response) -> {
                    response.header("Access-Control-Allow-Origin", "*");
                    response.header("Access-Control-Allow-Methods", "*");
                });

        //Should I add a "/"?
        Spark.get("loadcsv", new LoadCSVHandler(accessCSV));
        Spark.get("viewcsv", new ViewCSVHandler(accessCSV));
        Spark.get("searchcsv", new SearchCSVHandler(accessCSV));
        Spark.get("broadband", new CountyAccessHandler(census));
        Spark.init();
        Spark.awaitInitialization();
    }

    public static void main(String[] args) {
        //or should this be called on the caching class, no right?
        Server server = new Server(new AccessCSV(), new CensusAPIUtilities());
        System.out.println("Server started at http://localhost:" + port);
    }
}
