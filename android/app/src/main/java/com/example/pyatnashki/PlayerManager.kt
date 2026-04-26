package com.example.pyatnashki

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PlayerManager {

    data class GameRecord(
        val moves: Int,
        val seconds: Int,
        val themeId: String,
        val timestamp: Long
    )

    fun getName(ctx: Context): String = prefs(ctx).getString("name", "") ?: ""

    fun setName(ctx: Context, name: String) {
        prefs(ctx).edit().putString("name", name).apply()
    }

    fun getWins(ctx: Context): Int = prefs(ctx).getInt("wins", 0)

    fun getBestMoves(ctx: Context): Int = prefs(ctx).getInt("best_moves", Int.MAX_VALUE)

    fun getBestTime(ctx: Context): Int = prefs(ctx).getInt("best_time", Int.MAX_VALUE)

    fun addRecord(ctx: Context, moves: Int, seconds: Int, themeId: String) {
        val p = prefs(ctx)
        val wins = p.getInt("wins", 0) + 1
        val bestMoves = minOf(p.getInt("best_moves", Int.MAX_VALUE), moves)
        val bestTime  = minOf(p.getInt("best_time",  Int.MAX_VALUE), seconds)

        val old = runCatching { JSONArray(p.getString("history", "[]")) }.getOrElse { JSONArray() }
        val entry = JSONObject().apply {
            put("moves", moves)
            put("seconds", seconds)
            put("theme", themeId)
            put("ts", System.currentTimeMillis())
        }
        val arr = JSONArray().put(entry)
        for (i in 0 until minOf(old.length(), 19)) arr.put(old.getJSONObject(i))

        p.edit()
            .putInt("wins", wins)
            .putInt("best_moves", bestMoves)
            .putInt("best_time", bestTime)
            .putString("history", arr.toString())
            .apply()
    }

    fun getHistory(ctx: Context): List<GameRecord> {
        val arr = runCatching {
            JSONArray(prefs(ctx).getString("history", "[]"))
        }.getOrElse { JSONArray() }
        return (0 until arr.length()).map { i ->
            arr.getJSONObject(i).let { obj ->
                GameRecord(
                    moves     = obj.getInt("moves"),
                    seconds   = obj.getInt("seconds"),
                    themeId   = obj.optString("theme", ""),
                    timestamp = obj.optLong("ts", 0L)
                )
            }
        }
    }

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
}
