package CSV;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/** The Main class of our project. This is where execution begins. */

/** User */
public final class Main {

  CreatorFromRow<List<String>> rowToStringList = new StringListCreateFromRow();
  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */

  // String filename, String searchWord, (string/int) header (optional), boolean hasHeader

  /**
   * Allows the user to run search. User must input 2 to 4 Arguments. The arguments are: String
   * filename, String searchWord, (String/Integer) header (optional), (String) hasHeader (optional)
   * hasHeader must state: true, if the given text has Headers. If calling search with a header but
   * hasHeader is not true an error will be thrown. Filenames must also come from the specific
   * directory: they must come from "/Users/sylviewatts/Desktop/CS 320/csv-sylviewatts/data"
   *
   * @param args - Arguments to use when running the program. Format indicated above
   */
  public static void main(String[] args) {
    FileReader fReader = null;
    Boolean hasHeader = false;
    if (args.length < 2 || args.length > 4) {
      System.err.println("Wrong format/wrong number of inputs");
      System.err.println(
          "You should input a String filename, a String searchWord, a String or Integer header (optional), "
              + "and a String hasHeader (optional). hasHeader should be true if the given text has headers");
      return;
    }
    try {
      String filename = args[0];
      if (filename.startsWith("/Users/sylviewatts/Desktop/CS 320/csv-sylviewatts/data")) {
        fReader = new FileReader(filename);
      } else {
        System.err.println("File in the wrong directory");
        return;
      }
    } catch (FileNotFoundException e) {
      System.err.println("File not found");
      return;
    }

    if (args.length == 4) {
      if (args[3].toUpperCase().equals("TRUE")) {
        hasHeader = true;
      }
    }

    CreatorFromRow<List<String>> rowToStringList = new StringListCreateFromRow();
    CSVParser<List<String>> parser = new CSVParser<>(fReader, rowToStringList, hasHeader);
    CSVSearcher searcher = null;

    try {
      searcher = new CSVSearcher(parser);
    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
      return;
    } catch (FactoryFailureException e) {
      System.err.println("Error parsing CSV row: " + e.getMessage());
      return;
    } catch (InconsistentRowException e) {
      System.err.println("Error parsing file: " + e.getMessage());
      return;
    }

    if (args.length >= 3) {
      boolean isNumeric = args[2].chars().allMatch(Character::isDigit);
      if (isNumeric) {
        Integer header = Integer.parseInt(args[2]);
        try {
          System.out.println(searcher.search(args[1], header));
        } catch (NotFoundException e) {
          System.err.println("Error matching header: " + e.getMessage());
          return;
        }
      } else {
        String header = args[2];
        try {
          System.out.println(searcher.search(args[1], header));
        } catch (NotFoundException e) {
          System.err.println("Error matching header: " + e.getMessage());
          return;
        }
      }
      return;
    }

    System.out.println(searcher.search(args[1]));
  }
}
