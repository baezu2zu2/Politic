package bazu

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class mcSociety: JavaPlugin() {
    override fun onEnable() {

        Bukkit.getPluginManager().registerEvents(Event(), this)
        Bukkit.getPluginManager().registerEvents(GUI(), this)
        dst = this

        for (i in CommandsLabel.values()) {
            val command = Bukkit.getPluginCommand(i.label)
            if (command != null) {
                command.setExecutor(Commands())
                command.setTabCompleter(Commands())
            }
        }
    }
}