package io.github.codeutilities.mod.mixin.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.codeutilities.sys.sidedchat.ChatRule;
import io.github.codeutilities.sys.util.SoundUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.DrawableHelper.fill;

@Mixin(ChatHud.class)
public abstract class MSideChatHUD {
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private MinecraftClient client;
    @Shadow        private int scrolledLines;
    @Shadow @Final private List<ChatHudLine<OrderedText>> visibleMessages;
    @Shadow @Final private Deque<Text> messageQueue;
    @Shadow        private boolean hasUnreadNewMessages;

    @Shadow protected abstract boolean isChatFocused();
    @Shadow public abstract double getChatScale();
    @Shadow public abstract int getVisibleLineCount();
    @Shadow public abstract int getWidth();
    @Shadow protected abstract void processMessageQueue();

    @Shadow public static int getWidth(double widthOption) { return 0; }
    @Shadow private static double getMessageOpacityMultiplier(int age) { return 0; }

    @Shadow protected abstract void removeMessage(int messageId);

    private final List<ChatHudLine<OrderedText>> sideVisibleMessages = Lists.newArrayList();

    @Inject(method = "render",at = @At("HEAD"), cancellable = true)
    private void render(MatrixStack matrices, int tickDelta, CallbackInfo ci) {
        this.processMessageQueue();
        int renderedLines = renderChat(matrices,tickDelta,visibleMessages,0,0);
        renderChat(matrices,tickDelta,sideVisibleMessages,0,0);
        renderOthers(matrices,renderedLines);
        ci.cancel();
    }

    /**
     * Renders a chat box, drawn into its own function so I don't repeat code for side chat
     * Most params are just stuff the code needs and I don't have the confidence to change
     * @param displayX X to display at
     * @param displayY Y to display at
     * @return The amount of lines actually rendered. Other parts of rendering need to know this
     */
    @SuppressWarnings("deprecation")
    private int renderChat(MatrixStack matrices, int tickDelta, List<ChatHudLine<OrderedText>> visibleMessages, int displayX, int displayY) {
        // will apologise - most code is taken from deobfuscated minecraft jar
        // have attempted to make it as readable as possible but some lines idk man no clue
        int visibleLineCount = this.getVisibleLineCount();
        int visibleMessagesSize = visibleMessages.size();
        int renderedLines = 0;
        if (visibleMessagesSize > 0) {
            boolean chatFocused = this.isChatFocused();

            double d = this.getChatScale();
            int k = MathHelper.ceil((double) this.getWidth() / d);
            RenderSystem.pushMatrix();
            RenderSystem.translatef(2.0F, 8.0F, 0.0F);
            RenderSystem.scaled(d, d, 1.0D);
            double opacity = this.client.options.chatOpacity * 0.8999999761581421D + 0.10000000149011612D;
            double backgroundOpacity = this.client.options.textBackgroundOpacity;
            double lineSpacing = 9.0D * (this.client.options.chatLineSpacing + 1.0D);
            double lineSpacing2 = -8.0D * (this.client.options.chatLineSpacing + 1.0D) + 4.0D * this.client.options.chatLineSpacing;

            for (int i = 0; i + this.scrolledLines < visibleMessages.size() && i < visibleLineCount; ++i) {
                ChatHudLine<OrderedText> chatHudLine = visibleMessages.get(i + this.scrolledLines);
                if (chatHudLine != null) {
                    int ticksSinceCreation = tickDelta - chatHudLine.getCreationTick();
                    if (ticksSinceCreation < 200 || chatFocused) {
                        double o = chatFocused ? 1.0D : getMessageOpacityMultiplier(ticksSinceCreation);
                        int aa = (int) (255.0D * o * opacity);
                        int ab = (int) (255.0D * o * backgroundOpacity);
                        ++renderedLines;
                        if (aa > 3) {
                            double s = (double) (-i) * lineSpacing;
                            matrices.push();
                            matrices.translate(0.0D, 0.0D, 50.0D);
                            fill(matrices, -2, (int) (s - lineSpacing), k + 4, (int) s, ab << 24);
                            RenderSystem.enableBlend();
                            matrices.translate(0.0D, 0.0D, 50.0D);
                            this.client.textRenderer.drawWithShadow(matrices, chatHudLine.getText(), 0.0F, (float) ((int) (s + lineSpacing2)), 16777215 + (aa << 24));
                            RenderSystem.disableAlphaTest();
                            RenderSystem.disableBlend();
                            matrices.pop();
                        }
                    }
                }
            }

            RenderSystem.popMatrix();
        }
        return renderedLines;
    }

