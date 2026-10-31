package com.coderxi.pulgin.paidfly

import com.coderxi.pulgin.paidfly.utils.Localizer
import com.coderxi.pulgin.paidfly.command.PaidFlyCmdExecutor
import com.coderxi.pulgin.paidfly.command.PaidFlyTabCompleter
import com.coderxi.pulgin.paidfly.expansion.PaidFlyPAPIExpansion
import com.coderxi.pulgin.paidfly.service.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Path

class PaidFlyPlugin : JavaPlugin() {

    companion object {
        val plugin: PaidFlyPlugin get() = getPlugin(PaidFlyPlugin::class.java)
    }
    lateinit var localizer: Localizer
    private val services = listOf(PaidFlyService, PayService)

    override fun onLoad() {
        saveDefaultConfig()
        reloadConfig()
        localizer = Localizer.of(plugin) { "lang/" + plugin.config.getString("Language") }
    }

    override fun onEnable() {
        if (!File(dataFolder, "lang/zh-TW.yml").exists()) {
            saveResource("lang/zh-TW.yml", false)
        }
        localizer.reload()
        services.forEach { it.init() }
        getCommand("paidfly")?.setExecutor(PaidFlyCmdExecutor)
        getCommand("paidfly")?.tabCompleter = PaidFlyTabCompleter()
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            PaidFlyPAPIExpansion().register()
        }
    }

    fun reload() {
        reloadConfig()
        localizer.reload()
        services.forEach { it.reload() }
    }

}
