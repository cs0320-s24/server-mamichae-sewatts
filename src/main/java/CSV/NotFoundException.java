package CSV;

/**
 * An exception thrown when a requested item is not found.
 */
public class NotFoundException extends Exception {
  /**
   * Constructs a new NotFoundException with the specified message.
   *
   * @param message the detail message.
   */
  public NotFoundException(String message) {
    super(message);
  }
}
