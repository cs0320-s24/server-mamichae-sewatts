package countyAccess;

import server.DatasourceException;

public interface CensusDataSource {
  AccessData getBroadbandSubscription(LocationData location) throws DatasourceException;
}
