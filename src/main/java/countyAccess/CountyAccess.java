package countyAccess;

/**
 * This is a class that models an Activity received from the API.
 *
 * TODO: we will have to convert the state codes and county codes into state and county names
 * (when searching and when sending back, probably deal with this in handler)
 *
 * Where do we cache and save information (using google guava stuff, in handler? i think so)
 * Should save the county codes we have searched for
 *
 * Check these values but this should be right and the right order
 */


public class CountyAccess {
    private String countyAndStateName;

    private double broadbandSubscription;
    private int stateCode;
    private int countyCode;

    public CountyAccess(){}
}
