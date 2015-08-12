package com.bluescape.collaboration.socket;

import com.bluescape.collaboration.socket.handler.HeCreateBrowserMessageHandler;
import com.bluescape.collaboration.socket.handler.HeCreateGroupMessageHandler;
import com.bluescape.collaboration.socket.handler.HeCreateImageMessageHandler;
import com.bluescape.collaboration.socket.handler.HeCreateMessageHandler;
import com.bluescape.collaboration.socket.handler.HeCreateNoteMessageHandler;
import com.bluescape.collaboration.socket.handler.HeCreatePDFMessageHandler;
import com.bluescape.collaboration.socket.handler.HeDeleteBrowserMessageHandler;
import com.bluescape.collaboration.socket.handler.HeDeleteMessageHandler;
import com.bluescape.collaboration.socket.handler.HeGeometryChangedMessageHandler;
import com.bluescape.collaboration.socket.handler.HeMarkerCreateMessageHandler;
import com.bluescape.collaboration.socket.handler.HeMarkerDeleteMessageHandler;
import com.bluescape.collaboration.socket.handler.HeMarkerMoveMessageHandler;
import com.bluescape.collaboration.socket.handler.HeMembershipMessageHandler;
import com.bluescape.collaboration.socket.handler.HeMessageHandler;
import com.bluescape.collaboration.socket.handler.HeNavigateToMessageHandler;
import com.bluescape.collaboration.socket.handler.HePinMessageHandler;
import com.bluescape.collaboration.socket.handler.HePositionMessageHandler;
import com.bluescape.collaboration.socket.handler.HeStrokeMessageHandler;
import com.bluescape.collaboration.socket.handler.HeTemplateMessageHandler;
import com.bluescape.collaboration.socket.handler.HeTextMessageHandler;
import com.bluescape.collaboration.socket.handler.HeTsxappeventMessageHandler;
import com.bluescape.collaboration.socket.handler.IdMessageHandler;
import com.bluescape.collaboration.socket.handler.RlMessageHandler;
import com.bluescape.collaboration.socket.handler.RoomMessageHandler;
import com.bluescape.collaboration.socket.handler.UndoMessageHandler;
import com.bluescape.collaboration.socket.handler.VcMessageHandler;
import com.bluescape.collaboration.socket.handler.VeMessageHandler;

import java.util.concurrent.ConcurrentHashMap;

public class HandlerFactory {

    /**
     * Instance holder for our safe lazy instantiation pattern
     * https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    private static class instanceHolder {
        private static final HandlerFactory INSTANCE = new HandlerFactory();
    }

    /**
     * Returns the singleton
     *
     * @return
     */
    public static HandlerFactory getInstance() {
        return instanceHolder.INSTANCE;
    }

    final private ConcurrentHashMap<String, IHandler> mRegisteredHandlers;

    private HandlerFactory() {

        mRegisteredHandlers = new ConcurrentHashMap<>();

        registerHandler("id", new IdMessageHandler());
        registerHandler("room", new RoomMessageHandler());
        registerHandler("rl", new RlMessageHandler());
        registerHandler("vc", new VcMessageHandler());
        registerHandler("he", new HeMessageHandler());
        registerHandler("ve", new VeMessageHandler());

        // he subClass MessageType handlers
        registerHandler("text", new HeTextMessageHandler());
        registerHandler("position", new HePositionMessageHandler());
        registerHandler("stroke", new HeStrokeMessageHandler());
        registerHandler("create", new HeCreateMessageHandler());
        registerHandler("markercreate", new HeMarkerCreateMessageHandler());
        registerHandler("markerdelete", new HeMarkerDeleteMessageHandler());
        registerHandler("markermove", new HeMarkerMoveMessageHandler());

        registerHandler("delete", new HeDeleteMessageHandler());
        registerHandler("undo", new UndoMessageHandler());

        // heCreate subClass for note and image
        registerHandler("image", new HeCreateImageMessageHandler());
        registerHandler("note", new HeCreateNoteMessageHandler());
        registerHandler("pdf", new HeCreatePDFMessageHandler());
        registerHandler("template", new HeTemplateMessageHandler());
        registerHandler("pin", new HePinMessageHandler());

        //he messages for group and membership
        registerHandler("group", new HeCreateGroupMessageHandler());
        registerHandler("membership", new HeMembershipMessageHandler());


        registerHandler("tsxappevent", new HeTsxappeventMessageHandler());
        registerHandler("createBrowser", new HeCreateBrowserMessageHandler());
        registerHandler("geometryChanged", new HeGeometryChangedMessageHandler());
        registerHandler("deleteBrowser", new HeDeleteBrowserMessageHandler());
        registerHandler("navigateTo", new HeNavigateToMessageHandler());
    }

    public IHandler getHandler(String name) {
        if (!isRegisteredHandler(name)) return null;

        return mRegisteredHandlers.get(name);
    }

    public boolean registerHandler(String handlerName, IHandler handler) {

        mRegisteredHandlers.put(handlerName, handler);

        return true;
    }

    private boolean isRegisteredHandler(String name) {
        return mRegisteredHandlers.containsKey(name);
    }
}
