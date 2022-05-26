@file:JvmName("Game")

package bazu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team

val players = mutableListOf<Player>()
var system: System? = null
var tax = 10
var leader: Player? = null
var voteNum = mutableMapOf<Player, Int>()
var YesNoVote = hashMapOf<Boolean, Int>(false to 0, true to 0)
var voted = 0
val gui = GUI()
var taxRunnable = TaxRunnable()
var changeMoney = ChangeMoney()
var attackRunnable = AttackRunnable()

var attack = Attack()
var attacking = false
var noAttackMore = false
val leaderTimer= mutableMapOf<Player, Int>()

lateinit var dst: mcSociety
val inst = lazy { dst }

var playerHeads: ArrayList<ItemStack> = arrayListOf()

var leaderWinTimer: LeaderWinTimer? = null

var canRevolution = true
var revolutionTimer = 0
var politicPower = 0

fun setplayerHeads(){
    playerHeads.clear()

    for (i in players) {
        val playerHead = ItemStack(Material.PLAYER_HEAD)
        val meta = playerHead.itemMeta
        if (meta is SkullMeta) {
            meta.owningPlayer = i
            playerHead.itemMeta = meta
        }
        playerHeads.add(playerHead)
    }
}

var taxTerm = 5.0

val prisonTerm: Lazy<Objective?> = lazy {
    if (Bukkit.getScoreboardManager().mainScoreboard.getObjective("prisonTerm") == null){
        Bukkit.getScoreboardManager().mainScoreboard.registerNewObjective("prisonTerm", "dummy"
            , Component.text("남은 형량"))
    }else{
        Bukkit.getScoreboardManager().mainScoreboard.getObjective("prisonTerm")
    }
}

val revolutionTeam: Lazy<Team?> = lazy{
    if (Bukkit.getScoreboardManager().mainScoreboard.getTeam("revolution") == null){
        Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("revolution")
    }else{
        Bukkit.getScoreboardManager().mainScoreboard.getTeam("revolution")
    }
}

val leaderTeam: Lazy<Team?> = lazy{
    if (Bukkit.getScoreboardManager().mainScoreboard.getTeam("leader") == null){
        Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("leader")
    }else{
        Bukkit.getScoreboardManager().mainScoreboard.getTeam("leader")
    }
}

val midTeam: Lazy<Team?> = lazy{
    if (Bukkit.getScoreboardManager().mainScoreboard.getTeam("mid") == null){
        Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("mid")
    }else{
        Bukkit.getScoreboardManager().mainScoreboard.getTeam("mid")
    }
}

val treasury: Lazy<Inventory?> = lazy {
    val chest = Location(Bukkit.getWorld("world"), -32.5, 67.0, -23.5).block.state
    if (chest is Chest) {
        Bukkit.createInventory(chest,9*3, Component.text("국고"))
    }else{
        null
    }
}

val money = hashMapOf(ItemStack(Material.DARK_OAK_LOG, 15) to 8,
    ItemStack(Material.COBBLESTONE, 15) to 10, ItemStack(Material.COD, 1) to 7,
    ItemStack(Material.SALMON, 1) to 12, ItemStack(Material.TROPICAL_FISH, 1) to 17,
    ItemStack(Material.PUFFERFISH, 1) to 13, ItemStack(Material.NAUTILUS_SHELL, 1) to 13,
    ItemStack(Material.NAME_TAG, 1) to 13, ItemStack(Material.ROTTEN_FLESH, 1) to 4,
    ItemStack(Material.BONE, 1) to 5, ItemStack(Material.SPIDER_EYE, 1) to 5,
    ItemStack(Material.STRING, 1) to 5, ItemStack(Material.MELON_SLICE, 1) to 7,
    ItemStack(Material.PUMPKIN, 1) to 7, ItemStack(Material.SWEET_BERRIES, 1) to 7,
    ItemStack(Material.CARROT, 1) to 7, ItemStack(Material.POTATO, 1) to 7,
    ItemStack(Material.BEETROOT, 1) to 7, ItemStack(Material.WHEAT, 1) to 7,
    ItemStack(Material.WHITE_WOOL, 1) to 5, ItemStack(Material.EGG, 1) to 12,)

