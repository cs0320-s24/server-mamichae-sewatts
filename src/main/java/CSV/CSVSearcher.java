package CSV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVSearcher {
  List<List<String>> listOfStringRows;
  CSVParser parser;

  List<String> headerList;

  Boolean hasHeader;

  /**
   * Searches for a given searchWord in a CSV document parsed into a List<List<String>> where each
   * row is a List<String>. Searches include partial matches if the document contains the given
   * searchWord. Searcher can either search the whole document and return the matching Rows as a
   * List<List<String>>, or it can take in a column as a string or an int column number and search
   * for matching rows only in the given column.
   *
   * @param parser - Class parser to parse the text
   * @throws IOException - error reading file
   * @throws FactoryFailureException - error creating object from row
   */
  public CSVSearcher(CSVParser<List<String>> parser)
      throws IOException, FactoryFailureException, InconsistentRowException {
    this.parser = parser;
    this.listOfStringRows = this.parser.parse();
    this.hasHeader = this.parser.getHasHeader();
    this.headerList = this.parser.getHeaderList();
  }

  public CSVSearcher(List<List<String>> parsedText, List<String> headerList, Boolean hasHeader) throws IOException, FactoryFailureException, InconsistentRowException {
    this.listOfStringRows = parsedText;
    this.headerList = headerList;
    this.hasHeader = hasHeader;
  }


  /**
   * Calls search with the searchWord and with a string header - so only searches in the column that
   * has that String header. This searches for the column with that string header - gets the integer
   * value of that column And then calls the other search method with the integer header passed in
   *
   * @param searchWord - String word looking for in the text
   * @param header - String header that indicates which column to look at
   * @return rows that have the search word in the given column
   */
  public List<List<String>> search(String searchWord, String header) throws NotFoundException {
    if (!this.hasHeader) {
      throw new NotFoundException("CSV missing headers");
    }
    List<String> headerList = this.headerList;
    int wordCounter = 0;
    for (String word : headerList) {
      if (word.equals(header)) {
        return search(searchWord, wordCounter);
      }
      wordCounter++;
    }

    // Error here: bc did not give header that is in headerList
    throw new NotFoundException("Header inputted was not in CSV header list");
  }

  /**
   * Calls search with an integer header - so only searches in the column that is at that index in
   * the headerList
   *
   * @param searchWord - String word looking for in the text
   * @param header - Int index that indicates which column to look at
   * @return rows that have the search word in the given column
   */
  public List<List<String>> search(String searchWord, int header) throws NotFoundException {
    if (!this.hasHeader) {
      throw new NotFoundException("CSV missing headers");
    }

    List<String> headerList = this.headerList;
    if (header >= headerList.size()) {
      throw new NotFoundException("Header index out of bounds of CSV header list");
    }
    if (header < 0) {
      throw new NotFoundException("Header index out of bounds of CSV header list");
    }

    List<List<String>> returnList = new ArrayList<>();
    for (List<String> stringRow : this.listOfStringRows) {
      String wordAtColumn = stringRow.get(header);
      if (wordAtColumn.contains(searchWord)) {
        returnList.add(stringRow);
      }
    }
    return returnList;
  }

  /**
   * Calls search with no header - searches the whole document to find rows that have the given
   * search word in them
   *
   * @param searchWord - String word looking for in the text
   * @return rows that have the search word in them
   */
  public List<List<String>> search(String searchWord) {
    List<List<String>> returnList = new ArrayList<>();
    for (List<String> stringRow : this.listOfStringRows) {
      for (String word : stringRow) {
        if (word.contains(searchWord)) {
          System.out.println("searchWord" + searchWord);
          System.out.println("word" + word);
          returnList.add(stringRow);
          break;
        }
      }
    }
    return returnList;
  }
}
