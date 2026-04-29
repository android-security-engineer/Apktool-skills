package brut.androlib.search;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private String query;
    private String type;
    private int totalMatches;
    private List<SearchMatch> matches = new ArrayList<>();

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getTotalMatches() { return totalMatches; }
    public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }
    public List<SearchMatch> getMatches() { return matches; }
    public void setMatches(List<SearchMatch> matches) { this.matches = matches; }

    public static class SearchMatch {
        private String name;
        private String value;
        private String source;

        public SearchMatch() {}

        public SearchMatch(String name, String value, String source) {
            this.name = name;
            this.value = value;
            this.source = source;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
