package github.iri.jerm.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record RepairData(Ingredient repairIngredient, List<ItemStack> repairables) {
    public RepairData(Ingredient repairIngredient, ItemStack... repairables) {
        this(repairIngredient, List.of(repairables));
    }
}