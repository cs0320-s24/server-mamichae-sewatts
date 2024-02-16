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
 *
 * To run the program, run the main method
 * In local host with the browser
 * To run LoadCSV, add /loadcsv?filepath={input filepath}&headers={boolean true/false}
 * To run ViewCSV, add /viewcsv
 * To run SearchCSV, add /searchcsv?value={search value}&columnID={number or String name}
 * For SearchCSV, addition of &columnID={number or String name} is optional
 * To run broadband, add /broadband?state={String state name}&county={String county name}
 * If the user inputs an incorrect State or county, the Server lets them know that
 */
public class Server {
  static final int port = 3232;

  /**
   * Constructor for the Server class.
   *
   * @param accessCSV An instance of AccessCSV for handling CSV operations.
   * @param census    An instance of CensusDataSource for accessing census data.
   */
  public Server(AccessCSV accessCSV, CensusDataSource census) {

    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    // Should I add a "/"?
    Spark.get("loadcsv", new LoadCSVHandler(accessCSV));
    Spark.get("viewcsv", new ViewCSVHandler(accessCSV));
    Spark.get("searchcsv", new SearchCSVHandler(accessCSV));
    Spark.get("broadband", new CountyAccessHandler(census));
    Spark.init();
    Spark.awaitInitialization();
  }

  /**
   * Main method to start the server.
   *
   * @param args Command-line arguments (not used here).
   */
  public static void main(String[] args) {
    Server server = new Server(new AccessCSV(), new CachingCensusDataSource(new CensusAPIUtilities()));
    System.out.println("Server started at http://localhost:" + port);
  }
}
