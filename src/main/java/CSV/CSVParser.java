package CSV;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CSVParser<T> {
  private Reader reader;
  private CreatorFromRow<T> rowToObject;
  private Boolean hasHeader;
  private List<String> headerList;
  private List<T> objectList;

  /**
   * Class that takes in a reader with some text and converts the text into a List of objects for
   * each Row The object is specified by the user calling parse Also if the text file has headers -
   * which is specified by the user - the parser class saves a list of strings for the header row
   *
   * @param reader - to read text
   * @param rowToObject - class under interface CreaterFromRow to create rows into objects of
   *     choosing
   * @param hasHeader - boolean indicated whether text has headers
   */
  public CSVParser(Reader reader, CreatorFromRow<T> rowToObject, Boolean hasHeader) {
    this.reader = reader;
    this.rowToObject = rowToObject;
    this.hasHeader = hasHeader;
  }

  /**
   * Parses the document by commas using given regex, returns the object list Does not return the
   * headerList which is empty unless hasHeader is true - need to call getHeaderList when you want
   * to have it in search
   *
   * @return List <T> objectList
   * @throws FactoryFailureException - error creating object from row
   * @throws IOException - error reading file
   * @throws InconsistentRowException - error because rows are not same length
   */
  public List<T> parse() throws FactoryFailureException, IOException, InconsistentRowException {
    String line;
    this.objectList = new ArrayList<>();
    BufferedReader buffReader = new BufferedReader(this.reader);
    int rowNumber = 0;

    int rowLength = -1;

    while ((line = buffReader.readLine()) != null) {
      final Pattern regexSplitCSVRow =
          Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
      List<String> words = Arrays.asList(regexSplitCSVRow.split(line));

      if (rowLength == -1) {
        rowLength = words.size();
      }

      if (words.size() != rowLength) {
        throw new InconsistentRowException("Inconsistent length of the row " + words);
      }

      if (rowNumber == 0) {
        if (this.hasHeader) {
          this.headerList = words;
        } else {
          T obj = this.rowToObject.create(words);
          this.objectList.add(obj);
        }
      } else {
        T obj = this.rowToObject.create(words);
        this.objectList.add(obj);
      }
      rowNumber++;
    }
    return this.objectList;
  }

  /**
   * Getter for Boolean hasHeader
   *
   * @return hasHeader
   */
  public Boolean getHasHeader() {
    return this.hasHeader;
  }

  /**
   * Getter for headerList
   *
   * @return headerList
   */
  public List<String> getHeaderList() {
    return this.headerList;
  }
}
