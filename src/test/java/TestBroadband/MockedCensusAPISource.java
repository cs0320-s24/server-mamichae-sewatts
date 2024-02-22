package TestBroadband;

import countyAccess.AccessData;
import countyAccess.CensusDataSource;
import countyAccess.LocationData;
import server.DatasourceException;

/**
 *
 */
public class MockedCensusAPISource implements CensusDataSource {
  private final AccessData constantData;

  /**
   * Constructs a new MockedCensusAPISource with the given constant access data.
   *
   * @param constantData The constant access data value to be returned by this mocked data source.
   */
  public MockedCensusAPISource(AccessData constantData) {
    this.constantData = constantData;
  }

  /**
   * Gets the broadband subscription data for the given location.
   * This method always returns the constant access data value provided during construction.
   *
   * @param location The location for which to retrieve the broadband subscription data.
   * @return The constant access data value.
   * @throws DatasourceException if there is any error in retrieving the data.
   */
  @Override
  public AccessData getBroadbandSubscription(LocationData location) throws DatasourceException {
    return constantData;
  }
}