package dev.slava7777.buyer.config;

import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;

import java.util.List;
import java.util.Map;

public record CommandsConf(String buyer_menu, String setting_menu, List<String> aliases, Map<String, String> renamer) {
    public static final YamlDecoder<CommandsConf> DECODER = RecordYamlDecoder.mapOf(
            CommandsConf::new,
            YamlDecoder.STRING.fieldOf("buyer_menu"),
            YamlDecoder.STRING.fieldOf("setting_menu"),
            YamlDecoder.STRINGS.fieldOf("aliases"),
            YamlDecoder.mapOf(YamlDecoder.STRING, YamlDecoder.STRING)
                    .fieldOf("commands_renamer")
    );

    public String rename(String s) {
        return renamer.getOrDefault(s, s);
    }
}

