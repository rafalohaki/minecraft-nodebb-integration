package com.radiofreederp.nodebbintegration.tasks;

import com.radiofreederp.nodebbintegration.MinecraftServerCommon;
import com.radiofreederp.nodebbintegration.NodeBBIntegrationPlugin;
import com.radiofreederp.nodebbintegration.socketio.ESocketEvent;
import com.radiofreederp.nodebbintegration.socketio.SocketIOClient;
import io.socket.client.Ack;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yari on 4/20/2016.
 */
public class TaskTick implements Runnable {

    private final NodeBBIntegrationPlugin plugin;
    private final MinecraftServerCommon server;

    private static TaskTick instance;

    public static TaskTick getTask() {
        return instance;
    }

    public TaskTick(NodeBBIntegrationPlugin plugin){
        instance = this;

        this.plugin = plugin;
        this.server = plugin.getMinecraftServer();

        plugin.runTaskTimerAsynchronously(instance);
    }

    @Override
    public void run() {
        if (SocketIOClient.connected()) {

            final String socketEvent = ESocketEvent.SEND_SERVER_STATUS;

            JSONObject obj = new JSONObject();

            try {

                obj.put("tps", server.getTPS());
                obj.put("key", plugin.getPluginConfig().getForumAPIKey());
                obj.put("players", server.getPlayerList());

                obj.put("version", server.getVersion());
                obj.put("name", server.getServerName());

                obj.put("gametype", server.getWorldType());
                obj.put("map", server.getWorldName());

                obj.put("motd", server.getMotd());
                obj.put("onlinePlayers", server.getOnlinePlayers());
                obj.put("maxPlayers", server.getMaxPlayers());

                obj.put("pluginList", server.getPluginList());

                obj.put("icon", server.getServerIcon());

            } catch (JSONException e) {
                plugin.log("Error constructing JSON Object for " + socketEvent);
                e.printStackTrace();
                return;
            }

            plugin.log("Sending " + socketEvent);
            SocketIOClient.emit(socketEvent, obj, new Ack() {
                @Override
                public void call(Object... args) {
                    plugin.log(args[0] == null ? "no errors" : "got error");
                }
            });
        }
    }
}
