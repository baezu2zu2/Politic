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
            list.add("start")
            list.add("end")
        }

        return list
    }
}

enum class CommandsLabel(val label: String, val needLeader: Boolean = true): CommandsLabelFun{
    GAME("game", false){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (args.size == 1){
                if (args[0].equals("start", ignoreCase = true)) gameStart()
                else if (args[0].equals("end", ignoreCase = true)) gameEnd()
            }
        }
    },
    LEADER_RIGHT("leaderRight"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            Windows.LEADER_DECISION.run(arrayListOf(leader!!))
        }
    },
    COUNTRY_INFO("countryInfo"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            sender.sendMessage("현재 지도자: ${leader!!.name}")
            sender.sendMessage("현재 세금: ${tax}")
            sender.sendMessage("현재 세금 기간: ${taxTerm}")
            sender.sendMessage("현재 체제: ${system!!.label}")
        }
    },
    VOTE("vote"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (gui.unacceptedPolicy != null && sender is Player && sender == leader!!) {
                gui.subject = gui.unacceptedPolicy!!.item.displayName()
                val array = arrayListOf<Player>()
                array.addAll(Bukkit.getOnlinePlayers().filter { it.scoreboardTags.contains("conference") })

                Windows.YES_OR_NO_VOTE.run(array)
            }
        }
    },
    FINISH_MEETING("finishMeeting"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (sender is Player && sender == leader!!){
                for (i in Bukkit.getOnlinePlayers()) i.removeScoreboardTag("conference")
                Bukkit.broadcast(Component.text("회의가 취소되었습니다!"))
            }
        }
    },
    REVOLUTION("revolution"){
        override fun run(sender: CommandSender, args: Array<out String>) {
            if (sender is Player && !sender.scoreboardTags.contains("prison") && sender != leader!!){
                sender.inventory.itemInMainHand.amount -= 32
                revolution(sender)
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