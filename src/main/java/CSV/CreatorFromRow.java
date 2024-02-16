package CSV;

import java.util.List;

/**
 * This interface defines a method that allows your CSV parser to convert each row into an object of
 * some arbitrary passed type.
 *
 * <p>Your parser class constructor should take a second parameter of this generic interface type.
 *
 * @param <T> The type of object to create from each row.
 */
public interface CreatorFromRow<T> {

  /**
   * Converts a CSV row into an object of type T.
   *
   * @param row The CSV row to convert.
   * @return The object created from the CSV row.
   * @throws FactoryFailureException If an error occurs during object creation.
   */
  T create(List<String> row) throws FactoryFailureException;
}
