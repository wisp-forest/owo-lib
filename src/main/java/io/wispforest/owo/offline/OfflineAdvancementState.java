package io.wispforest.owo.offline;

import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;

/**
 * A utility class that allows easy
 * modification of the advancement data
 * of offline players
 *
 * @author BasiqueEvangelist
 */
public class OfflineAdvancementState {

    private final Map<Identifier, AdvancementProgress> advancementData;

    OfflineAdvancementState(Map<Identifier, AdvancementProgress> advancementData) {
        this.advancementData = advancementData;
    }

    /**
     * @return The raw data this state holds
     */
    public Map<Identifier, AdvancementProgress> advancementData() {
        return advancementData;
    }

    /**
     * Either obtains or creates an entry for
     * the given advancement
     *
     * @param advancement The target advancement
     * @return The progress of the targeted advancement
     */
    public AdvancementProgress getOrAddProgress(AdvancementHolder advancement) {
        return advancementData.computeIfAbsent(advancement.id(), id -> {
            AdvancementProgress progress = new AdvancementProgress();
            progress.update(advancement.value().requirements());
            return progress;
        });
    }

    /**
     * Grants the given advancement to the player
     * this state refers to
     *
     * @param advancement The advancement to grant
     */
    public void grant(AdvancementHolder advancement) {
        AdvancementProgress progress = getOrAddProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            progress.grantProgress(criterion);
        }
    }

    /**
     * Revokes the given advancement from the player
     * this state refers to
     *
     * @param advancement The advancement to revoke
     */
    public void revoke(AdvancementHolder advancement) {
        AdvancementProgress progress = getOrAddProgress(advancement);
        for (String criterion : progress.getCompletedCriteria()) {
            progress.revokeProgress(criterion);
        }
    }
}
