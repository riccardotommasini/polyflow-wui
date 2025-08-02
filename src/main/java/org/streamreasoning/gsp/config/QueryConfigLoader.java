package org.streamreasoning.gsp.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class QueryConfigLoader {

    public static QueryConfig load(String yamlFileName) {
        Yaml yaml = new Yaml(new Constructor(QueryConfig.class, new LoaderOptions()));
        try (InputStream in = QueryConfigLoader.class.getClassLoader().getResourceAsStream(yamlFileName)) {
            if (in == null) {
                throw new IllegalArgumentException("YAML file not found: " + yamlFileName);
            }
            return yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML config: " + yamlFileName, e);
        }
    }
}

