package bazu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit

class LeaderWinTimer(var goalTime: Int) :Thread(){

    var cancel = false
    var stop = false

    override fun run() {
        cancel = false
        stop = false
        while (!cancel) {
            if (!stop) {
                leaderTimer[leader!!] = leaderTimer[leader!!]!!+1
                when (leaderTimer[leader!!]) {
                    20*60 * (goalTime-10) ->
                        Bukkit.broadcast(
                            Component.text(
                                "지도자의 승리까지 10분 남았습니다!", TextColor.color(
                                    0xff, 0x3b, 0x1f
                                )
                            )
                        )
                    20*60 * (goalTime-5) ->
                        Bukkit.broadcast(
                            Component.text(
                                "지도자의 승리까지 5분 남았습니다!", TextColor.color(
                                    0xf4, 0x00, 0x00
                                )
                            )
                        )
                    20*60 * (goalTime-3) ->
                        Bukkit.broadcast(
                            Component.text(
                                "지도자의 승리까지 3분 남았습니다!", TextColor.color(
                                    0xdf, 0x00, 0x00
                                )
                            )
                        )
                    20*(60 * (goalTime-1) + 30) ->
                        Bukkit.broadcast(
                            Component.text(
                                "지도자의 승리까지 30초 남았습니다!", TextColor.color(
                                    0x52, 0x17, 0x0b
                                )
                            )
                        )
                    20*60 * goalTime ->
                        gameEnd()
                }
                sleep(1000/20)
            }
        }
    }
}