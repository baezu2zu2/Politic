package bazu

import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.event.player.ChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class Event: Listener {
    @EventHandler
    fun onDamaged(event: EntityDamageByEntityEvent){
        if (event.entity is Player){
            if (!revolutioning && !attacking) {
                event.isCancelled = true
            }else{
                if (midTeam.value!!.entries.contains(event.damager.name) || midTeam.value!!.entries.contains(event.entity.name)){
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onCloseInventory(event: InventoryCloseEvent){
        if (event.reason == InventoryCloseEvent.Reason.PLAYER && event.player is Player) {
            for (i in Windows.values()) {
                if (event.view.title() == i.label && i.no_close) {
                    class openInv : BukkitRunnable() {
                        override fun run() {
                            event.player.openInventory(event.inventory)
                        }
                    }

                    openInv().runTaskLater(inst.value, 3)
                }
            }
        }
    }

    @EventHandler
    fun onInteractAtBlock(event: PlayerInteractEvent){
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            if (event.clickedBlock!!.location == Location(Bukkit.getWorld("world"), -11.0, 65.0, 39.0)) {
                val item = event.player.inventory.itemInMainHand

                event.player.inventory.removeItem(item)

                val random = java.util.Random()

                if (random.nextInt(4) == 0) {
                    event.player.sendMessage("성공! 축하드립니다!")
                    item.amount *= 2
                    event.player.inventory.addItem(item)
                }else{
                    event.player.sendMessage("실패! 축하드립니다!")
                }
            }else if (event.clickedBlock!!.location == Location(Bukkit.getWorld("world"), -11.0, 65.0, 35.0)){
                val item = event.player.inventory.itemInMainHand

                event.player.inventory.removeItem(item)

                val random = java.util.Random()

                if (random.nextInt(8) == 0) {
                    event.player.sendMessage("성공! 축하드립니다!")
                    item.amount *= 4
                    event.player.inventory.addItem(item)
                }else{
                    event.player.sendMessage("실패! 축하드립니다!")
                }
            }
        }
    }
}