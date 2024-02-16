package CSV;

import java.util.List;

/**
 * Implements interface CreatorFromRow. Allows CSV Parser to convert each row into a List of
 * strings.
 */
public class StringListCreateFromRow implements CreatorFromRow<List<String>> {

  /**
   * Converts each row of the CSV into a List of strings.
   *
   * @param row the row of the CSV represented as a List of strings
   * @return a List of strings representing the row of the CSV
   * @throws FactoryFailureException if an error occurs during the conversion process
   */
  public List<String> create(List<String> row) throws FactoryFailureException {
    return row;
  }
}
