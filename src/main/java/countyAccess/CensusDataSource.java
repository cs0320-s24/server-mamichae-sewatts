package countyAccess;

import server.DatasourceException;

/**
 * Interface defining methods for accessing census data sources.
 */
public interface CensusDataSource {
  /**
   * Retrieves broadband subscription data for the specified location.
   *
   * @param location The location for which broadband subscription data is requested.
   * @return The broadband subscription data for the location.
   * @throws DatasourceException If an error occurs during data retrieval.
   */
  AccessData getBroadbandSubscription(LocationData location) throws DatasourceException;
}
