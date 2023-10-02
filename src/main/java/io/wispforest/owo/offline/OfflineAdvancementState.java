package io.wispforest.owo.offline;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.util.Identifier;

import java.util.Map;

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
    public AdvancementProgress getOrAddProgress(AdvancementEntry advancement) {
        return advancementData.computeIfAbsent(advancement.id(), id -> {
            AdvancementProgress progress = new AdvancementProgress();
            progress.init(advancement.value().requirements());
            return progress;
        });
    }

    /**
     * Grants the given advancement to the player
     * this state refers to
     *
     * @param advancement The advancement to grant
     */
    public void grant(AdvancementEntry advancement) {
        AdvancementProgress progress = getOrAddProgress(advancement);
        for (String criterion : progress.getUnobtainedCriteria()) {
            progress.obtain(criterion);
        }
    }

    /**
     * Revokes the given advancement from the player
     * this state refers to
     *
     * @param advancement The advancement to revoke
     */
    public void revoke(AdvancementEntry advancement) {
        AdvancementProgress progress = getOrAddProgress(advancement);
        for (String criterion : progress.getObtainedCriteria()) {
            progress.reset(criterion);
        }
    }
}
