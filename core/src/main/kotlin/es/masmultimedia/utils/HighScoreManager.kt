package es.masmultimedia.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

class HighScoreManager {
    private val prefs: Preferences = Gdx.app.getPreferences("HighScores")
    private val maxScores = 10 // Máximo de puntuaciones altas a guardar

    /**
     * Returns a list of (name, score, timePlayedMs).
     * Entries saved before the time field was added will have timePlayedMs = 0.
     */
    fun getHighScores(): List<Triple<String, Int, Long>> {
        val scores = mutableListOf<Triple<String, Int, Long>>()
        for (i in 1..maxScores) {
            val name = prefs.getString("name$i", "")
            val score = prefs.getInteger("score$i", 0)
            val time = prefs.getLong("time$i", 0L)
            if (name.isNotEmpty()) {
                scores.add(Triple(name, score, time))
            }
        }
        return scores
    }

    fun addHighScore(name: String, score: Int, timePlayedMs: Long = 0L) {
        val scores = getHighScores().toMutableList()
        scores.add(Triple(name, score, timePlayedMs))
        // Ordenar las puntuaciones de mayor a menor
        scores.sortByDescending { it.second }
        // Limitar al máximo de puntuaciones altas
        if (scores.size > maxScores) {
            scores.removeAt(scores.lastIndex)
        }
        // Guardar las puntuaciones actualizadas
        for (i in 1..scores.size) {
            prefs.putString("name$i", scores[i - 1].first)
            prefs.putInteger("score$i", scores[i - 1].second)
            prefs.putLong("time$i", scores[i - 1].third)
        }
        prefs.flush()
    }

    fun clearHighScores() {
        prefs.clear()
        prefs.flush()
    }
}
