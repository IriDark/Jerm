package github.iri.jerm.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class JermRepairCategory implements IRecipeCategory<IJeiAnvilRecipe> {
    private static final String leftSlotName = "leftSlot";
    private static final String rightSlotName = "rightSlot";

    private final IDrawable background;
    private final IDrawable icon;

    public JermRepairCategory(IGuiHelper guiHelper) {
        var guiVanilla = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
        background = guiHelper.drawableBuilder(guiVanilla, 0, 168, 125, 18)
                .addPadding(0, 20, 0, 0)
                .build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.ANVIL));
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return ModJeiPlugin.JERM_REPAIR.getUid();
    }

    @Override
    public RecipeType<IJeiAnvilRecipe> getRecipeType() {
        return ModJeiPlugin.JERM_REPAIR;
    }

    @SuppressWarnings("removal")
    @Override
    public Class<? extends IJeiAnvilRecipe> getRecipeClass() {
        return IJeiAnvilRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("container.repair");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IJeiAnvilRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> leftInputs = recipe.getLeftInputs();
        List<ItemStack> rightInputs = recipe.getRightInputs();
        List<ItemStack> outputs = recipe.getOutputs();

        IRecipeSlotBuilder leftInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(leftInputs)
                .setSlotName(leftSlotName);

        IRecipeSlotBuilder rightInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 50, 1)
                .addItemStacks(rightInputs)
                .setSlotName(rightSlotName);

        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1)
                .addItemStacks(outputs);

        if (leftInputs.size() == rightInputs.size()) {
            if (leftInputs.size() == outputs.size()) {
                builder.createFocusLink(leftInputSlot, rightInputSlot, outputSlot);
            }
        } else if (leftInputs.size() == outputs.size() && rightInputs.size() == 1) {
            builder.createFocusLink(leftInputSlot, outputSlot);
        } else if (rightInputs.size() == outputs.size() && leftInputs.size() == 1) {
            builder.createFocusLink(rightInputSlot, outputSlot);
        }
    }

    @Override
    public void draw(IJeiAnvilRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        Integer cost = getCost(recipeSlotsView);
        if (cost != null) {
            String costText = cost < 0 ? "err" : Integer.toString(cost);
            String text = I18n.get("container.repair.cost", costText);

            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            int mainColor = playerHasEnoughLevels(player, cost) ? 0xFF80FF20 : 0xFFFF6060;
            drawRepairCost(minecraft, poseStack, text, mainColor);
        }
    }

    private @Nullable Integer getCost(IRecipeSlotsView recipeSlotsView) {
        Optional<ItemStack> leftStack = recipeSlotsView.findSlotByName(leftSlotName)
                .flatMap(slot -> slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK));

        Optional<ItemStack> rightStack = recipeSlotsView.findSlotByName(rightSlotName)
                .flatMap(slot -> slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK));

        if (leftStack.isEmpty() || rightStack.isEmpty()) {
            return null;
        }

        return ModJeiPlugin.findLevelsCost(leftStack.get(), rightStack.get());
    }

    private static boolean playerHasEnoughLevels(@Nullable LocalPlayer player, int cost) {
        if (player == null) {
            return true;
        }
        if (player.isCreative()) {
            return true;
        }
        return cost < 40 && cost <= player.experienceLevel;
    }

    private void drawRepairCost(Minecraft minecraft, PoseStack poseStack, String text, int mainColor) {
        int width = minecraft.font.width(text);
        int x = getBackground().getWidth() - 2 - width;
        int y = 27;
        minecraft.font.drawShadow(poseStack, text, x, y, mainColor);
    }
}