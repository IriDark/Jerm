package github.iri.jem.jei;

import github.iri.jem.*;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.*;
import mezz.jei.api.recipe.vanilla.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

import java.util.*;
import java.util.stream.*;

@JeiPlugin
public class JeiPlugin implements IModPlugin{
    private static final ResourceLocation JEI = new ResourceLocation(Jerm.MODID, "jei_plugin");

    private static Stream<IJeiAnvilRecipe> getRepairRecipes(
    RepairData repairData,
    IVanillaRecipeFactory vanillaRecipeFactory,
    IIngredientHelper<ItemStack> ingredientHelper
    ) {
        Ingredient repairIngredient = repairData.getRepairIngredient();
        List<ItemStack> repairables = repairData.getRepairables();

        List<ItemStack> repairMaterials = List.of(repairIngredient.getItems());

        return repairables.stream()
        .mapMulti((itemStack, consumer) -> {
            String uid = EnchantedBookSubtypeInterpreter.INSTANCE.getStringName(itemStack);
            String ingredientIdPath = ResourceLocationUtil.sanitizePath(uid);
            String itemModId = ingredientHelper.getResourceLocation(itemStack).getNamespace();

            ItemStack damagedThreeQuarters = itemStack.copy();
            damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
            ItemStack damagedHalf = itemStack.copy();
            damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

            var damagedThreeQuartersSingletonList = List.of(damagedThreeQuarters);

            IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
            damagedThreeQuartersSingletonList,
            damagedThreeQuartersSingletonList,
            List.of(damagedHalf),
            ResourceLocation.fromNamespaceAndPath(itemModId, "self_repair." + ingredientIdPath)
            );
            consumer.accept(repairWithSame);

            if (!repairMaterials.isEmpty()) {
                ItemStack damagedFully = itemStack.copy();
                damagedFully.setDamageValue(damagedFully.getMaxDamage());
                IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(
                List.of(damagedFully),
                repairMaterials,
                damagedThreeQuartersSingletonList,
                ResourceLocation.fromNamespaceAndPath(itemModId, "materials_repair." + ingredientIdPath)
                );
                consumer.accept(repairWithMaterial);
            }
        });
    }
    @Override
    public ResourceLocation getPluginUid(){
        return JEI;
    }
}