    @SuppressWarnings("deprecation")
    private void renderOthers(MatrixStack matrices, int renderedLines) {
        int visibleMessagesSize = this.visibleMessages.size();
        if (visibleMessagesSize == 0) return;
        boolean chatFocused = this.isChatFocused();

        double chatScale = this.getChatScale();
        int k = MathHelper.ceil((double)this.getWidth() / chatScale);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(2.0F, 8.0F, 0.0F);
        RenderSystem.scaled(chatScale, chatScale, 1.0D);
        double opacity = this.client.options.chatOpacity * 0.8999999761581421D + 0.10000000149011612D;
        double backgroundOpacity = this.client.options.textBackgroundOpacity;

        if (!this.messageQueue.isEmpty()) {
            int m = (int)(128.0D * opacity);
            int w = (int)(255.0D * backgroundOpacity);
            matrices.push();
            matrices.translate(0.0D, 0.0D, 50.0D);
            fill(matrices, -2, 0, k + 4, 9, w << 24);
            RenderSystem.enableBlend();
            matrices.translate(0.0D, 0.0D, 50.0D);
            this.client.textRenderer.drawWithShadow(matrices, new TranslatableText("chat.queue", this.messageQueue.size()), 0.0F, 1.0F, 16777215 + (m << 24));
            matrices.pop();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
        }

        if (chatFocused) {
            int v = 9;
            RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
            int w = visibleMessagesSize * v + visibleMessagesSize;
            int x = renderedLines * v + renderedLines;
            int y = this.scrolledLines * x / visibleMessagesSize;
            int z = x * x / w;
            if (w != x) {
                int aa = y > 0 ? 170 : 96;
                int ab = this.hasUnreadNewMessages ? 13382451 : 3355562;
                fill(matrices, 0, -y, 2, -y - z, ab + (aa << 24));
                fill(matrices, 2, -y, 1, -y - z, 13421772 + (aa << 24));
            }
        }

        RenderSystem.popMatrix();
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void clear(boolean clearHistory, CallbackInfo ci) {
        sideVisibleMessages.clear();
    }


    @Inject(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At("HEAD"), cancellable = true)
    private void addMessage(Text message, int messageId, int timestamp, boolean refresh, CallbackInfo ci) {
        // make sure dupe id messages are removed
        // this is as main addmessage func may get cancelled
        if (messageId != 0) {
            removeMessage(messageId);
        }

        boolean matchedARule = false;
        for (ChatRule chatRule : ChatRule.getChatRules()) {
            // compare against all rules
            if (chatRule.matches(message)) {
                // also don't add to chat if the chat side is either
                if (!matchedARule && chatRule.getChatSide() != ChatRule.ChatSide.EITHER) {
                    addToChat(chatRule.getChatSide(), message, messageId, timestamp);
                    matchedARule = true;
                }

                if (chatRule.getChatSound() != ChatRule.ChatSound.NONE) {
                    SoundUtil.playSound(chatRule.getChatSound().getSoundEvent());
                }
            }
        }
        // if rule matched, cancel normal behaviour
        if (matchedARule) {
            ci.cancel();
        }
    }

    private void addToChat(ChatRule.ChatSide side, Text message, int chatLineId, int updateCounter) {
        int i;
        switch (side) {
            case MAIN: default:
                i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
                break;
            case SIDE:
                i = MathHelper.floor((double) this.getWidth() / this.getChatScale());
                break;
        }

        List<ChatHudLine<OrderedText>> outputChatLines =
                ChatMessages.breakRenderedChatMessageLines(message, i, this.client.textRenderer)
                        .stream()
                        .map(iTextComponent -> new ChatHudLine<>(updateCounter, iTextComponent, chatLineId))
                        .collect(Collectors.toList());
        Collections.reverse(outputChatLines);
        this.getChatLines(side).addAll(0, outputChatLines);
    }

    private List<ChatHudLine<OrderedText>> getChatLines(ChatRule.ChatSide chatSide) {
        switch (chatSide) {
            case MAIN: default:
                return visibleMessages;
            case SIDE:
                return sideVisibleMessages;
        }
    }

    @Inject(method = "removeMessage", at = @At("TAIL"))
    private void removeMessage(int messageId, CallbackInfo ci) {
        this.sideVisibleMessages.removeIf((message) ->
            message.getId() == messageId
        );
    }
}
