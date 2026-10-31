package com.coderxi.pulgin.paidfly.service

import com.coderxi.pulgin.paidfly.PaidFlyPlugin.Companion.plugin
import com.coderxi.pulgin.paidfly.utils.isOnGroundByServerCheck
import io.papermc.paper.scoreboard.numbers.NumberFormat
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.util.Locale
import java.util.UUID

/** A separate board lets us restore the player's previous board when flight ends. */
object FlightScoreboard {
    private data class Session(val previous: Scoreboard, val board: Scoreboard)

    private val sessions = mutableMapOf<UUID, Session>()
    private val trackedPlayers = mutableSetOf<UUID>()
    private var updateTask: BukkitTask? = null
    private val entries = listOf("§0", "§1")

    fun startTracking(player: Player) {
        if (!plugin.config.getBoolean("Scoreboard.Enable", true)) return
        trackedPlayers.add(player.uniqueId)
        syncVisibility(player)
        if (updateTask == null) {
            var ticks = 0
            updateTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
                ticks++
                val iterator = trackedPlayers.iterator()
                while (iterator.hasNext()) {
                    val uuid = iterator.next()
                    val onlinePlayer = Bukkit.getPlayer(uuid)
                    if (onlinePlayer == null || !onlinePlayer.isOnline) {
                        iterator.remove()
                        sessions.remove(uuid)
                        continue
                    }
                    syncVisibility(onlinePlayer)
                    val session = sessions[uuid]
                    if (ticks % 20 == 0 && session != null && onlinePlayer.scoreboard === session.board) {
                        update(onlinePlayer, session.board)
                    }
                }
                if (trackedPlayers.isEmpty()) {
                    updateTask?.cancel()
                    updateTask = null
                }
            }, 1L, 1L)
        }
    }

    fun stopTracking(player: Player) {
        trackedPlayers.remove(player.uniqueId)
        hide(player)
        if (trackedPlayers.isEmpty()) {
            updateTask?.cancel()
            updateTask = null
        }
    }

    private fun syncVisibility(player: Player) {
        val inPaidFlight = player.isFlying && !player.isGliding && !player.isOnGround && !player.isOnGroundByServerCheck
        if (inPaidFlight && !sessions.containsKey(player.uniqueId)) show(player)
        else if (!inPaidFlight) hide(player)
    }

    private fun show(player: Player) {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objective = board.registerNewObjective("paidfly", "dummy", plugin.localizer.get("Scoreboard.Title"))
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.numberFormat(NumberFormat.blank())
        entries.forEachIndexed { index, entry ->
            board.registerNewTeam("paidfly_$index").addEntry(entry)
            objective.getScore(entry).score = entries.size - index
        }
        sessions[player.uniqueId] = Session(player.scoreboard, board)
        player.scoreboard = board
        update(player, board)
    }

    private fun hide(player: Player) {
        val session = sessions.remove(player.uniqueId) ?: return
        // Another plugin may have replaced our board in the meantime.
        if (player.scoreboard === session.board) player.scoreboard = session.previous
    }

    private fun update(player: Player, board: Scoreboard) {
        val rule = PayService.getPayRuleByPlayer(player)
        val balance = PayService.getPayTypeByName(rule.payType)?.getValue(player) ?: 0.0
        val seconds = PaidFlyService.getRemainingFlySeconds(player).coerceAtLeast(0)
        val time = String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
        val localizer = plugin.localizer
        val lines: List<Component> = listOf(
            localizer.getAndReplaceArgs("Scoreboard.Balance", "pay_type" to rule.payTypeLocalName, "balance" to String.format(Locale.ROOT, "%.2f", balance)),
            localizer.getAndReplaceArgs("Scoreboard.Remaining", "time" to time)
        )
        lines.forEachIndexed { index, line -> board.getTeam("paidfly_$index")?.prefix(line) }
    }
}
