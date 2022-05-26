package bazu

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

class Attack {
    var difficulty = 0

    val difficultyEntity: Array<Map<EntityType, Int>> = arrayOf(mapOf(EntityType.PILLAGER to 5)
        , mapOf(EntityType.PILLAGER to 11), mapOf(EntityType.PILLAGER to 15)
        , mapOf(EntityType.PILLAGER to 16, EntityType.VINDICATOR to 1)
        , mapOf(EntityType.PILLAGER to 17, EntityType.VINDICATOR to 3)
        , mapOf(EntityType.PILLAGER to 18, EntityType.VINDICATOR to 4, EntityType.EVOKER to 1)
        , mapOf(EntityType.PILLAGER to 20, EntityType.VINDICATOR to 5, EntityType.EVOKER to 2)
        , mapOf(EntityType.PILLAGER to 24, EntityType.VINDICATOR to 8, EntityType.EVOKER to 4)
        , mapOf(EntityType.PILLAGER to 28, EntityType.VINDICATOR to 11, EntityType.EVOKER to 6)
        , mapOf(EntityType.PILLAGER to 33, EntityType.VINDICATOR to 15, EntityType.EVOKER to 9)
        , mapOf(EntityType.PILLAGER to 38, EntityType.VINDICATOR to 19, EntityType.EVOKER to 12)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 45, EntityType.VINDICATOR to 25, EntityType.EVOKER to 17)
        , mapOf(EntityType.PILLAGER to 70, EntityType.VINDICATOR to 80, EntityType.EVOKER to 90))

    val pillagers = arrayOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER)

    val spawnedEntity = arrayListOf<Entity>()

    val fightingPlayer = arrayListOf<Player>()

    fun spawnMob(vararg loc: Location, multy: Double = 1.0){
        for (monsterEntry in attack.difficultyEntity[attack.difficulty]) {
            for (i in 0 until (monsterEntry.value*multy).toInt()) {
                val entity = Bukkit.getWorld("world")!!.spawnEntity(loc.random(), monsterEntry.key, CreatureSpawnEvent.SpawnReason.CUSTOM)

                if (entity is LivingEntity) {
                    entity.removeWhenFarAway = false
                }

                attack.spawnedEntity.add(entity)
            }
        }
    }

    fun resetNums(){
        attack.spawnedEntity.clear()
        attack.fightingPlayer.clear()
        attack.difficulty = 0
        attacking = false
    }


    fun attack(){
        Bukkit.broadcast(Component.text("우민들이 선전포고를 했습니다!" +
                "이곳까지 이동하는데 4분정도 걸릴겁니다! 그 안에  전투 준비를 하십시오!", TextColor.color(0xff, 0x00, 0x00)))

        noAttackMore = true

        class AttackLater: BukkitRunnable(){
            override fun run() {
                attack.fightingPlayer.addAll(players)
                attacking = true
                Bukkit.broadcast(Component.text("우민들이 도착했습니다!", TextColor.color(0xff, 0x00, 0x00)))

                spawnMob(Location(Bukkit.getWorld("world"), 1.5, 64.0, 0.5))
            }
        }

        AttackLater().runTaskLater(inst.value, (20*(Random().nextInt(2*60)+3*60)).toLong())
    }

    fun counterAttack(){

        if (attack.difficulty <= 0) {
            leader!!.sendMessage(Component.text("우민들을 공격할 명분이 없습니다.. " +
                    "우민들이 1번 이상 침략해야 우민들을 공격할 수 있습니다."))

            return
        }

        Bukkit.broadcast(
            Component.text("리더가 우민들에게 선전포고를 했습니다! 1분 뒤에 리더와 리더 주변의 플레이어가 우민마을을 공격하러 떠납니다!")
        )

        noAttackMore = true


        class counterAttackLater: BukkitRunnable(){
            override fun run() {
                attacking = true
                attack.fightingPlayer.addAll(
                    leader!!.getNearbyEntities(3.0, 3.0, 3.0).filter{ it is Player } as List<Player>
                )

                attack.fightingPlayer.add(leader!!)

                for (i in attack.fightingPlayer)
                    i.teleport(Location(Bukkit.getWorld("world"), -18.5, 64.0, 208.5))

                spawnMob(Location(Bukkit.getWorld("world"), -18.5, 64.0, 235.5)
                    , Location(Bukkit.getWorld("world"), -18.5, 64.0, 225.5)
                    , Location(Bukkit.getWorld("world"), -18.5, 65.07, 252.5), multy = 1.5)
            }
        }

        counterAttackLater().runTaskLater(inst.value, (20*60).toLong())

    }
}

class AttackRunnable: BukkitRunnable(){
    override fun run() {
        if (Random().nextInt(10) < 7 && !noAttackMore)
        attack.attack()
    }
}

fun attackResult(winMessage: String, failedMessage: String, tpAllPlayer: Boolean, entityType: EntityType, winResult: (()->Unit)?=null
                   , failedResult:(()->Unit)?=null){
    var end = false

    if(entityType != EntityType.PLAYER && attack.spawnedEntity.filter{ !it.isDead }.size <= 1){
        Bukkit.broadcast(Component.text(winMessage, TextColor.color(0x4b, 0x89, 0xdc)))
        if (winResult != null) winResult?.let { it() }
        end = true
    }
    else if (entityType == EntityType.PLAYER && attack.fightingPlayer.filter { it.gameMode != GameMode.SPECTATOR }.size <= 1) {
        Bukkit.broadcast(Component.text(failedMessage, TextColor.color(0xf0, 0x56, 0x50)))
        if (failedResult != null) failedResult?.let { it() }
        for (i in attack.spawnedEntity.filter{ !it.isDead })
            i.remove()
        end = true
    }
    if (end) {
        makeAdventure(tpAllPlayer)

        attack.difficulty++
        for (i in players) {
            i.removePotionEffect(PotionEffectType.BAD_OMEN)
        }
        attack.spawnedEntity.clear()
        attack.fightingPlayer.clear()
        attacking = false
    }
}