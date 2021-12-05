@file:JvmName("Game")

package bazu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import kotlin.random.Random

var system: System? = null
var tax = 10
var leader: Player? = null
var voteNum = mutableMapOf<Player, Int>()
var YesNoVote = hashMapOf<Boolean, Int>(false to 0, true to 0)
var voted = 0
val gui = GUI()
var taxRunnable = TaxRunnable()
var voteRunnable = VoteRunnable()
var changeMoney = ChangeMoney()
var attackRunnable = AttackRunnable()

var attack = Attack()
var attacking = false

lateinit var dst: mcSociety
val inst = lazy { dst }

var playerHeads: ArrayList<ItemStack> = arrayListOf()

fun setplayerHeads(){
    playerHeads.clear()

    for (i in Bukkit.getOnlinePlayers()) {
        val playerHead = ItemStack(Material.PLAYER_HEAD)
        val meta = playerHead.itemMeta
        if (meta is SkullMeta) {
            meta.owningPlayer = i
            playerHead.itemMeta = meta
        }
        playerHeads.add(playerHead)
    }
}

var taxTerm = (tax*Bukkit.getOnlinePlayers().size/20+1).toDouble()

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

val money = hashMapOf(ItemStack(Material.DARK_OAK_LOG, 15) to 3,
    ItemStack(Material.COBBLESTONE, 15) to 4, ItemStack(Material.COD, 1) to 1,
    ItemStack(Material.SALMON, 1) to 3, ItemStack(Material.TROPICAL_FISH, 1) to 5,
    ItemStack(Material.PUFFERFISH, 1) to 4, ItemStack(Material.NAUTILUS_SHELL, 1) to 4,
    ItemStack(Material.NAME_TAG, 1) to 4)

var revolutioning = false

fun initalTeams(){
    revolutionTeam.value!!.setAllowFriendlyFire(false)
    revolutionTeam.value!!.color(NamedTextColor.BLUE)

    leaderTeam.value!!.setAllowFriendlyFire(false)
    leaderTeam.value!!.color(NamedTextColor.RED)
}

fun resetVoteNum(){
    for (i in Bukkit.getOnlinePlayers()){
        voteNum[i] = 0
    }
    voted = 0
    YesNoVote.put(true, 0)
    YesNoVote.put(false, 0)
}

fun gameStart(){

    taxRunnable = TaxRunnable()
    voteRunnable = VoteRunnable()
    changeMoney = ChangeMoney()
    attackRunnable = AttackRunnable()

    for (i in Bukkit.getOnlinePlayers()){
        voteNum.put(i, 0)
        i.inventory.clear()
        i.gameMode = GameMode.ADVENTURE
        i.teleport(Location(Bukkit.getWorld("world"), 1.5, 64.0, 0.5))
        i.sendExperienceChange(0.0f, 0)
    }

    setplayerHeads()
    initalTeams()
    resetVoteNum()
    attack.resetNums()

    Bukkit.broadcast(Component.text("세금은 이제부터 ${taxTerm}분마다 걷어집니다!"))

    taxRunnable.runTaskTimer(inst.value, (taxTerm*20*60).toLong(), (taxTerm*20*60).toLong())
    changeMoney.runTaskTimer(inst.value, 0, 10)
    attackRunnable.runTaskTimer(inst.value, (20*60*4).toLong(), (20*60*3).toLong())

    system = System.DICTORSHIP
    Bukkit.broadcast(Component.text("오늘도 독재사회에서 하루를 살아갑니다."))
    setLeaderWithMessage(Bukkit.getOnlinePlayers().random())

    leader?.addScoreboardTag("leader")
}

fun gameEnd(){
    system = null
    tax = 5
    leader = null
    voteNum.clear()

    voted = 0
    playerHeads.clear()

    var sum = 0
    for (i in Bukkit.getOnlinePlayers().first().inventory.filter {it != null &&
        it.type == Material.EMERALD
    }) sum += i.amount

    var winner: MutableMap<Player, Int> = mutableMapOf(Bukkit.getOnlinePlayers().first()
            to sum)

    attack.resetNums()

    for (i in Bukkit.getOnlinePlayers()){
        i.removeScoreboardTag("leader")
        i.removeScoreboardTag("prison")
        i.removeScoreboardTag("conference")

        sum = 0
        for (j in i.inventory.filter { it != null && it.type == Material.EMERALD }){
            sum += j.amount
        }
        if (winner.values.first() < sum){
            winner.clear()
            winner = mutableMapOf(i
                    to sum)
        }else if (winner.values.first() == sum){
            winner = mutableMapOf(i
                    to sum)
        }
    }
    Bukkit.broadcast(Component.text("게임이 끝났습니다!"))
    Bukkit.broadcast(Component.text("우승자는..."))
    for (i in winner)Bukkit.broadcast(Component.text("${i.key.name}님이 ${i.value}개의 에메랄드로 1등 입니다!"))

    Bukkit.getScheduler().cancelTasks(inst.value)
}

fun setLeaderWithMessage(player: Player){
    Bukkit.broadcast(Component.text("리더는 "+player.name+"입니다!"))
    if (leader != null) leader!!.removeScoreboardTag("leader")
    leader = player
    player.addScoreboardTag("leader")
}

enum class System(val label: String){
    DICTORSHIP("독재"),
    DEMOCRACY("민주주의")
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
    if (player != leader!!){
        revolutionTeam.value!!.addEntry(player.name)
        leaderTeam.value!!.addEntry(leader!!.name)
    }

    Bukkit.broadcast(Component.text("${player.name}님이 혁명을 일으켰습니다!"))
    player.addScoreboardTag("revolutionFirst")

    val array = arrayListOf<Player>()
    array.addAll(Bukkit.getOnlinePlayers().filter {
        !revolutionTeam.value!!.entries.contains(it.name) && !leaderTeam.value!!.entries.contains(it.name)
                && !it.scoreboardTags.contains("prison")
    })

    Bukkit.getWorld("world")!!.setGameRule(GameRule.KEEP_INVENTORY, false)

    Windows.REVOLUTION.run(array)
    Windows.REVOLUTION.after()
}

enum class SucceedProperty{
    SUCCEED, NOT_YET, FAILED
}

class TaxRunnable: BukkitRunnable(){
    override fun run() {
        if (!revolutioning) {
            Bukkit.broadcast(Component.text("세금을 징수하겠습니다!"))
            for (i in Bukkit.getOnlinePlayers().filter { it != leader!! && !it.scoreboardTags.contains("prison") }) {
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

class VoteRunnable: BukkitRunnable(){
    override fun run() {
        Bukkit.broadcast(Component.text("3분 후 투표가 시작됩니다! 선거유세를 해 주세요!"))

        class RealVote: BukkitRunnable(){
            override fun run() {
                if (system == System.DEMOCRACY && !revolutioning) {
                    val players = arrayListOf<Player>()
                    players.addAll(Bukkit.getOnlinePlayers())
                    Windows.LEADER_VOTE.run(players)
                }
            }

        }

        val realVote = RealVote()

        realVote.runTaskLater(inst.value, (3*20*60).toLong())
    }
}

class ChangeMoney: BukkitRunnable(){
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            for (i in money) {
                var amount = 0
                for (j in player.inventory) {
                    if ((j != null && i.key.type == j.type)) {
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
