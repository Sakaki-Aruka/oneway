package online.aruka.oneway.obj

enum class Loader(val v: String) {
    BABRIC("babric"),
    BTA_BABRIC("bta-babric"),
    BUKKIT("bukkit"),
    BUNGEECORD("bungeecord"),
    CANVAS("canvas"),
    DATAPACK("datapack"),
    FABRIC("fabric"),
    FOLIA("folia"),
    FORGE("forge"),
    IRIS("iris"),
    JAVA_AGENT("java-agent"),
    LEGACY_FABRIC("legacy-fabric"),
    LITELOADER("liteloader"),
    MINECRAFT("minecraft"),
    MODLOADER("modloader"),
    NEOFORGE("neoforge"),
    NILLOADER("nilloader"),
    OPTIFINE("optifine"),
    ORNITHE("ornithe"),
    PAPER("paper"),
    PURPUR("purpur"),
    QUILT("quilt"),
    RIFT("rift"),
    SPIGOT("spigot"),
    SPONGE("sponge"),
    VANILLA("vanilla"),
    VELOCITY("velocity"),
    WATERFALL("waterfall"),
    ;

    fun isMod(): Boolean = this in MOD_LOADERS
    fun isPlugin(): Boolean = this in PLUGIN_PLATFORMS
    fun isProxy(): Boolean = this in PROXY_PLATFORMS
    fun isVanilla(): Boolean = this in VANILLA_PLATFORMS
    fun isOther(): Boolean = !isMod() && !isPlugin() && !isProxy() && !isVanilla()



    companion object {
        private val MOD_LOADERS = setOf(
            BABRIC, BTA_BABRIC, CANVAS, FABRIC, FORGE, IRIS,
            LEGACY_FABRIC, LITELOADER, MODLOADER, NEOFORGE,
            NILLOADER, OPTIFINE, ORNITHE, QUILT, RIFT,
        )
        private val PLUGIN_PLATFORMS = setOf(
            BUKKIT, FOLIA, PAPER, PURPUR, SPIGOT, SPONGE,
        )
        private val PROXY_PLATFORMS = setOf(
            BUNGEECORD, VELOCITY, WATERFALL,
        )
        private val VANILLA_PLATFORMS = setOf(
            DATAPACK, MINECRAFT, VANILLA,
        )
    }
}