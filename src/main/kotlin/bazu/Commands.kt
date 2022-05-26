package bazu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class Commands: CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        for (i in CommandsLabel.values()){
            if (command.label.equals(i.label, ignoreCase = true)){
                if (i.needLeader){
                    if (leader != null) i.run(sender, args)
                }else{
                    i.run(sender, args)
                }
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        val list = arrayListOf<String>()

        if (command.label.equals(CommandsLabel.GAME.label, ignoreCase = true)){
            list.add("시작")
            list.add("끝")
        }

        return list
    }
}

enum class CommandsLabel(val label: String, val needLeader: Boolean = true): CommandsLabelFun{
    GAME("게임", false){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (args.size >= 1){
                if (args[0].equals("시작", ignoreCase = true) && args.size == 2){
                    val goalTime = args[1].toIntOrNull()
                    if (goalTime != null)
                        gameStart(goalTime)

                }
                else if (args[0].equals("끝", ignoreCase = true) && leader != null) gameEnd()
            }
        }
    },
    LEADER_RIGHT("leaderRight"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            Windows.LEADER_DECISION.run(arrayListOf(leader!!))
        }
    },
    COUNTRY_INFO("국가정보"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            sender.sendMessage("현재 지도자: ${leader!!.name}")
            sender.sendMessage("현재 세금: ${tax}")
            sender.sendMessage("현재 세금 기간: ${taxTerm}분")
            sender.sendMessage("현재 체제: ${system!!.label}")
        }
    },
    VOTE("vote"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (gui.unacceptedPolicy != null && sender is Player && sender == leader!!) {
                gui.subject = gui.unacceptedPolicy!!.item.displayName()
                val array = arrayListOf<Player>()
                array.addAll(players.filter { it.scoreboardTags.contains("conference") })

                Windows.YES_OR_NO_VOTE.run(array)
            }
        }
    },
    FINISH_MEETING("finishMeeting"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (sender is Player && sender == leader!!){
                for (i in players) i.removeScoreboardTag("conference")
                Bukkit.broadcast(Component.text("회의가 취소되었습니다!"))
            }
        }
    },
    REVOLUTION("혁명"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (sender is Player && !sender.scoreboardTags.contains("prison") && sender != leader!!){
                if (canRevolution) {
                    revolution(sender)
                    canRevolution = false
                    revolutionTimer = 0

                    object:BukkitRunnable(){
                        override fun run() {
                            revolutionTimer++
                            if (revolutionTimer >= 60*2){
                                canRevolution = true
                                this.cancel()
                            }
                        }
                    }.runTaskTimer(inst.value, 0, 20)
                }else{
                    sender.sendMessage("지금은 반란을 일으킬 수 없습니다! ${60*2-revolutionTimer}초 후부터 반란을 일으킬 수 있습니다!")
                }
            }
        }
    },
    N("N", false){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (args.size == 1 && sender is Player){
                sender.sendMessage(Component.text("<${sender.name}> ${args[0]}"
                    , TextColor.color(0x80, 0x80, 0x80), TextDecoration.ITALIC))
                for (i in sender.getNearbyEntities(2.0, 2.0, 2.0))
                    i.sendMessage(Component.text("<${sender.name}> ${args[0]}"
                        , TextColor.color(0x80, 0x80, 0x80), TextDecoration.ITALIC))
            }
        }
    },
    CALL_ATTACK("callAttack", true){
        override fun run(sender: CommandSender, args: Array<out String>) {
            attack.attack()
        }
    }
}

private interface CommandsLabelFun {
    fun run(sender: CommandSender, args: Array<out String>)
}