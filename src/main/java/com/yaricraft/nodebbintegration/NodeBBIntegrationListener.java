package com.yaricraft.nodebbintegration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * Created by Yari on 5/28/2015.
 */
public class NodeBBIntegrationListener implements Listener {
    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        String data = event.getPlayer().getUniqueId() + ":" + event.getPlayer().getName();
        SocketServer.getInstance().emitPlayerJoin(data);
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        String data = event.getPlayer().getUniqueId() + ":" + event.getPlayer().getName();
        SocketServer.getInstance().emitPlayerQuit(data);
    }

    @EventHandler
    public void handlePlayerChat(AsyncPlayerChatEvent event) {
        String data = event.getPlayer().getName() + ":" + event.getMessage();
        System.out.println("Sending data PlayerChat, " + data);
        SocketServer.getInstance().emitPlayerChat(data);
    }

    @EventHandler
    public void handleServerCommand(ServerCommandEvent event) {
        String data = event.getSender().getName() + ":" + event.getCommand();
        System.out.println("Sending data ServerCommand, " + data);
        //SocketServer.getInstance().emitServerCommand(data);
    }
}
