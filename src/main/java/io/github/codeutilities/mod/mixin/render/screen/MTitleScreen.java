package io.github.codeutilities.mod.mixin.render.screen;

import io.github.codeutilities.CodeUtilities;
import io.github.codeutilities.mod.config.Config;
import io.github.codeutilities.sys.renderer.BlendableTexturedButtonWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MTitleScreen extends Screen {

    private final Identifier identifier_main = new Identifier(CodeUtilities.MOD_ID + ":textures/gui/df.png");
    private final Identifier identifier_beta = new Identifier(CodeUtilities.MOD_ID + ":textures/gui/beta.png");
    private final Identifier identifier_node1 = new Identifier(CodeUtilities.MOD_ID + ":textures/gui/node1.png");
    private final Identifier identifier_node2 = new Identifier(CodeUtilities.MOD_ID + ":textures/gui/node2.png");
    private final Identifier identifier_node3 = new Identifier(CodeUtilities.MOD_ID + ":textures/gui/node3.png");
    private final Identifier identifier_node4 = new Identifier(CodeUtilities.MOD_ID + ":textures/gui/node4-valley.png");
    private final Identifier identifier_node5 = new Identifier(CodeUtilities.MOD_ID + ":textures/gui/node5.png");

    protected MTitleScreen(LiteralText title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "initWidgetsNormal")
    public void drawMenuButton(int y, int spacingY, CallbackInfo info) {
        if (Config.getBoolean("dfButton")) {
            if (!Config.getBoolean("dfNodeButtons")) {
                this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104, y + spacingY, 20, 20, 0, 0, 20, identifier_main, 20, 40,
                        (button) -> {
                            MinecraftClient mc = MinecraftClient.getInstance();
                            ServerInfo serverInfo = new ServerInfo("DF", "mcdiamondfire.com:25565", false);
                            mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                        }));
            }
        }

        if (Config.getBoolean("dfNodeButtons")) {
            boolean modMenuButtonPresent = false;
            if (FabricLoader.getInstance().isModLoaded("modmenu")) {
                modMenuButtonPresent = io.github.codeutilities.sys.util.ModMenuSupport.isModsButtonPresent();
            }

            this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104, y - spacingY, 20, 20, 0, 0, 20, identifier_main, 20, 40,
                    (button) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        ServerInfo serverInfo = new ServerInfo("DF", "mcdiamondfire.com:25565", false);
                        mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                    }));

            this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104 + 22, y - spacingY, 20, 20, 0, 0, 20, identifier_beta, 20, 40,
                    (button) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        ServerInfo serverInfo = new ServerInfo("DF Beta", "beta.mcdiamondfire.com:25565", false);
                        mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                    }));

            this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104, y, 20, 20, 0, 0, 20, identifier_node1, 20, 40,
                    (button) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        ServerInfo serverInfo = new ServerInfo("DF Node1", "node1.mcdiamondfire.com:25565", false);
                        mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                    }));

            this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104 + 22, y, 20, 20, 0, 0, 20, identifier_node2, 20, 40,
                    (button) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        ServerInfo serverInfo = new ServerInfo("DF Node2", "node2.mcdiamondfire.com:25565", false);
                        mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                    }));

            this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104, y + spacingY, 20, 20, 0, 0, 20, identifier_node3, 20, 40,
                    (button) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        ServerInfo serverInfo = new ServerInfo("DF Node3", "node3.mcdiamondfire.com:25565", false);
                        mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                    }));

            this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104 + 22, y + spacingY, 20, 20, 0, 0, 20, identifier_node4, 20, 40,
                    (button) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        ServerInfo serverInfo = new ServerInfo("DF Node4", "node4.mcdiamondfire.com:25565", false);
                        mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                    }));

            this.addButton(new BlendableTexturedButtonWidget(this.width / 2 + 104 + (modMenuButtonPresent ? 22 : 0), y + spacingY + 24, 20, 20, 0, 0, 20, identifier_node5, 20, 40,
                    (button) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        ServerInfo serverInfo = new ServerInfo("DF Node5", "node5.mcdiamondfire.com:25565", false);
                        mc.openScreen(new ConnectScreen(mc.currentScreen, mc, serverInfo));
                    }));
        }
    }

}
