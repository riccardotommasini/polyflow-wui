package org.streamreasoning.gsp.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryConfig {
    private String defaultQueryName;
    private Map<String, QueryDefinition> queries;

    public String getDefaultQueryName() {
        return defaultQueryName;
    }

    public void setDefaultQueryName(String defaultQueryName) {
        this.defaultQueryName = defaultQueryName;
    }

    public Map<String, QueryDefinition> getQueries() {
        return queries;
    }

    public void setQueries(Map<String, QueryDefinition> queries) {
        this.queries = queries;
    }

    public QueryDefinition getQueryByName(String name) {
        return queries != null ? queries.get(name) : null;
    }

    public QueryDefinition getDefaultQuery() {
        return getQueryByName(defaultQueryName);
    }

    public Set<String> getAllQueryNames() {
        return queries != null ? queries.keySet() : Set.of();
    }

    public static class QueryDefinition {
        private String input_stream;
        private List<String> labels;
        private String query_template;

        public String getInput_stream() {
            return input_stream;
        }

        public void setInput_stream(String input_stream) {
            this.input_stream = input_stream;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public String getQuery_template() {
            return query_template;
        }

        public void setQuery_template(String query_template) {
            this.query_template = query_template;
        }
    }
}
