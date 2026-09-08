package dev.slava7777.buyer.config;

import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandsConf {
    public static final YamlDecoder<CommandsConf> DECODER = RecordYamlDecoder.mapOf(
            CommandsConf::new,
            YamlDecoder.STRING.fieldOf("buyer_menu"),
            YamlDecoder.STRING.fieldOf("setting_menu"),
            YamlDecoder.STRINGS.fieldOf("aliases"),
            YamlDecoder.mapOf(YamlDecoder.STRING, YamlDecoder.STRING)
                    .fieldOf("commands_renamer")
    );
    public final String buyer_menu;
    public final String setting_menu;
    public final List<String> aliases;
    public final Map<String, String> renamer;
    public CommandsConf(String ahMenu, String settingMenu, List<String> aliases, Map<String, String> renamer) {
        buyer_menu = ahMenu;
        setting_menu = settingMenu;
        this.aliases = aliases;
        this.renamer = Collections.unmodifiableMap(renamer);
    }
    public String rename(String s){
        return renamer.getOrDefault(s, s);
    }
}

