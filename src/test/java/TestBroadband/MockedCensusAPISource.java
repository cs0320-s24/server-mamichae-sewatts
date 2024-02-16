package TestBroadband;

import countyAccess.AccessData;
import countyAccess.CensusDataSource;
import countyAccess.LocationData;
import server.DatasourceException;

/**
 * A datasource that never actually calls the NWS API, but always returns a constant
 * weather-data value. This is very useful in testing, and avoiding the costs of
 * real API invocations. The technique is called "mocking", as in "faking".
 */
public class MockedCensusAPISource implements CensusDataSource {
  private final AccessData constantData;

  public MockedCensusAPISource(AccessData constantData) {
    this.constantData = constantData;
  }

  @Override
  public AccessData getBroadbandSubscription(LocationData location) throws DatasourceException {
    return constantData;
  }
}