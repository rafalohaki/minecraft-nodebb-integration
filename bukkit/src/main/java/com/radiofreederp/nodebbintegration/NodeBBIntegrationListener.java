package com.radiofreederp.nodebbintegration;

import com.radiofreederp.nodebbintegration.hooks.OnTimeHook;
import com.radiofreederp.nodebbintegration.hooks.VanishNoPacketHook;
import com.radiofreederp.nodebbintegration.hooks.VaultHook;
import com.radiofreederp.nodebbintegration.socketio.SocketIOClient;
import io.socket.client.Ack;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Yari on 5/28/2015.
 */
public class NodeBBIntegrationListener implements Listener {

    private NodeBBIntegrationBukkit plugin;

    public NodeBBIntegrationListener(NodeBBIntegrationPlugin plugin) {
        this.plugin = (NodeBBIntegrationBukkit)plugin;
    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        String socketEvent = SocketIOClient.Events.onPlayerJoin;

        if (VanishNoPacketHook.isEnabled()) {
            if (VanishNoPacketHook.isVanished(event.getPlayer().getName())) return;
        }

        SocketIOClient.emit(socketEvent, getPlayerJoinData(event.getPlayer()), (Object... args) -> plugin.log(socketEvent + " callback received."));
    }

    public static JSONObject getPlayerJoinData(Player player) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", player.getName());
            obj.put("id", player.getUniqueId());
            obj.put("key", NodeBBIntegrationBukkit.instance.getAPIKey());

            if (OnTimeHook.isEnabled()) {
                if (OnTimeHook.isEnabled()) {
                    OnTimeHook.onTimeCheckTime(player, obj);
                }
            }

            if (VaultHook.chat != null && VaultHook.permission != null) {
                String[] groups = VaultHook.permission.getPlayerGroups(null, player);

                World world = Bukkit.getWorlds().get(0);
                HashMap<String,Object> groupsData = new HashMap<String,Object>();

                for (int i = 0; i < groups.length; i++)
                {
                    groupsData.put(groups[i], VaultHook.chat.getGroupPrefix(world, groups[i]));
                }
                obj.put("groups", groupsData);
                obj.put("prefix", VaultHook.chat.getPlayerPrefix(player));
            }
        } catch (JSONException e) {
            NodeBBIntegrationBukkit.instance.log("Error constructing JSON Object for " + SocketIOClient.Events.onPlayerJoin);
            e.printStackTrace();
        }

        return obj;
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        String socketEvent = SocketIOClient.Events.onPlayerQuit;
        SocketIOClient.emit(socketEvent, getPlayerQuitData(event.getPlayer()), (Object... args) -> plugin.log(socketEvent + " callback received."));
    }

    public static JSONObject getPlayerQuitData(Player player) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("name", player.getName());
            obj.put("id", player.getUniqueId());
            obj.put("key", NodeBBIntegrationBukkit.instance.getAPIKey());
        } catch (JSONException e) {
            NodeBBIntegrationBukkit.instance.log("Error constructing JSON Object for " + SocketIOClient.Events.onPlayerQuit);
            e.printStackTrace();
        }

        return obj;
    }

    @EventHandler
    public void handlePlayerChat(AsyncPlayerChatEvent event) {
        if (SocketIOClient.disconnected()) return;
        final String socketEvent = "eventPlayerChat";

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", event.getPlayer().getName());
            obj.put("id", event.getPlayer().getUniqueId());
            obj.put("message", event.getMessage());
            obj.put("key", plugin.getConfig().getString("APIKEY"));
        } catch (JSONException e) {
            plugin.log("Error constructing JSON Object for " + socketEvent);
            e.printStackTrace();
            return;
        }

        plugin.log("Sending " + socketEvent);
        SocketIOClient.emit(socketEvent, obj, new Ack() {
            @Override
            public void call(Object... args) {
                plugin.log(socketEvent + " callback received.");
            }
        });
    }

    // TODO
    @EventHandler
    public void onServerListPing(final ServerListPingEvent event) {
        if (SocketIOClient.disconnected()) return;

        plugin.log("Server List Ping from: " + event.getAddress().toString());
    }

    // Allow the WorldSaveEvent to save the config once a minute.
    private long stamp = 0;
    @EventHandler
    public void onWorldSave(WorldSaveEvent event)
    {
        long now = new Date().getTime();
        if (now - stamp > 60000) {
            stamp = now;
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerManager.saveConfig();
                    plugin.log("Saved player data.");
                }
            }.runTask(plugin);
        }
    }
}
