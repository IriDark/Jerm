package github.iri.jem.jei;

import github.iri.jem.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.*;
import mezz.jei.api.ingredients.*;
import mezz.jei.api.ingredients.subtypes.*;
import mezz.jei.api.recipe.vanilla.*;
import mezz.jei.api.registration.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.registries.*;

import java.util.*;
import java.util.stream.*;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin{
    private static final ResourceLocation JEI = new ResourceLocation(Jerm.MODID, "jei_plugin");

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var manager = registration.getIngredientManager();
        var factory = registration.getVanillaRecipeFactory();
        registration.addRecipes(RecipeTypes.ANVIL, getRepairRecipes(factory, manager.getIngredientHelper(VanillaTypes.ITEM_STACK)).toList());
    }

    private static class RepairData {
        private final Ingredient repairIngredient;
        private final List<ItemStack> repairables;

        public RepairData(Ingredient repairIngredient, ItemStack... repairables) {
            this.repairIngredient = repairIngredient;
            this.repairables = List.of(repairables);
        }

        public Ingredient getRepairIngredient() {
            return repairIngredient;
        }

        public List<ItemStack> getRepairables() {
            return repairables;
        }
    }

    private static Stream<RepairData> getRepairData() {
        return ForgeRegistries.ITEMS.getValues().stream()
        .filter(Item::canBeDepleted)
        .filter(item -> !ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("minecraft"))
        .map(item -> {
            ItemStack stack = new ItemStack(item);
            Item repairItem = item.getCraftingRemainingItem();
            if(item instanceof ArmorItem armorItem) {
                Ingredient repairIngredient = armorItem.getMaterial().getRepairIngredient();
                if(repairIngredient == Ingredient.EMPTY || repairIngredient.getItems().length == 0) return null;
                return new RepairData(repairIngredient, stack);
            }

            if(item instanceof TieredItem tieredItem){
                Ingredient repairIngredient = tieredItem.getTier().getRepairIngredient();
                if(repairIngredient == Ingredient.EMPTY || repairIngredient.getItems().length == 0) return null;
                return new RepairData(repairIngredient, stack);
            }

            return null;
        })
        .filter(Objects::nonNull);
    }

    //copy of JEI
    private static Stream<IJeiAnvilRecipe> getRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientHelper<ItemStack> ingredientHelper) {
        return getRepairData().flatMap(repairData -> getRepairRecipes(repairData, vanillaRecipeFactory, ingredientHelper));
    }

    private static Stream<IJeiAnvilRecipe> getRepairRecipes(RepairData repairData, IVanillaRecipeFactory vanillaRecipeFactory, IIngredientHelper<ItemStack> ingredientHelper) {
        Ingredient repairIngredient = repairData.getRepairIngredient();
        List<ItemStack> repairables = repairData.getRepairables();
        List<ItemStack> repairMaterials = List.of(repairIngredient.getItems());

        return repairables.stream().mapMulti((itemStack, consumer) -> {
            String ingredientIdPath = ResourceLocationUtil.sanitizePath(ingredientHelper.getUniqueId(itemStack, UidContext.Recipe));
            String itemModId = ingredientHelper.getResourceLocation(itemStack).getNamespace();

            ItemStack damagedThreeQuarters = itemStack.copy();
            damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
            ItemStack damagedHalf = itemStack.copy();
            damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

            var damagedThreeQuartersSingletonList = List.of(damagedThreeQuarters);

            IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(damagedThreeQuartersSingletonList, damagedThreeQuartersSingletonList, List.of(damagedHalf), new ResourceLocation(itemModId, "self_repair." + ingredientIdPath));
            consumer.accept(repairWithSame);
            if (!repairMaterials.isEmpty()) {
                ItemStack damagedFully = itemStack.copy();
                damagedFully.setDamageValue(damagedFully.getMaxDamage());
                IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedFully), repairMaterials, damagedThreeQuartersSingletonList, new ResourceLocation(itemModId, "materials_repair." + ingredientIdPath));
                consumer.accept(repairWithMaterial);
            }
        });
    }

    @Override
    public ResourceLocation getPluginUid(){
        return JEI;
    }
}