var revolutioning = false


fun initalTeams(){
    revolutionTeam.value!!.setAllowFriendlyFire(false)
    revolutionTeam.value!!.color(NamedTextColor.BLUE)

    leaderTeam.value!!.setAllowFriendlyFire(false)
    leaderTeam.value!!.color(NamedTextColor.RED)
}

fun resetVoteNum(){
    for (i in players){
        voteNum[i] = 0
    }
    voted = 0
    YesNoVote.put(true, 0)
    YesNoVote.put(false, 0)
}

fun addRunnables(goalTime: Int){
    taxRunnable = TaxRunnable()
    changeMoney = ChangeMoney()
    attackRunnable = AttackRunnable()
    leaderWinTimer = LeaderWinTimer(goalTime)
}

fun playersSetting(){
    for (i in players){
        voteNum.put(i, 0)
        i.inventory.clear()
        i.gameMode = GameMode.ADVENTURE
        i.teleport(Location(Bukkit.getWorld("world"), 1.5, 64.0, 0.5))
        i.sendExperienceChange(0.0f, 0)
        i.foodLevel = 20
    }
}

fun gameStart(goalTime: Int){
    players.addAll(Bukkit.getOnlinePlayers())

    setplayerHeads()
    initalTeams()
    resetVoteNum()
    attack.resetNums()

    playersSetting()

    addRunnables(goalTime)

    gui.changeTax(10)


    taxRunnable.runTaskTimer(inst.value, (taxTerm*20*60).toLong(), (taxTerm*20*60).toLong())
    changeMoney.runTaskTimer(inst.value, 0, 10)
    attackRunnable.runTaskTimer(inst.value, (20*60*4).toLong(), (20*60*3).toLong())


    object:BukkitRunnable(){
        override fun run() {
            politicPower += 10
        }
    }.runTaskTimer(inst.value, 0, 20*60)

    system = System.values().random()
    Bukkit.broadcast(Component.text("이 국가의 체제는 ${system!!.label}입니다."))
    val list = arrayListOf<Player>()

    list.addAll(players)

    setLeaderWithMessage(players.random())

    leader?.addScoreboardTag("leader")

    leaderWinTimer = LeaderWinTimer(goalTime)

    if (leaderWinTimer == null) return;

    leaderWinTimer!!.start()
}

fun gameEnd(){
    Bukkit.broadcast(Component.text("게임이 끝났습니다!"))
    getRanking()

    system = null
    tax = 5
    leader = null
    voteNum.clear()

    voted = 0
    playerHeads.clear()
    revolutionTimer = 0
    canRevolution = true
    politicPower = 0

    attack.resetNums()

    for (i in players){
        i.removeScoreboardTag("leader")
        i.removeScoreboardTag("prison")
        i.removeScoreboardTag("conference")
    }


    Bukkit.getScheduler().cancelTasks(inst.value)

    if (leaderWinTimer == null) return
    leaderWinTimer!!.cancel = true

    players.clear()
}

fun getRanking(){
    Bukkit.broadcast(Component.text("승리자 : 현재 리더 ${leader!!.name}"))
    var idx = 2
    for(i in sortRanking(players.filter{ it != leader!! } as MutableList<Player>)){
        Bukkit.broadcast(Component.text("${idx}위 : ${i.name}"))
        idx++
    }
}

fun sortRanking(players: MutableList<Player>) : Set<Player>{
    if (players.size <= 1)
        return players.toSet()

    val pivot = players[0]

    val lesser = ArrayList<Player>()
    val equal = ArrayList<Player>()
    val greater = ArrayList<Player>()

    for(i in players){
        if (getEmerald(i) < getEmerald(pivot))
            lesser.add(i)
        else if (getEmerald(i) == getEmerald(pivot))
            equal.add(i)
        else
            greater.add(i)
    }

    return sortRanking(greater).union(equal).union(lesser)
}

