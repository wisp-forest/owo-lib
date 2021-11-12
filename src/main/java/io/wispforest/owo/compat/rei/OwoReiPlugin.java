package io.wispforest.owo.compat.rei;

public class OwoReiPlugin /*implements REIClientPlugin*/ {

    /*@Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(CreativeInventoryScreen.class, screen -> {
            var group = ItemGroup.GROUPS[screen.getSelectedTab()];
            if (!(group instanceof OwoItemGroup owoGroup)) return Collections.emptySet();
            if (owoGroup.getButtons().isEmpty()) return Collections.emptySet();

            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();

            int stackHeight = owoGroup.getStackHeight();
            if (stackHeight > 4) y -= 15 * (stackHeight - 4);

            return Collections.singleton(new Rectangle(x + 200, y + 15, 28 + (28 * owoGroup.getButtons().size() / stackHeight), 25 * Math.min(owoGroup.getButtons().size(), owoGroup.getStackHeight())));
        });
    }*/
}
