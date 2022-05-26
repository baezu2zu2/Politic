package bazu

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

//TODO 반란 쿨타임 2분(공용)
//TODO 지도자 권한 행사 쿨타임 1분

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

    override fun onDisable() {
        gameEnd()
    }
}