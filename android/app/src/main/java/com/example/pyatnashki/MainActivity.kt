package com.example.pyatnashki

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pyatnashki.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val state = IntArray(16)
    private var emptyIndex = 15
    private var moveCount = 0
    private var elapsedSeconds = 0
    private var gameActive = false
    private var animating = false
    private var tileSize = 0

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (gameActive) {
                elapsedSeconds++
                binding.tvTime.text = formatTime(elapsedSeconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShuffle.setOnClickListener { startGame() }

        binding.board.post {
            val padding = dp(10) * 2
            val gaps = dp(6) * 3
            tileSize = (binding.board.width - padding - gaps) / 4
            startGame()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).run {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // ── Game logic ──────────────────────────────────────────────────────────────

    private fun startGame() {
        handler.removeCallbacks(timerRunnable)
        doShuffle()
        moveCount = 0
        elapsedSeconds = 0
        gameActive = true
        animating = false
        binding.tvMoves.text = "0"
        binding.tvTime.text = "00:00"
        renderBoard()
        handler.postDelayed(timerRunnable, 1000)
    }

    private fun doShuffle() {
        for (i in 0 until 15) state[i] = i + 1
        state[15] = 0
        emptyIndex = 15
        var prevEmpty = -1
        repeat(300 + (Math.random() * 200).toInt()) {
            val neighbors = getNeighbors(emptyIndex).filter { it != prevEmpty }
            val pick = neighbors[(Math.random() * neighbors.size).toInt()]
            state[emptyIndex] = state[pick]
            state[pick] = 0
            prevEmpty = emptyIndex
            emptyIndex = pick
        }
    }

    private fun getNeighbors(idx: Int): List<Int> = buildList {
        val r = idx / 4; val c = idx % 4
        if (r > 0) add(idx - 4)
        if (r < 3) add(idx + 4)
        if (c > 0) add(idx - 1)
        if (c < 3) add(idx + 1)
    }

    private fun isSolved(): Boolean {
        for (i in 0 until 15) if (state[i] != i + 1) return false
        return state[15] == 0
    }

    // ── Rendering ───────────────────────────────────────────────────────────────

    private fun tileX(idx: Int) = dp(10) + (idx % 4) * (tileSize + dp(6))
    private fun tileY(idx: Int) = dp(10) + (idx / 4) * (tileSize + dp(6))

    private fun renderBoard() {
        binding.board.removeAllViews()
        for (pos in 0 until 16) {
            if (state[pos] == 0) continue
            binding.board.addView(makeTile(pos, state[pos]))
        }
    }

    private fun makeTile(pos: Int, value: Int): Button {
        val isCorrect = value == pos + 1
        return Button(this).apply {
            text = value.toString()
            textSize = (tileSize / resources.displayMetrics.density / 4.2f).coerceIn(14f, 30f)
            setTextColor(if (isCorrect) Color.parseColor("#00FF80") else Color.parseColor("#00F5FF"))
            typeface = Typeface.MONOSPACE
            background = ContextCompat.getDrawable(
                this@MainActivity,
                if (isCorrect) R.drawable.tile_correct else R.drawable.tile_normal
            )
            elevation = dp(3).toFloat()
            tag = pos
            gravity = Gravity.CENTER
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            layoutParams = FrameLayout.LayoutParams(tileSize, tileSize).apply {
                leftMargin = tileX(pos)
                topMargin = tileY(pos)
            }
            setOnClickListener { onTileClick(pos) }
        }
    }

    // ── Interaction ─────────────────────────────────────────────────────────────

    private fun onTileClick(pos: Int) {
        if (!gameActive || animating || state[pos] == 0) return
        if (emptyIndex !in getNeighbors(pos)) return

        animating = true
        val tileView = binding.board.findViewWithTag<Button>(pos) ?: run {
            animating = false; return
        }

        val dx = (tileX(emptyIndex) - tileX(pos)).toFloat()
        val dy = (tileY(emptyIndex) - tileY(pos)).toFloat()

        tileView.animate()
            .translationXBy(dx)
            .translationYBy(dy)
            .setDuration(150)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    state[emptyIndex] = state[pos]
                    state[pos] = 0
                    emptyIndex = pos
                    moveCount++
                    binding.tvMoves.text = moveCount.toString()
                    renderBoard()
                    animating = false
                    if (isSolved()) showWin()
                }
            })
            .start()
    }

    // ── Win ─────────────────────────────────────────────────────────────────────

    private fun showWin() {
        gameActive = false
        handler.removeCallbacks(timerRunnable)

        AlertDialog.Builder(this, R.style.WinDialog)
            .setTitle("◈  ПОБЕДА")
            .setMessage(
                "Пазл собран!\n\n" +
                "Ходов:  $moveCount\n" +
                "Время:  ${formatTime(elapsedSeconds)}"
            )
            .setPositiveButton("// ЗАНОВО") { _, _ -> startGame() }
            .setNegativeButton("// СТОП") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    // ── Utils ────────────────────────────────────────────────────────────────────

    private fun formatTime(s: Int) = "%02d:%02d".format(s / 60, s % 60)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
