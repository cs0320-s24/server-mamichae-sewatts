package CSV;

import java.util.List;

/**
 * Class representing access to CSV data.
 */
public class AccessCSV {
  private List<List<String>> parsedText;
  private List<String> header;
  private Boolean loaded;

  private Boolean hasHeaders;

  /**
   * Constructor for the AccessCSV class.
   * Initializes loaded status as false and hasHeaders as false.
   */
  public AccessCSV() {
    this.loaded = false;
    this.hasHeaders = false;
  }

  /**
   * Sets the parsed CSV data.
   *
   * @param text The parsed CSV data to set.
   */
  public void setParsedText(List<List<String>> text) {
    this.parsedText = text;
  }

  /**
   * Sets the headers of the CSV file.
   *
   * @param header The headers to set.
   */
  public void setHeaders(List<String> header) {
    this.header = header;
  }

  /**
   * Sets the loaded status of the CSV file.
   *
   * @param loaded The loaded status to set.
   */
  public void setLoaded(Boolean loaded) {
    this.loaded = loaded;
  }

  /**
   * Gets the loaded status of the CSV file.
   *
   * @return The loaded status.
   */
  public Boolean getLoaded() {
    return this.loaded;
  }

  /**
   * Gets the headers of the CSV file.
   *
   * @return The list of headers.
   */
  public List<String> getHeader() {
    return this.header;
  }

  /**
   * Gets the parsed CSV data.
   *
   * @return The parsed CSV data.
   */
  public List<List<String>> getParsedText() {
    return this.parsedText;
  }

  /**
   * Sets whether the CSV file has headers.
   *
   * @param hasHeaders The flag indicating whether the CSV file has headers.
   */
  public void setHasHeaders(Boolean hasHeaders) {
    this.hasHeaders = hasHeaders;
  }

  /**
   * Gets whether the CSV file has headers.
   *
   * @return The flag indicating whether the CSV file has headers.
   */
  public Boolean getHasHeaders() {
    return this.hasHeaders;
  }
}
