package dev.slava7777.buyer.buyer.category;

import dev.by1337.yaml.decoder.PipelineYamlDecoder;
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;

public class Category {
    private String id;
    private String displayName;

    public Category() {
    }

    public Category(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }

    public void setId(String id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getTitle() {
        return "<lang:dbuyer.categories.%s.title>".formatted(id);
    }

    public String getName() {
        return "<lang:dbuyer.categories.%s.name>".formatted(id);
    }

    public static final YamlDecoder<Category> DECODER = new PipelineYamlDecoder<>(Category::new)
            .field(YamlDecoder.STRING, "id", Category::setId)
            .field(YamlDecoder.STRING, "display_name", Category::setDisplayName)
            .build();
}
