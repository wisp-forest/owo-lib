package io.wispforest.owo.offline;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

public class AdvancementsTransaction {
    private final UUID player;
    private final Map<Identifier, AdvancementProgress> advancementData;

    AdvancementsTransaction(UUID player, Map<Identifier, AdvancementProgress> advancementData) {
        this.player = player;
        this.advancementData = advancementData;
    }

    public Map<Identifier, AdvancementProgress> getAdvancementData() {
        return advancementData;
    }

    public AdvancementProgress getOrAddProgress(Advancement advancement) {
        return advancementData.computeIfAbsent(advancement.getId(), id -> {
            AdvancementProgress progress = new AdvancementProgress();
            progress.init(advancement.getCriteria(), advancement.getRequirements());
            return progress;
        });
    }

    public void grant(Advancement advancement) {
        AdvancementProgress progress = getOrAddProgress(advancement);
        for (String criterion : progress.getUnobtainedCriteria()) {
            progress.obtain(criterion);
        }
    }

    public void revoke(Advancement advancement) {
        AdvancementProgress progress = getOrAddProgress(advancement);
        for (String criterion : progress.getObtainedCriteria()) {
            progress.reset(criterion);
        }
    }

    public void commit() {
        OfflineAdvancementLookup.save(player, advancementData);
    }
}
