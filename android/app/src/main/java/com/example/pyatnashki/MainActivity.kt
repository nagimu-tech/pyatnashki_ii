package com.example.pyatnashki

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pyatnashki.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentTheme: GameTheme

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

        currentTheme = ThemeManager.load(this)

        binding.btnShuffle.setOnClickListener { startGame() }
        binding.btnStyle.setOnClickListener { showStyleDialog() }

        binding.board.post {
            val padding = dp(10) * 2
            val gaps = dp(6) * 3
            tileSize = (binding.board.width - padding - gaps) / 4
            applyTheme()
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

    // ── Theme ────────────────────────────────────────────────────────────────────

    private fun applyTheme() {
        val t = currentTheme

        binding.root.setBackgroundColor(t.bgColor)
        binding.board.setBackgroundColor(t.boardBgColor)

        binding.boardOuter.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(t.boardBorderColor)
            cornerRadius = dp(6).toFloat()
        }

        binding.tvTitle.text = t.titleText
        binding.tvTitle.setTextColor(t.titleColor)
        binding.tvSubtitle.text = t.subtitleText
        binding.tvSubtitle.setTextColor(t.mutedColor)

        binding.tvMoves.setTextColor(t.statsColor)
        binding.tvTime.setTextColor(t.statsColor)
        binding.tvMovesLabel.setTextColor(t.mutedColor)
        binding.tvTimeLabel.setTextColor(t.mutedColor)
        binding.statsDivider.setBackgroundColor(t.mutedColor)

        val strokeList = ColorStateList.valueOf(t.btnStrokeColor)
        binding.btnShuffle.setTextColor(t.btnStrokeColor)
        binding.btnShuffle.strokeColor = strokeList
        binding.btnStyle.setTextColor(t.btnStrokeColor)
        binding.btnStyle.strokeColor = strokeList
    }

    private fun showStyleDialog() {
        val themes = ThemeManager.themes
        val currentIdx = themes.indexOfFirst { it.id == currentTheme.id }.coerceAtLeast(0)

        val items: Array<CharSequence> = themes.map { theme ->
            SpannableString("⬤  ${theme.name}").apply {
                setSpan(ForegroundColorSpan(theme.titleColor), 0, 1, 0)
            }
        }.toTypedArray()

        AlertDialog.Builder(this, R.style.WinDialog)
            .setTitle("Выбери стиль")
            .setSingleChoiceItems(items, currentIdx) { dialog, which ->
                currentTheme = themes[which]
                ThemeManager.save(this, currentTheme.id)
                applyTheme()
                renderBoard()
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .show()
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
        val t = currentTheme
        return Button(this).apply {
            text = value.toString()
            textSize = (tileSize / resources.displayMetrics.density / 4.2f).coerceIn(14f, 30f)
            setTextColor(if (isCorrect) t.correctTileTextColor else t.tileTextColor)
            typeface = when (t.typeface) {
                1 -> Typeface.SERIF
                2 -> Typeface.SANS_SERIF
                else -> Typeface.MONOSPACE
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(t.tileBgColor)
                setStroke(dp(1), if (isCorrect) t.correctTileBorderColor else t.tileBorderColor)
                cornerRadius = dp(t.tileCornerDp).toFloat()
            }
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
