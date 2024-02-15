package countyAccess;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import server.DatasourceException;

public class CachingCensusDataSource implements CensusDataSource {
  private final CensusDataSource original;
  private final LoadingCache<LocationData, AccessData> cache;

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

  @Override
  public AccessData getBroadbandSubscription(LocationData location) throws DatasourceException {
    try {
      return cache.get(location, () -> original.getBroadbandSubscription(location));
    } catch (ExecutionException e) {
      // Extract the cause of the exception and rethrow, CHECK THIS
      Throwable cause = e.getCause();
      if (cause instanceof DatasourceException) {
        throw (DatasourceException) cause;
      } else {
        throw new DatasourceException("Error while retrieving broadband subscription data", cause);
      }
    }
  }
}

