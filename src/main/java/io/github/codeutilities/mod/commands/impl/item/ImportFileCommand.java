package io.github.codeutilities.mod.commands.impl.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import io.github.codeutilities.CodeUtilities;
import io.github.codeutilities.mod.commands.Command;
import io.github.codeutilities.mod.commands.arguments.ArgBuilder;
import io.github.codeutilities.sys.hypercube.templates.CompressionUtil;
import io.github.codeutilities.sys.hypercube.templates.TemplateUtils;
import io.github.codeutilities.sys.player.chat.ChatType;
import io.github.codeutilities.sys.player.chat.ChatUtil;
import io.github.codeutilities.sys.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ImportFileCommand extends Command {

    @Override
    public void register(MinecraftClient mc, CommandDispatcher<FabricClientCommandSource> cd) {
        cd.register(ArgBuilder.literal("importfile")
                .executes(ctx -> {
                    if (!isCreative(mc)) return -1;

                    ChatUtil.sendMessage("Opening File Picker", ChatType.INFO_BLUE);
                    CodeUtilities.EXECUTOR.submit(() -> {
                        try {
                            FileDialog fd = new FileDialog((Dialog) null, "Choose a text file", FileDialog.LOAD);
                            fd.setMultipleMode(true);
                            fd.setVisible(true);
                            File[] files = fd.getFiles();
                            fd.dispose();
                            if (files == null || files.length == 0) {
                                ChatUtil.sendMessage("You didnt choose a file!", ChatType.FAIL);
                                return;
                            }

                            int valid = 0;
                            files:
                            for (File f : files) {
                                if (files.length != 1)
                                    ChatUtil.sendMessage("Loading file: " + f.getName(), ChatType.INFO_BLUE);
                                Scanner sc = new Scanner(f, "utf-8");

                                List<String> lines = new ArrayList<>();

                                while (sc.hasNextLine()) {
                                    String line = sc.nextLine();
                                    if (line.length() > 2000) {
                                        ChatUtil.sendMessage("Line " + (lines.size() + 1) + " is too long! (" + line.length() + " > 2000)", ChatType.FAIL);
                                        continue files;
                                    }
                                    lines.add(line);
                                    if (lines.size() > 10000) {
                                        ChatUtil.sendMessage("File contains contains too many lines! (Max: 10000)", ChatType.FAIL);
                                        continue files;
                                    }
                                }

                                List<JsonObject> blocks = new ArrayList<>();
                                List<String> current = new ArrayList<>();

                                boolean first = true;
                                for (String line : lines) {
                                    current.add(line);
                                    if (current.size() >= 26) {
                                        blocks.add(block(current, first));
                                        first = false;
                                        current = new ArrayList<>();
                                    }
                                }
                                if (current.size() != 0) blocks.add(block(current, first));

                                String template = template(blocks);
                                if (template.getBytes().length > 65536) {//i have no idea what the actual limit is it just seems to be close to this
                                    ChatUtil.sendMessage("Your file is too large!", ChatType.FAIL);
                                } else {
                                    ItemStack item = new ItemStack(Items.ENDER_CHEST);
                                    TemplateUtils.applyRawTemplateNBT(item, f.getName(), "CodeUtilities", template);
                                    ItemUtil.giveCreativeItem(item, files.length == 1);
                                    if (files.length != 1) Thread.sleep(500);
                                    valid++;
                                }
                            }
                            if (files.length != 1 && valid > 0)
                                ChatUtil.sendMessage("Loaded " + valid + " files!", ChatType.SUCCESS);

                        } catch (Exception err) {
                            err.printStackTrace();
                            ChatUtil.sendMessage("Unexpected Error.", ChatType.FAIL);
                        }
                    });
                    return 1;
                })
        );
    }

    private String template(List<JsonObject> iblocks) throws IOException {
        JsonArray blocks = new JsonArray();
        blocks.add(CodeUtilities.JSON_PARSER.parse("{\"id\":\"block\",\"block\":\"func\",\"args\":{\"items\":[]},\"data\":\"file\"}"));
        for (JsonObject block : iblocks) {
            blocks.add(block);
        }
        JsonObject root = new JsonObject();
        root.add("blocks", blocks);
        byte[] b64 = CompressionUtil.toBase64(CompressionUtil.toGZIP(root.toString().getBytes(StandardCharsets.UTF_8)));
        return new String(b64);
    }

    private JsonObject block(List<String> texts, boolean first) {
        JsonObject var = CodeUtilities.JSON_PARSER.parse("{\"item\":{\"id\":\"var\",\"data\":{\"name\":\"file\",\"scope\":\"local\"}},\"slot\":0}").getAsJsonObject();
        JsonArray items = new JsonArray();
        items.add(var);
        for (String text : texts) {
            items.add(textItem(text, items.size()));
        }
        JsonObject args = new JsonObject();
        args.add("items", items);
        JsonObject root = new JsonObject();
        root.add("args", args);
        root.addProperty("id", "block");
        root.addProperty("block", "set_var");
        root.addProperty("action", first ? "CreateList" : "AppendValue");
        return root;
    }

    private JsonObject textItem(String text, int slot) {
        JsonObject data = new JsonObject();
        data.addProperty("name", text);
        JsonObject item = new JsonObject();
        item.addProperty("id", "txt");
        item.add("data", data);
        JsonObject root = new JsonObject();
        root.add("item", item);
        root.addProperty("slot", slot);
        return root;
    }

    @Override
    public String getDescription() {
        return "[blue]/importfile[reset]\n"
                + "\n"
                + "Import a text file as a code template.\n"
                + "[red]Notice[reset]: Does NOT support line wrapping so if the code line is too long it will get cut off";
    }

    @Override
    public String getName() {
        return "/importfile";
    }
}
