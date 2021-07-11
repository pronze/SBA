package io.github.pronze.sba.utils;

import io.github.pronze.sba.data.DegradableItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ShopUtilTest {

    @BeforeAll
    public static void mockMode() {
        Logger.mockMode();
    }

    @Test
    public void downgradeTools() {
        var sword = new ItemStack(Material.WOODEN_SWORD);
        var downgradedItem = ShopUtil.downgradeItem(sword, DegradableItem.WEAPONARY);
        assertTrue(downgradedItem.isSimilar(sword));

        sword = new ItemStack(Material.STONE_SWORD);
        downgradedItem = ShopUtil.downgradeItem(sword, DegradableItem.WEAPONARY);
        assertSame(Material.WOODEN_SWORD, downgradedItem.getType());

        sword = new ItemStack(Material.IRON_SWORD);
        downgradedItem = ShopUtil.downgradeItem(sword, DegradableItem.WEAPONARY);
        assertSame(Material.GOLDEN_SWORD, downgradedItem.getType());

        var axe = new ItemStack(Material.WOODEN_AXE);
        downgradedItem = ShopUtil.downgradeItem(axe, DegradableItem.TOOLS);
        assertSame(Material.WOODEN_AXE, downgradedItem.getType());

        axe = new ItemStack(Material.DIAMOND_AXE);
        downgradedItem = ShopUtil.downgradeItem(axe, DegradableItem.TOOLS);
        assertSame(Material.IRON_AXE, downgradedItem.getType());
    }

    @Test
    public void getLevelFromMaterialName() {
        assertSame(0, ShopUtil.getLevelFromMaterialName("WOODEN_AXE", ShopUtil.orderOfTools));
        assertSame(4, ShopUtil.getLevelFromMaterialName("DIAMOND_PICKAXE", ShopUtil.orderOfTools));
        assertSame(4, ShopUtil.getLevelFromMaterialName("DIAMOND", ShopUtil.orderOfTools));
    }
}
