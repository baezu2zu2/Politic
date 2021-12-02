package bazu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

var attack = Attack()
var attacking = false

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

    val spawnedEntity = arrayListOf<Entity>()

    val fightingPlayer = arrayListOf<Player>()

    fun spawnMob(vararg loc: Location, multy: Int = 1){
        for (monsterEntry in attack.difficultyEntity[attack.difficulty]) {
            for (i in 1..monsterEntry.value*multy) {
                val entity = Bukkit.getWorld("world")!!.spawnEntity(loc.random(), monsterEntry.key)

                entity.customName = "우민"

                attack.spawnedEntity.add(entity)
            }
        }
    }

    fun resetNums(){
        attack.spawnedEntity.clear()
        attack.fightingPlayer.clear()
        attack.difficulty = 0
    }


    fun attack(){
        Bukkit.broadcast(Component.text("우민들이 선전포고를 했습니다!" +
                "이곳까지 이동하는데 4분정도 걸릴겁니다! 그 안에  전투 준비를 하십시오!", TextColor.color(0xff, 0x00, 0x00)))

        attack.fightingPlayer.addAll(Bukkit.getOnlinePlayers())

        attacking = true

        class AttackLater: BukkitRunnable(){
            override fun run() {
                spawnMob(Location(Bukkit.getWorld("world"), 70.5, 64.0, 0.5)
                    , Location(Bukkit.getWorld("world"), 1.5, 64.0, 46.5))

                class AttackResult: BukkitRunnable(){
                    override fun run() {
                        if (attack.fightingPlayer.filter { it.gameMode != GameMode.SPECTATOR }.isEmpty()
                            || attack.spawnedEntity.filter{ !it.isDead }.isEmpty()){

                            if (attack.fightingPlayer.filter { it.gameMode != GameMode.SPECTATOR }.isEmpty()) {
                                Bukkit.broadcast(Component.text("우민들의 침략이 성공으로 끝났습니다.."))
                                if (attack.difficulty < 0) attack.difficulty--
                                for (i in Bukkit.getOnlinePlayers())
                                    for (j in 0 until i.inventory.size)
                                        if (Random().nextInt(20) == 0)
                                            i.inventory.setItem(j, ItemStack(Material.AIR))
                            }

                            else if(attack.spawnedEntity.filter{ !it.isDead }.isEmpty()){
                                Bukkit.broadcast(Component.text("우민들의 침략이 실패로 끝났습니다!"))
                                if (attack.difficulty > attack.difficultyEntity.size) attack.difficulty++
                            }


                            for (i in Bukkit.getOnlinePlayers()){
                                i.gameMode = GameMode.ADVENTURE
                            }
                            attack.spawnedEntity.clear()
                            attack.fightingPlayer.clear()
                            this.cancel()
                            attacking = false
                        }
                    }
                }

                AttackResult().runTaskTimer(inst.value, 0, 1)
            }
        }

        AttackLater().runTaskLater(inst.value, (20*(Random().nextInt(2*60)+3*60)).toLong())
    }

    fun counterAttack(){

        class counterAttackLater: BukkitRunnable(){
            override fun run() {
                if (attack.difficulty > 3) {
                    attack.fightingPlayer.addAll(
                        leader!!.getNearbyEntities(3.0, 3.0, 3.0).filter{ it is Player } as List<Player>
                    )

                    attack.fightingPlayer.add(leader!!)

                    for (i in attack.fightingPlayer)
                        i.teleport(Location(Bukkit.getWorld("world"), -18.5, 64.0, 208.5))

                    spawnMob(Location(Bukkit.getWorld("world"), -18.5, 64.0, 235.5)
                        , Location(Bukkit.getWorld("world"), -18.5, 64.0, 225.5)
                        , Location(Bukkit.getWorld("world"), -18.5, 64.0, 252.5), multy = 2)

                    attacking = true

                    class AttackResult: BukkitRunnable(){
                        override fun run() {
                            if (attack.fightingPlayer.filter { it.gameMode != GameMode.SPECTATOR }.isEmpty()
                                || attack.spawnedEntity.filter{ !it.isDead }.isEmpty()){

                                if (attack.fightingPlayer.filter { it.gameMode != GameMode.SPECTATOR }.isEmpty()) {
                                    Bukkit.broadcast(Component.text("우민마을 침략이 실패로 끝났습니다.."))
                                } else if(attack.spawnedEntity.filter{ !it.isDead }.isEmpty()){
                                    Bukkit.broadcast(Component.text("우민마을 침략이 성공으로 끝났습니다!"))

                                    if (attack.difficulty < attack.difficultyEntity.size-1) attack.difficulty+=2
                                    if (attack.difficulty == attack.difficultyEntity.size-1)
                                        attack.difficulty = attack.difficultyEntity.size

                                    treasury.value!!.addItem(ItemStack(Material.EMERALD, 64)
                                        , ItemStack(Material.EMERALD, 64))
                                }


                                for (i in Bukkit.getOnlinePlayers()){
                                    i.gameMode = GameMode.ADVENTURE
                                    for (j in 0 until i.inventory.size)
                                        if (Random().nextInt(20) == 0) i.inventory.setItem(j, ItemStack(Material.AIR))
                                }
                                attack.spawnedEntity.clear()
                                attack.fightingPlayer.clear()
                                this.cancel()
                                attacking = false
                            }
                        }
                    }

                    AttackResult().runTaskTimer(inst.value, 0, 1)
                }
            }
        }

        counterAttackLater().runTaskLater(inst.value, (20*60).toLong())

    }
}

class AttackRunnable: BukkitRunnable(){
    override fun run() {
        if (Random().nextInt(10) < 3 && !attacking)
        attack.attack()
    }
}