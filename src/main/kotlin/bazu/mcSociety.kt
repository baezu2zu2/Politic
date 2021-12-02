package bazu

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

private lateinit var dst: mcSociety
val inst = lazy { dst }

class mcSociety: JavaPlugin() {
    override fun onEnable() {

        Bukkit.getPluginManager().registerEvents(Event(), this)
        Bukkit.getPluginManager().registerEvents(GUI(), this)
        dst = this

        for (i in commands) {
            val command = Bukkit.getPluginCommand(i)
            if (command != null) {
                command.setExecutor(Commands())
                command.setTabCompleter(Commands())
            }
        }
    }
}