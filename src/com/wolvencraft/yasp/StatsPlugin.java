/*
 * Statistics
 * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.wolvencraft.yasp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.wolvencraft.yasp.StatsSignFactory.StatsSign;
import com.wolvencraft.yasp.db.Database;
import com.wolvencraft.yasp.db.Query;
import com.wolvencraft.yasp.db.data.hooks.*;
import com.wolvencraft.yasp.db.data.sync.Settings;
import com.wolvencraft.yasp.listeners.*;
import com.wolvencraft.yasp.util.Message;
import com.wolvencraft.yasp.util.TPSTracker;

public class StatsPlugin extends JavaPlugin {
	private static StatsPlugin instance;
	private static PluginMetrics metrics;
	
	private static boolean paused;
	private static boolean crashed;
	
	private static VaultHook vaultHook;
	private static WorldGuardHook worldGuardHook;
	
	@Override
	public void onEnable() {

		instance = this;
		paused = true;
		crashed = false;
		
		if(!new File(getDataFolder(), "config.yml").exists()) {
			Message.log("Config.yml not found. Creating a one for you.");
			getConfig().options().copyDefaults(true);
			saveConfig();
			crashed = true;
			this.setEnabled(false);
			return;
		}
		
		new Query();
		
		try { new Database(); }
		catch (Exception e) {
			Message.log(Level.SEVERE, "Cannot establish a database connection!");
			if (Settings.LocalConfiguration.Debug.asBoolean()) e.printStackTrace();
			crashed = true;
			this.setEnabled(false);
			return;
		}
		
		Message.log("Database connection established.");
		
		if (getServer().getPluginManager().getPlugin("Vault") != null && Settings.RemoteConfiguration.HookVault.asBoolean()) {
			vaultHook = new VaultHook();
			vaultHook.patch();
		}
		
		if (getServer().getPluginManager().getPlugin("WorldGuard") != null && Settings.RemoteConfiguration.HookWorldGuard.asBoolean()) {
			worldGuardHook = new WorldGuardHook();
			worldGuardHook.patch();
		}

		ConfigurationSerialization.registerClass(StatsSign.class, "StatsSign");
		
		new ServerListener(this);
		new PlayerListener(this);
		new BlockListener(this);
		new ItemListener(this);
		new DeathListener(this);
		new FeedbackListener(this);

		new Settings();
		long ping = Settings.RemoteConfiguration.Ping.asInteger() * 20;
		Message.debug("ping=" + ping);
		
		try {
			metrics = new PluginMetrics(this);
			if(!metrics.isOptOut()) metrics.start();
		}
		catch (IOException e) { Message.log(Level.SEVERE, "An error occurred while connecting to PluginMetrics"); }
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new DataCollector(), 0L, ping);
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new StatsSignFactory(), (ping / 2), ping);
		Bukkit.getScheduler().runTaskTimer(this, new TPSTracker(), 0, 1);
	}

	@Override
	public void onDisable() {
		Message.log("Plugin shutting down.");
		if(crashed) return;
		try {
			DataCollector.getServerStats().pluginShutdown();
			DataCollector.dumpAll();
			
			Bukkit.getScheduler().cancelTasks(this);
			
			if(vaultHook != null) { vaultHook.cleanup(); }
			if(worldGuardHook != null) { worldGuardHook.cleanup(); }

			Database.cleanup();
			instance = null;
		} catch (NullPointerException npe) {
			Message.log(Level.SEVERE, npe.getMessage());
		} catch (Exception e) { 
			Message.log(Level.SEVERE, e.getMessage());
			if(Settings.LocalConfiguration.Debug.asBoolean()) e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(command.getName().equalsIgnoreCase("stats"))) return false;
		CommandManager.setSender(sender);
		
		if(args.length == 0) {
			CommandManager.Help.run();
			CommandManager.resetSender();
			return true;
		}
		
		for(CommandManager cmd : CommandManager.values()) {
			if(cmd.isCommand(args[0])) {
				if(Settings.LocalConfiguration.Debug.asBoolean()) {
					String argString = "/stats";
					for (String arg : args) { argString = argString + " " + arg; }
					Message.log(sender.getName() + ": " + argString);
				}
				
				boolean result = cmd.run(args);
				CommandManager.resetSender();
				return result;
			}
		}
		
		Message.sendFormattedError(sender, "Unknown command");
		CommandManager.resetSender();
		return false;
	}
	
	/**
	 * Returns a static plugin instance
	 * @return <b>YASP</b> plugin instance
	 */
	public static StatsPlugin getInstance() {
		return instance;
	}
	
	/**
	 * Checks if the database synchronization is paused or not
	 * @return <b>true</b> if the database synchronization is paused, <b>false</b> otherwise
	 */
	public static boolean getPaused() {
		return paused;
	}
	
	/**
	 * Sets the synchronization status
	 * @param paused <b>true</b> to pause the database synchronization, <b>false</b> to unpause it.
	 */
	public static void setPaused(boolean paused) {
		StatsPlugin.paused = paused;
	}
}
