package io.github.codeutilities.mixin.messages;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.codeutilities.config.Config;
import io.github.codeutilities.features.social.chat.ConversationTimer;
import io.github.codeutilities.util.chat.ChatType;
import io.github.codeutilities.util.chat.ChatUtil;
import io.github.codeutilities.util.networking.DFInfo;
import io.github.codeutilities.util.networking.State;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinPlayerChatMessage {
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
//    private boolean stopTimer = false;
//    private final Thread conversationTimer = new Thread(() -> {
//        while (true) {
//            if (stopTimer) {
//                stopTimer = false;
//                stopConversationTimer();
//            }
//            if (System.currentTimeMillis() - Config.getLong("automsg_timeoutNumber") >= Long.parseLong(DFInfo.conversationUpdateTime)) {
//                ChatUtil.sendMessage("Your conversation with " + DFInfo.currentConversation + " was inactive and ended.", ChatType.INFO_BLUE);
//                DFInfo.currentConversation = null;
//                DFInfo.conversationUpdateTime = null;
//                stopConversationTimer();
//            }
//        }
//    });

    @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void onMessage(String string, CallbackInfo ci) {
        String[] args = string.split(" ");
        if (minecraftClient.player != null) {
            if (!string.startsWith("/")) {
                ItemStack mainHand = minecraftClient.player.inventory.getMainHandStack();
                if (mainHand.hasTag()) {
                    NbtCompound tag = mainHand.getTag();
                    NbtCompound publicBukkitValues = tag.getCompound("PublicBukkitValues");
                    if (tag.contains("PublicBukkitValues") && publicBukkitValues.contains("hypercube:varitem")) {
                        if ((string.endsWith(" -l") || string.endsWith(" -s") || string.endsWith(" -g")) && Config.getBoolean("quickVarScope")) {
                            String varItem = publicBukkitValues.getString("hypercube:varitem");
                            try {
                                JsonObject jsonObject = new JsonParser().parse(varItem).getAsJsonObject();
                                if (jsonObject.has("id")) {
                                    if (jsonObject.get("id").getAsString().equals("var")) {
                                        JsonObject data = jsonObject.get("data").getAsJsonObject();
                                        String displayScope = "";
                                        String displayScopeColor = "";
                                        if (string.endsWith(" -l")) {
                                            displayScope = "LOCAL";
                                            displayScopeColor = "green";
                                            data.addProperty("scope", "local");
                                        }
                                        if (string.endsWith(" -s")) {
                                            displayScope = "SAVE";
                                            displayScopeColor = "yellow";
                                            data.addProperty("scope", "saved");
                                        }
                                        if (string.endsWith(" -g")) {
                                            displayScope = "GAME";
                                            displayScopeColor = "gray";
                                            data.addProperty("scope", "unsaved");
                                        }
                                        final String name = string.substring(0, string.length() - 3);
                                        data.addProperty("name", name);
                                        jsonObject.add("data", data);
                                        publicBukkitValues.putString("hypercube:varitem", jsonObject.toString());
                                        tag.put("PublicBukkitValues", publicBukkitValues);
                                        mainHand.setTag(tag);

                                        mainHand.getTag().remove("display");
                                        NbtCompound display = new NbtCompound();
                                        NbtList lore = new NbtList();
                                        display.putString("Name", String.format("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"white\",\"text\":\"%s\"}],\"text\":\"\"}", name));
                                        lore.add(NbtString.of(String.format("{\"extra\":[{\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"%s\",\"text\":\"%s\"}],\"text\":\"\"}", displayScopeColor, displayScope)));
                                        display.put("Lore", lore);
                                        mainHand.getTag().put("display", display);

                                        ci.cancel();
                                        minecraftClient.interactionManager.clickCreativeStack(mainHand, minecraftClient.player.inventory.selectedSlot + 36);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (DFInfo.currentState.getMode() != State.Mode.SPAWN || !mainHand.getName().getString().equals("◇ Game Menu ◇"))
                            conversationMessage(string, ci);
                    }
                } else conversationMessage(string, ci);
            }
        }
        //start conversation
        if (Config.getBoolean("automsg") && (string.startsWith("/msg ") || string.startsWith("/w "))) {
            if (args.length == 2) {
                ci.cancel();
                ConversationTimer.currentConversation = args[1];
                ConversationTimer.conversationUpdateTime = String.valueOf(System.currentTimeMillis());
                if (Config.getBoolean("automsg_timeout")) ConversationTimer.isTimerOn = true;
                ChatUtil.sendMessage("Started a conversation with " + ConversationTimer.currentConversation + "!", ChatType.SUCCESS);

            }
        }
        //end conversation
        if (ConversationTimer.currentConversation != null && (string.startsWith("/chat ") || string.startsWith("/c "))) {
            ConversationTimer.currentConversation = null;
            ConversationTimer.isTimerOn = false;
            ChatUtil.sendMessage("The conversation was ended.", ChatType.SUCCESS);
        }
    }

    private void conversationMessage(String message, CallbackInfo ci) {
        if (ConversationTimer.currentConversation != null && (DFInfo.currentState.getMode() != State.Mode.PLAY || !message.startsWith("@"))) {
            ci.cancel();
            ConversationTimer.conversationUpdateTime = String.valueOf(System.currentTimeMillis());
            minecraftClient.player.sendChatMessage("/msg " + ConversationTimer.currentConversation + " " + message);
        }
    }


//    private void startConversationTimer() {
//        synchronized (conversationTimer) {
//            Thread.State timerState = conversationTimer.getState();
//            if (timerState == Thread.State.NEW) conversationTimer.start();
//            if (timerState == Thread.State.WAITING) conversationTimer.notify();
//        }
//    }

//    private void stopConversationTimer() {
//        synchronized (conversationTimer) {
//            try {
//                conversationTimer.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
