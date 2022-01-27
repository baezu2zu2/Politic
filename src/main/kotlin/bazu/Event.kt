package bazu

import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.event.player.ChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.type.Farmland
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class Event: Listener {

    val damageList = listOf<EntityType>(EntityType.SPIDER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.DROWNED)

    @EventHandler
    fun onDamaged(event: EntityDamageEvent){
        var canceled: Boolean = true

        if (attacking && event is EntityDamageByEntityEvent){
            canceled = (event.damager is Player && event.entity is Player)
        }

        if (revolutioning && event is EntityDamageByEntityEvent){
            canceled = midTeam.value!!.entries.contains(event.entity.name) || midTeam.value!!.entries.contains(event.damager.name)
        }

        if (event.cause == EntityDamageEvent.DamageCause.VOID) canceled = false

        if (event is EntityDamageByEntityEvent){
            if (damageList.contains(event.entityType) || damageList.contains(event.damager.type)) canceled = false
            if (event.damager is Projectile && (event.damager as Projectile).shooter is Skeleton) canceled = false
        }

        event.isCancelled = canceled
    }

    fun onPotion(event: EntityPotionEffectEvent){
        var canceled: Boolean = true

        if (attacking){
            canceled = false
        }

        if (revolutioning) canceled = false

        event.isCancelled = canceled
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
    fun onInteract(event: PlayerInteractEvent){
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.player.inventory.itemInMainHand.type == Material.EMERALD) {
            if (event.clickedBlock!!.location == Location(Bukkit.getWorld("world"), -11.0, 65.0, 39.0))
                randomItem(4, event.player, 2)
            else if (event.clickedBlock!!.location == Location(Bukkit.getWorld("world"), -11.0, 65.0, 35.0))
                randomItem(8, event.player, 4)
        }else if (event.action == Action.PHYSICAL && event.clickedBlock != null
            && event.clickedBlock!!.type == Material.FARMLAND)
            event.isCancelled = true
        else if(event.clickedBlock != null && event.clickedBlock!!.type == Material.SPAWNER
            && event.player.gameMode != GameMode.CREATIVE) event.isCancelled = true
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent){
        if (revolutioning){
            event.keepInventory = false
            event.player.gameMode = GameMode.SPECTATOR
            revolutionTeam.value!!.removeEntry(event.player.name)
            leaderTeam.value!!.removeEntry(event.player.name)
            midTeam.value!!.removeEntry(event.player.name)
        }
        if (attacking){
            if (attack.fightingPlayer.contains(event.player)) event.player.gameMode = GameMode.SPECTATOR
        }
    }

    fun randomItem(bound: Int, player: Player, multiply: Int){
        val item = player.inventory.itemInMainHand.clone()

        val random = java.util.Random()

        if (random.nextInt(bound) == 0) {
            player.sendMessage("성공! 축하드립니다!")
            item.amount *= multiply-1
            player.inventory.addItem(item)
        }else{
            player.sendMessage("실패! 축하드립니다!")
            player.inventory.setItemInMainHand(ItemStack(Material.AIR))
        }
    }
}