fun getEmerald(player: Player): Int{
    var sum = 0
    for (i in player.inventory){
        if (i != null)
            if (i.type == Material.EMERALD)
                sum += i.amount
    }

    return sum
}

fun setLeaderWithMessage(player: Player){
    Bukkit.broadcast(Component.text("리더는 "+player.name+"입니다!", TextColor.color(0xff, 0x50, 0x50)))
    if (leader != null) leader!!.removeScoreboardTag("leader")
    leader = player
    player.addScoreboardTag("leader")
    if (leaderTimer[player] == null) leaderTimer[player] = 0
}

enum class System(val label: String){
    DICTORSHIP("독재"),
}

fun genMeta(item: ItemStack, displayName: Component, vararg lore: Component): ItemStack{
    val meta = item.itemMeta
    meta.displayName(displayName)

    val lores = arrayListOf<Component?>()
    lores.addAll(lore)
    meta.lore(lores)

    item.setItemMeta(meta)

    return item
}

fun revolution(player: Player){
    leader!!.removeScoreboardTag("leader")
    if (player != leader!!) {
        revolutionTeam.value!!.addEntry(player.name)
        leaderTeam.value!!.addEntry(leader!!.name)
    }

    Bukkit.broadcast(Component.text("${player.name}님이 혁명을 일으켰습니다!"))
    player.addScoreboardTag("revolutionFirst")

    val array = arrayListOf<Player>()
    array.addAll(players.filter {
        !revolutionTeam.value!!.entries.contains(it.name) && !leaderTeam.value!!.entries.contains(it.name)
                && !it.scoreboardTags.contains("prison")
    })
    for (i in players.filter { it.scoreboardTags.contains("prison") }) {
        midTeam.value!!.addEntry(i.name)
    }

    Bukkit.getWorld("world")!!.setGameRule(GameRule.KEEP_INVENTORY, false)

    Windows.REVOLUTION.run(array)
    Windows.REVOLUTION.after()

    if (leaderWinTimer == null) return

    leaderWinTimer!!.stop = true
}

fun makeAdventure(tpAllPlayer: Boolean){
    for (i in players){
        if (i.gameMode == GameMode.SPECTATOR || tpAllPlayer) i.teleport(Location(Bukkit.getWorld("world"), 1.5, 64.0, 0.5))
        i.gameMode = GameMode.ADVENTURE
    }
}



enum class SucceedProperty{
    SUCCEED, NOT_YET, FAILED
}

class TaxRunnable: BukkitRunnable(){
    override fun run() {
        if (!revolutioning) {
            Bukkit.broadcast(Component.text("세금을 징수하겠습니다!"))
            for (i in players.filter {it != leader!! && !it.scoreboardTags.contains("prison") }) {
                val failedItem = i.inventory.removeItemAnySlot(ItemStack(Material.EMERALD, tax))
                if (failedItem[0] != null) {
                    treasury.value!!.addItem(ItemStack(Material.EMERALD, tax - failedItem[0]!!.amount))
                    leader!!.sendMessage(i.name + "님이 세금 ${failedItem[0]!!.amount}개를 내지 못했습니다!")
                } else {
                    treasury.value!!.addItem(ItemStack(Material.EMERALD, tax))
                }
            }
        }
    }
}

class ChangeMoney: BukkitRunnable(){
    override fun run() {
        for (player in players) {
            for (i in money) {
                var amount = 0
                for (j in player.inventory) {
                    if ((j != null && i.key.type == j.type && i.key.itemMeta == j.itemMeta)) {
                        amount += j.amount
                    }
                }

                if (amount >= i.key.amount) {
                    player.inventory.removeItemAnySlot(i.key)
                    player.inventory.addItem(ItemStack(Material.EMERALD, i.value))
                    break
                }
            }
        }
    }
}
