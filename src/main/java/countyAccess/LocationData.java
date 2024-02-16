package countyAccess;

public record LocationData(String state, String county) {
  /**
   * Convenience function to convert this location to an API parameter string
   * @return API parameter string corresponding to this location
   */
  public String toOurServerParams() {
    return "state=" + state + "&county=" + county;
  }
}
