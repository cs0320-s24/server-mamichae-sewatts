package CSV;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements interface CreatorFromRow. Allows CSV Parser to convert each row into a List of
 * integers by parsing the Strings given to integers. Throws NumberFormatException if the list of
 * strings given
 */
public class IntegerListCreateFromRow implements CreatorFromRow<List<Integer>> {

  @Override
  public List<Integer> create(List<String> row) throws FactoryFailureException {
    List<Integer> integerList = new ArrayList<>();
    for (String value : row) {
      try {
        Integer intValue = Integer.parseInt(value);
        integerList.add(intValue);
      } catch (NumberFormatException e) {
        throw new FactoryFailureException("Not an integer: " + value, row);
      }
    }
    return integerList;
  }
}
