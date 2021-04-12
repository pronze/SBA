package pronze.hypixelify.api.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor(staticName = "of")
@Data
public class HealthIndicatorData {
    private final UUID playerUUID;
    private double health;
}
