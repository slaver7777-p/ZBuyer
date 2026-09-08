package dev.slava7777.buyer.buyer;

import dev.by1337.yaml.decoder.PipelineYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;

public class BuyerMultiplier {

    public static final YamlDecoder<BuyerMultiplier> DECODER = new PipelineYamlDecoder<>(BuyerMultiplier::new)
            .field(YamlDecoder.FLOAT, "start-multiplier", (c, v) -> c.start = v)
            .field(YamlDecoder.FLOAT, "points-per-level", (c, v) -> c.pointsPerLevel = v)
            .field(YamlDecoder.FLOAT, "increase-per-level", (c, v) -> c.increasePerLevel = v)
            .field(YamlDecoder.FLOAT, "max-multiplier", (c, v) -> c.max = v)
            .build();

    private float start;
    private double pointsPerLevel;
    private  float increasePerLevel;
    private float max;

    public float calculate(double totalPoints) {
        if (pointsPerLevel <= 0) return start;
        int levels = (int) (totalPoints / pointsPerLevel);
        float result = start + levels * increasePerLevel;
        if (result > max) {
            result = max;
        }
        return result;
    }
}
