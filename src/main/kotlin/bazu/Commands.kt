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

val commands = arrayOf("game", "leaderRight", "countryInfo", "vote", "finishMeeting", "revolution", "n")

class Commands: CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.label.equals(commands[0], ignoreCase = true) && args.size == 1){
            if (args[0].equals("start", ignoreCase = true)) gameStart()
            else if (args[0].equals("end", ignoreCase = true)) gameEnd()
        }

        else if (command.label.equals(commands[6], ignoreCase = true)){
            if (args.size == 1 && sender is Player){
                sender.sendMessage(Component.text("<${sender.name}> ${args[0]}"
                    , TextColor.color(0x80, 0x80, 0x80), TextDecoration.ITALIC))
                for (i in sender.getNearbyEntities(2.0, 2.0, 2.0))
                    i.sendMessage(Component.text("<${sender.name}> ${args[0]}"
                        , TextColor.color(0x80, 0x80, 0x80), TextDecoration.ITALIC))
            }
        }

        if (leader != null) {
            if (command.label.equals(commands[1], ignoreCase = true)) Windows.LEADER_DECISION.run(arrayListOf(leader!!))


            else if (command.label.equals(commands[2], ignoreCase = true)) {
                sender.sendMessage("현재 지도자: ${leader!!.name}")
                sender.sendMessage("현재 세금: ${tax}")
                sender.sendMessage("현재 체제: ${system!!.label}")
            }


            else if (command.label.equals(commands[3], ignoreCase = true)){
                if (gui.unacceptedPolicy != null && sender is Player && sender == leader!!) {
                    gui.subject = gui.unacceptedPolicy!!.item.displayName()
                    val array = arrayListOf<Player>()
                    array.addAll(Bukkit.getOnlinePlayers().filter { it.scoreboardTags.contains("conference") })

                    Windows.YES_OR_NO_VOTE.run(array)
                }
            }


            else if (command.label.equals(commands[4], ignoreCase = true) && sender is Player && sender == leader!!){
                for (i in Bukkit.getOnlinePlayers()) i.removeScoreboardTag("conference")
                Bukkit.broadcast(Component.text("회의가 취소되었습니다!"))
            }


            else if (command.label.equals(commands[5], ignoreCase = true)){
                if (sender is Player && !sender.scoreboardTags.contains("prison") && sender != leader!!){
                    if (sender.inventory.itemInMainHand.type == Material.EMERALD && sender.inventory.itemInMainHand.amount > 32) {
                        sender.inventory.itemInMainHand.amount -= 32
                        revolution(sender)
                    }else{
                        sender.sendMessage(Component.text("에메랄드 32개를 들고 해 주세요"))
                    }
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

        if (command.label.equals(commands[0], ignoreCase = true)){
            list.add("start")
            list.add("end")
        }

        return list
    }
}