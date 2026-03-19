package es.masmultimedia.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import java.util.Locale

/**
 * Internationalization manager for multi-language support.
 * Loads string bundles and provides easy access to localized text.
 */
object I18n {

    private lateinit var bundle: I18NBundle
    private var currentLocale: Locale = Locale.getDefault()

    // Supported locales
    val SUPPORTED_LOCALES = listOf(
        Locale.ENGLISH,
        Locale("es"), // Spanish
        Locale("pt"), // Portuguese
        Locale.FRENCH,
        Locale.GERMAN,
    )

    /**
     * Initialize i18n with system default locale or saved preference
     */
    fun init() {
        val prefs = Gdx.app.getPreferences("SimpleSurvivorSettings")
        val savedLang = prefs.getString("language", "")

        currentLocale = if (savedLang.isNotEmpty()) {
            Locale(savedLang)
        } else {
            // Use system locale if supported, otherwise default to English
            val systemLocale = Locale.getDefault()
            if (SUPPORTED_LOCALES.any { it.language == systemLocale.language }) {
                systemLocale
            } else {
                Locale.ENGLISH
            }
        }

        loadBundle()
    }

    /**
     * Load the string bundle for current locale
     */
    private fun loadBundle() {
        try {
            val baseFileHandle = Gdx.files.internal("i18n/strings")
            bundle = I18NBundle.createBundle(baseFileHandle, currentLocale)
            Gdx.app.log("I18n", "Loaded language: ${currentLocale.language}")
        } catch (e: Exception) {
            Gdx.app.error("I18n", "Failed to load bundle for ${currentLocale.language}, falling back to English", e)
            // Fallback to English
            try {
                val baseFileHandle = Gdx.files.internal("i18n/strings")
                bundle = I18NBundle.createBundle(baseFileHandle, Locale.ENGLISH)
            } catch (e2: Exception) {
                Gdx.app.error("I18n", "Failed to load English bundle", e2)
            }
        }
    }

    /**
     * Change the current language
     */
    fun setLanguage(locale: Locale) {
        currentLocale = locale
        loadBundle()

        // Save preference
        val prefs = Gdx.app.getPreferences("SimpleSurvivorSettings")
        prefs.putString("language", locale.language)
        prefs.flush()
    }

    /**
     * Get current language code
     */
    fun getCurrentLanguage(): String = currentLocale.language

    /**
     * Get localized string by key
     */
    fun get(key: String): String {
        return try {
            bundle.get(key)
        } catch (e: Exception) {
            Gdx.app.error("I18n", "Missing translation for key: $key")
            key // Return key as fallback
        }
    }

    /**
     * Get localized string with format arguments
     */
    fun format(key: String, vararg args: Any): String {
        return try {
            bundle.format(key, *args)
        } catch (e: Exception) {
            Gdx.app.error("I18n", "Missing translation or format error for key: $key")
            key
        }
    }

    // Common string keys as constants for type-safety
    object Keys {
        // Menu
        const val APP_NAME = "app_name"
        const val PLAY = "play"
        const val HIGH_SCORES = "high_scores"
        const val SETTINGS = "settings"
        const val INFO = "info"
        const val EXIT = "exit"
        const val BACK = "back"

        // Game UI
        const val SCORE = "score"
        const val TIME = "time"
        const val KILLS = "kills"
        const val PAUSED = "paused"
        const val RESUME = "resume"
        const val RESTART = "restart"
        const val MAIN_MENU = "main_menu"

        // Game Over
        const val GAME_OVER = "game_over"
        const val YOU_SURVIVED = "you_survived"
        const val FINAL_SCORE = "final_score"
        const val NEW_HIGH_SCORE = "new_high_score"
        const val PLAY_AGAIN = "play_again"

        // Settings
        const val SOUND = "sound"
        const val MUSIC = "music"
        const val LANGUAGE = "language"
        const val ON = "on"
        const val OFF = "off"
        const val SOUND_VOLUME = "sound_volume"
        const val MUSIC_VOLUME = "music_volume"
        const val LANG_EN = "lang_en"
        const val LANG_ES = "lang_es"
        const val LANG_PT = "lang_pt"

        // Shop
        const val SHOP = "shop"
        const val BUY = "buy"
        const val COST = "cost"
        const val OWNED = "owned"
        const val NOT_ENOUGH_POINTS = "not_enough_points"

        // Gameplay tips
        const val TIP_MOVEMENT = "tip_movement"
        const val TIP_AIMING = "tip_aiming"
        const val TIP_POWERUPS = "tip_powerups"
        const val TIP_SURVIVE = "tip_survive"
        const val TIP_STORM = "tip_storm"
        const val TIP_DRONES = "tip_drones"
        const val TIP_SHOP = "tip_shop"
        const val TIP_ENEMIES = "tip_enemies"

        // Power-ups
        const val POWERUP_SPEED = "powerup_speed"
        const val POWERUP_DAMAGE = "powerup_damage"
        const val POWERUP_SHIELD = "powerup_shield"
        const val POWERUP_HEAL = "powerup_heal"

        // Stats
        const val STAT_HP = "stat_hp"
        const val STAT_SPEED = "stat_speed"
        const val STAT_FIRE_RATE = "stat_fire_rate"
        const val STAT_ARMOR = "stat_armor"
        const val STAT_WEAPON = "stat_weapon"
    }
}

