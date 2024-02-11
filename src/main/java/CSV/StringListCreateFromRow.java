package CSV;

import java.util.List;

/**
 * Implements interface CreatorFromRow. Allows CSV Parser to convert each row into a List of
 * strings.
 */
public class StringListCreateFromRow implements CreatorFromRow<List<String>> {
  public List<String> create(List<String> row) throws FactoryFailureException {
    return row;
  }
}
