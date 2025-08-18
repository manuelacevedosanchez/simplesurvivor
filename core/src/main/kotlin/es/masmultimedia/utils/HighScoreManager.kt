package es.masmultimedia.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

class HighScoreManager {
    private val prefs: Preferences = Gdx.app.getPreferences("HighScores")
    private val maxScores = 10 // Máximo de puntuaciones altas a guardar

    fun getHighScores(): List<Pair<String, Int>> {
        val scores = mutableListOf<Pair<String, Int>>()
        for (i in 1..maxScores) {
            val name = prefs.getString("name$i", "")
            val score = prefs.getInteger("score$i", 0)
            if (name.isNotEmpty()) {
                scores.add(Pair(name, score))
            }
        }
        return scores
    }

    fun addHighScore(name: String, score: Int) {
        val scores = getHighScores().toMutableList()
        scores.add(Pair(name, score))
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
        }
        prefs.flush()
    }
}
