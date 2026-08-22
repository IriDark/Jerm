package github.iri.jerm.jei;

import github.iri.jerm.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.*;
import mezz.jei.api.ingredients.subtypes.*;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.*;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.*;

import java.util.*;
import java.util.stream.*;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin{
    private static final ResourceLocation JEI = new ResourceLocation(Jerm.MODID, "jei_plugin");
    public static final RecipeType<IJeiAnvilRecipe> JERM_REPAIR = RecipeType.create(Jerm.MODID, "jerm_repair", IJeiAnvilRecipe.class);

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JERM_REPAIR, getRepairRecipes(registration).toList());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JermRepairCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Blocks.ANVIL), JERM_REPAIR);
    }

    public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return -1;
        Inventory fakeInventory = new Inventory(player);
        try {
            AnvilMenu repair = new AnvilMenu(0, fakeInventory);
            repair.slots.get(0).set(leftStack);
            repair.slots.get(1).set(rightStack);
            return repair.getCost();
        } catch (RuntimeException e) {
            return -1;
        }
    }

    private static Stream<RepairData> getRepairData() {
        return ForgeRegistries.ITEMS.getValues().stream()
        .filter(Item::canBeDepleted)
        .map(item -> {
            ItemStack stack = new ItemStack(item);
            if(item instanceof ArmorItem armorItem) {
                Ingredient repairIngredient = armorItem.getMaterial().getRepairIngredient();
                if(isRepairIngredientNull(repairIngredient)) return null;
                return new RepairData(repairIngredient, stack);
            }

            if(item instanceof TieredItem tieredItem){
                Ingredient repairIngredient = tieredItem.getTier().getRepairIngredient();
                if(isRepairIngredientNull(repairIngredient)) return null;
                return new RepairData(repairIngredient, stack);
            }

            return null;
        }).filter(Objects::nonNull);
    }

    private static boolean isRepairIngredientNull(Ingredient repairIngredient) {
        return repairIngredient == null || repairIngredient.isEmpty();
    }

    private static Stream<IJeiAnvilRecipe> getRepairRecipes(IRecipeRegistration registration) {
        return getRepairData().flatMap(repairData -> getRepairRecipes(registration, repairData));
    }

    private static Stream<IJeiAnvilRecipe> getRepairRecipes(IRecipeRegistration registration, RepairData repairData) {
        Ingredient repairIngredient = repairData.repairIngredient();
        List<ItemStack> repairables = repairData.repairables();
        List<ItemStack> repairMaterials = List.of(repairIngredient.getItems());
        var vanillaRecipeFactory = registration.getVanillaRecipeFactory();
        return repairables.stream().mapMulti((itemStack, consumer) -> {
            ItemStack damagedThreeQuarters = itemStack.copy();
            damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
            ItemStack damagedHalf = itemStack.copy();
            damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

            var damagedThreeQuartersSingletonList = List.of(damagedThreeQuarters);

            IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(damagedThreeQuartersSingletonList, damagedThreeQuartersSingletonList, List.of(damagedHalf));
            consumer.accept(repairWithSame);
            if (!repairMaterials.isEmpty()) {
                ItemStack damagedFully = itemStack.copy();
                damagedFully.setDamageValue(damagedFully.getMaxDamage());
                IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedFully), repairMaterials, damagedThreeQuartersSingletonList);
                consumer.accept(repairWithMaterial);
            }

            ItemStack renamedItem = itemStack.copy();
            renamedItem.setHoverName(Component.literal("Amazing ").append(itemStack.getHoverName()));

            IJeiAnvilRecipe renameRecipe = vanillaRecipeFactory.createAnvilRecipe(
                    List.of(itemStack.copy()),
                    List.of(new ItemStack(Items.NAME_TAG)),
                    List.of(renamedItem)
            );
            consumer.accept(renameRecipe);
        });
    }

    @Override
    public ResourceLocation getPluginUid(){
        return JEI;
    }
}
