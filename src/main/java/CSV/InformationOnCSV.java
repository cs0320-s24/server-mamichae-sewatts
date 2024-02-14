package CSV;

import java.util.List;

public class InformationOnCSV {
    private List<List<String>> parsedText;
    private List<String> header;

    private Boolean loaded;

    public InformationOnCSV() {
        this.loaded = false;
    }

    public void setParsedText(List<List<String>> text) {
        this.parsedText = text;
    }

    public void setHeaders(List<String> header) {
        this.header = header;
    }

    //or just make it true?
    public void setLoaded(Boolean loaded) {
        this.loaded = loaded;
    }

    public Boolean getLoaded() {
        return this.loaded;
    }

    public List<String> getHeader() {
        return this.header;
    }

    public List<List<String>> getParsedText(){
        return this.parsedText;
    }
}
