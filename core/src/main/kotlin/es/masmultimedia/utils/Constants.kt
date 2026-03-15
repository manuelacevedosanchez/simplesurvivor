package es.masmultimedia.utils

object Constants {
    const val MAX_ACTIVE_ENEMIES = 75
    const val MAX_ACTIVE_PROJECTILES = 160
    const val MAX_ACTIVE_POWER_UPS = 40

    // Reserved for upcoming enemy shooters.
    const val MAX_ACTIVE_ENEMY_PROJECTILES = 120

    // Timed asteroid storm event.
    const val ASTEROID_STORM_INTERVAL_MS = 60_000L
    const val ASTEROID_STORM_DURATION_MS = 15_000L
    const val ASTEROID_STORM_SPAWN_INTERVAL_MS = 400L

    // Score milestone shop.
    const val SHOP_FIRST_MILESTONE_SCORE = 2_000
    const val SHOP_MILESTONE_STEP_SCORE = 2_000

    const val MAX_PROJECTILE_TRAVEL_DISTANCE = 2400f
    const val MAX_PROJECTILE_LIFETIME_MS = 8000L

    // Score awarded per enemy type.
    // NORMAL  – baseline enemy, slow, 100 HP.
    // FAST    – low HP but hard to hit due to high speed: moderate bonus.
    // STRONG  – high HP, requires many hits: highest reward.
    const val SCORE_ENEMY_NORMAL = 100
    const val SCORE_ENEMY_FAST   = 150
    const val SCORE_ENEMY_STRONG = 300
}
