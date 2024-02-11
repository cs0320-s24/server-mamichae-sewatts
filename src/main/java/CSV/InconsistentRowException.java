package CSV;

/**
 * This is an error provided to catch the error when a document being parsed has rows with different
 * lengths /different number of columns
 */
public class InconsistentRowException extends Exception {
  public InconsistentRowException(String message) {
    super(message);
  }
}
