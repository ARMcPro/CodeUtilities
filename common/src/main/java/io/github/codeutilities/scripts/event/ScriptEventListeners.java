package io.github.codeutilities.scripts.event;

import io.github.codeutilities.event.EventHandler;
import io.github.codeutilities.event.impl.KeyPressEvent;
import io.github.codeutilities.event.impl.ReceiveChatEvent;
import io.github.codeutilities.event.impl.SendChatEvent;
import io.github.codeutilities.scripts.ScriptContext;
import io.github.codeutilities.scripts.ScriptHandler;
import io.github.codeutilities.util.ComponentUtil;

public class ScriptEventListeners {

    public static void init() {
        EventHandler.register(SendChatEvent.class, (event) -> {
            ScriptContext ctx = new ScriptContext();
            ctx.setVar("message", event.getMessage());
            ScriptHandler.triggerEvent(ScriptEventType.SEND_CHAT, ctx, event);
        });

        EventHandler.register(ReceiveChatEvent.class, (event) -> {
            try {
                ScriptContext ctx = new ScriptContext();
                String msg = ComponentUtil.toFormattedString(event.getMessage());
                ctx.setVar("message", msg.replaceAll("§", "&"));
                ScriptHandler.triggerEvent(ScriptEventType.RECEIVE_CHAT, ctx, event);
            } catch (Throwable err) {
                err.printStackTrace();
            }
        });

        EventHandler.register(KeyPressEvent.class, (event) -> {
            ScriptContext ctx = new ScriptContext();
            ctx.setVar("key", event.getKey().getName());
            if (event.getAction() == 1) {
                ScriptHandler.triggerEvent(ScriptEventType.KEY_PRESS, ctx, event);
            } else if (event.getAction() == 0) {
                ScriptHandler.triggerEvent(ScriptEventType.KEY_RELEASE, ctx, event);
            }
        });
    }

}
