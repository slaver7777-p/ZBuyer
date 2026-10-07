package dev.slava7777.buyer.buyer;

import dev.by1337.yaml.BukkitCodecs;
import dev.by1337.yaml.codec.YamlCodec;
import dev.by1337.yaml.decoder.PipelineYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.slava7777.buyer.util.EconomyUtil;
import org.bukkit.Material;

import java.util.EnumSet;

public class BuyerItem {

    public int id;
    public Type type;
    public String category;
    public EnumSet<Material> materials = EnumSet.noneOf(Material.class);
    public Material[] materialsArray = new Material[0];
    public float points;
    public float money;
    public transient String cachedMoney;
    public transient String cachedPoints;

    public enum Type {
        SINGLE, GROUPED
    }

    public static final YamlDecoder<BuyerItem> DECODER = new PipelineYamlDecoder<>(BuyerItem::new)
            .field(YamlDecoder.INT, "id", (i, v) -> i.id = v)
            .field(YamlDecoder.STRING, "type", (i, v) -> i.type = Type.valueOf(v.toUpperCase()))
            .field(YamlDecoder.STRING, "category", (i, v) -> i.category = v)
            .field(BukkitCodecs.material().asDecoder().listOf(), "materials", (i, v) -> {
                if (v == null || v.isEmpty()) return;
                i.materials.clear();
                i.materials.addAll(v);
                i.materialsArray = i.materials.toArray(new Material[0]);
            })
            .field(YamlDecoder.FLOAT, "points", (i, v) -> i.points = v)
            .field(YamlDecoder.FLOAT, "money", (i, v) -> i.money = v)
            .build();

    public static final YamlCodec<BuyerItem> CODEC = YamlCodec.of(
            DECODER::decode,
            (item) -> { throw new UnsupportedOperationException("Encoding not supported"); }
    );

    public boolean isSingle() {
        return type == Type.SINGLE;
    }

    public boolean isGrouped() {
        return type == Type.GROUPED;
    }

    public int id() {
        return id;
    }

    public boolean matches(Material material) {
        return materials.contains(material);
    }

    public void bake() {
        this.cachedMoney = EconomyUtil.format(money);
        this.cachedPoints = EconomyUtil.format(points);
    }
}
