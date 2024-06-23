package dev.dfonline.codeclient.dev.overlay;

import com.google.common.collect.Lists;
import dev.dfonline.codeclient.CodeClient;
import dev.dfonline.codeclient.config.Config;
import dev.dfonline.codeclient.dev.menu.customchest.CustomChestMenu;
import dev.dfonline.codeclient.hypercube.ReferenceBook;
import dev.dfonline.codeclient.hypercube.actiondump.Action;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.location.Dev;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Iterator;
import java.util.List;

public class ActionViewer {
    private static Action action = null;
    private static ReferenceBook book = null;

    private static int scroll = 0;
    private static boolean tall = false;

    public static void invalidate() {
        action = null;
        book = null;
        scroll = 0;
        tall = false;
    }
    public static boolean isValid() {
        return action != null || book != null;
    }

    public static void onClickChest(BlockHitResult hitResult) {
        invalidate();
        var world = CodeClient.MC.world;
        if (world == null || !Config.getConfig().ActionViewer) return;
        var position = hitResult.getBlockPos();
        if (CodeClient.location instanceof Dev dev && dev.isInDev(position) && world.getBlockEntity(position) instanceof ChestBlockEntity) {
            var signEntity = world.getBlockEntity(position.down().west());
            if (signEntity instanceof SignBlockEntity sign) {
                try {
                    action = ActionDump.getActionDump().findAction(sign.getFrontText());
                } catch (Exception ignored) {

                }
            }
        }
    }

    public static List<Text> getOverlayText() {
        if (CodeClient.location instanceof Dev dev) {
            if (!Config.getConfig().ActionViewer) return null;
            if (book != null) return book.getTooltip();
            if (action == null) {
                var book = dev.getReferenceBook();
                if (book == null || book.isEmpty()) return null;
                ActionViewer.book = book;
                return book.getTooltip();
            }
            var item = action.icon.getItem();
            return item.getTooltip(null, TooltipContext.BASIC);
        }
        return null;
    }

    public static void render(DrawContext context, int mouseX, int mouseY, HandledScreen<?> handledScreen) {
        if (!(handledScreen instanceof GenericContainerScreen || handledScreen instanceof CustomChestMenu))
            return;
        var text = getOverlayText();
        if (text == null) return;

        ActionTooltipPositioner.INSTANCE.setMousePosition(mouseX,mouseY); // I would pass these through like normal, but draw tooltip might be moved to a utility class in the future.

        drawTooltip(context, handledScreen, transformText(text), 176, scroll, 300);
    }

    public static boolean scroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!(screen instanceof GenericContainerScreen || screen instanceof CustomChestMenu)) return false;
        if (isValid() && tall) {
            scroll -= 8 * (int) verticalAmount;
            return true;
        }
        return false;
    }

    private static List<TooltipComponent> transformText(List<Text> texts) {
        var orderedText = Lists.transform(texts, Text::asOrderedText);
        return  orderedText.stream().map(TooltipComponent::of).toList();
    }

    // this implementation of draw tooltip allows a change in the z-index of the rendered tooltip.
    private static void drawTooltip(DrawContext context, HandledScreen<?> handledScreen, List<TooltipComponent> components, int x, int y, int z) {
        var textRenderer = CodeClient.MC.textRenderer;

        var positioner = ActionTooltipPositioner.INSTANCE;
        if (handledScreen instanceof CustomChestMenu menu) {
            var handler = menu.getScreenHandler();
            positioner.setBackgroundHeight(handler.numbers.MENU_HEIGHT);
            positioner.setBackgroundWidth(handler.numbers.MENU_WIDTH);
            x += handler.numbers.MENU_WIDTH - 176;
        } else {
            positioner.setBackgroundHeight(166);
            positioner.setBackgroundWidth(176);
        }

        if (components.isEmpty()) return;
        int tooltipWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;

        TooltipComponent tooltipComponent;
        for (Iterator<TooltipComponent> iterator = components.iterator(); iterator.hasNext(); tooltipHeight += tooltipComponent.getHeight()) {
            tooltipComponent = iterator.next();
            int width = tooltipComponent.getWidth(textRenderer);
            if (width > tooltipWidth) {
                tooltipWidth = width;
            }
        }
        Vector2ic vector = positioner.getPosition(context.getScaledWindowWidth(), context.getScaledWindowHeight(), x, y, tooltipWidth, tooltipHeight);
        context.getMatrices().push();

        var finalWidth = tooltipWidth;
        var finalHeight = tooltipHeight;
        context.draw(() -> TooltipBackgroundRenderer.render(context, vector.x(), vector.y(), finalWidth, finalHeight, z));
        context.getMatrices().translate(0.0F, 0.0F, (float) z);

        int textY = vector.y();
        for (int index = 0; index < components.size(); ++index) {
            tooltipComponent = components.get(index);
            tooltipComponent.drawText(textRenderer, vector.x(), textY, context.getMatrices().peek().getPositionMatrix(), context.getVertexConsumers());
            textY += tooltipComponent.getHeight() + (index == 0 ? 2 : 0);
        }

        context.getMatrices().pop();
    }

    private static class ActionTooltipPositioner implements TooltipPositioner {
        public static final ActionTooltipPositioner INSTANCE = new ActionTooltipPositioner();
        private int backgroundHeight = 166;
        private int backgroundWidth = 176;
        private int mouseX = 0, mouseY = 0;

        private ActionTooltipPositioner() {
        }

        public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
            var vector = new Vector2i(x, y);

            // can scroll or not.
            if (screenHeight < height + 24) tall = true;

            var difference = (height - backgroundHeight) / 2;
            vector.add(6, -difference); // align to chest

            int space = ((screenWidth - backgroundWidth) / 2) - 6 /* for padding */;
            if (width + 6 > space) {
                if (isMouseInside(x+6, y-difference, width, height, screenWidth, screenHeight)) {
                    vector.add(space-width - 5, 0);
                }
            }

            return vector;
        }

        private boolean isMouseInside(int x, int y, int width, int height, int screenWidth, int screenHeight) {
            // positioning changes
            x += (screenWidth - backgroundWidth) / 2;
            y += (screenHeight - backgroundHeight) / 2;

            // tooltip padding changes
            x -= 4;
            y -= 5;
            width += 6;
            height += 6;

            // check in range
            if (x <= mouseX && mouseX <= x+width) {
                if (y <= mouseY && mouseY <= y+height) {
                    return true;
                };
            }
            return false;
        }

        public void setBackgroundHeight(int backgroundHeight) {
            this.backgroundHeight = backgroundHeight;
        }
        public void setBackgroundWidth(int backgroundWidth) {
            this.backgroundWidth = backgroundWidth;
        }

        public void setMousePosition(int x, int y) {
            mouseX = x;
            mouseY = y;
        }
    }
}
