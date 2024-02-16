package countyAccess;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import server.DatasourceException;

/**
 * This class caches the results of broadband subscription data queries
 * and reduces the number of expensive calls.
 */
public class CachingCensusDataSource implements CensusDataSource {
  private final CensusDataSource original;
  private final LoadingCache<LocationData, AccessData> cache;

  /**
   * Constructor for the CachingCensusDataSource.
   *
   * @param original The original CensusDataSource to be cached.
   */
  public CachingCensusDataSource(CensusDataSource original) {
    this.original = original;
    this.cache =
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                new CacheLoader<LocationData, AccessData>() {
                  public AccessData load(LocationData location) throws DatasourceException {
                    return original.getBroadbandSubscription(location);
                  }
                });
  }

  /**
   * Retrieves the broadband subscription data for the specified location.
   * If  data is available in the cache, it is returned from the cache.
   * If not, it is taken from original data source and stored in the cache for later use.
   *
   * @param location The location for which broadband subscription data is requested.
   * @return The broadband subscription data for the specified location.
   * @throws DatasourceException If an error occurs while retrieving data.
   */
  @Override
  public AccessData getBroadbandSubscription(LocationData location) throws DatasourceException {
    try {
      return cache.get(location, () -> original.getBroadbandSubscription(location));
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof DatasourceException) {
        throw (DatasourceException) cause;
      } else {
        throw new DatasourceException("Error while retrieving broadband subscription data", cause);
      }
    }
  }
}

