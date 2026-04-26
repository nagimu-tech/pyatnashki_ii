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
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pyatnashki.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        binding.btnProfile.setOnClickListener { showProfileDialog() }

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
        binding.btnProfile.setTextColor(t.btnStrokeColor)
        binding.btnProfile.strokeColor = strokeList
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

    // ── Profile / Settings ───────────────────────────────────────────────────────

    private fun showProfileDialog() {
        val t = currentTheme
        val ctx = this

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(8))
        }

        // Name label
        root.addView(TextView(ctx).apply {
            text = "ИМЯ ИГРОКА"
            textSize = 10f
            setTextColor(t.mutedColor)
            letterSpacing = 0.12f
        })

        // Name input
        val editName = EditText(ctx).apply {
            setText(PlayerManager.getName(ctx))
            hint = "введите имя..."
            setTextColor(t.tileTextColor)
            setHintTextColor(t.mutedColor)
            textSize = 15f
            maxLines = 1
            setPadding(0, dp(2), 0, dp(6))
        }
        root.addView(editName)

        // Divider
        root.addView(makeDivider(t.mutedColor))

        // Stats
        val wins = PlayerManager.getWins(ctx)
        val bestMoves = PlayerManager.getBestMoves(ctx)
        val bestTime  = PlayerManager.getBestTime(ctx)

        val statsBuilder = StringBuilder()
        statsBuilder.append("ПОБЕД:  $wins\n")
        if (bestMoves < Int.MAX_VALUE) statsBuilder.append("ЛУЧШИЙ РЕЗУЛЬТАТ:  $bestMoves ходов\n")
        if (bestTime  < Int.MAX_VALUE) statsBuilder.append("ЛУЧШЕЕ ВРЕМЯ:  ${formatTime(bestTime)}\n")

        root.addView(TextView(ctx).apply {
            text = statsBuilder.toString().trimEnd('\n')
            textSize = 13f
            setTextColor(t.tileTextColor)
            setPadding(0, dp(4), 0, dp(4))
        })

        // History
        val history = PlayerManager.getHistory(ctx)
        if (history.isNotEmpty()) {
            root.addView(makeDivider(t.mutedColor))

            root.addView(TextView(ctx).apply {
                text = "ИСТОРИЯ ИГР"
                textSize = 10f
                setTextColor(t.mutedColor)
                letterSpacing = 0.12f
                setPadding(0, 0, 0, dp(4))
            })

            val histLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
            val fmt = SimpleDateFormat("dd.MM  HH:mm", Locale.getDefault())
            history.take(10).forEachIndexed { i, rec ->
                val dateStr = if (rec.timestamp > 0) fmt.format(Date(rec.timestamp)) else "—"
                histLayout.addView(TextView(ctx).apply {
                    text = "$dateStr  —  ${rec.moves} ход.  ${formatTime(rec.seconds)}"
                    textSize = 11f
                    setTextColor(if (i == 0) t.tileTextColor else t.mutedColor)
                    setPadding(0, dp(2), 0, dp(2))
                })
            }

            root.addView(ScrollView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(120)
                )
                addView(histLayout)
            })
        }

        AlertDialog.Builder(ctx, R.style.WinDialog)
            .setTitle("ПРОФИЛЬ")
            .setView(root)
            .setPositiveButton("СОХРАНИТЬ") { _, _ ->
                PlayerManager.setName(ctx, editName.text.toString().trim())
            }
            .setNegativeButton("ЗАКРЫТЬ") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun makeDivider(color: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
        ).apply { setMargins(0, dp(8), 0, dp(8)) }
        setBackgroundColor(color)
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
        val depth3d = if (t.is3D) (tileSize * 0.09f).toInt() else 0
        val hasEmoji = t.tileEmoji.isNotEmpty()
        val displayText = if (hasEmoji) "${t.tileEmoji}\n$value" else value.toString()

        return Button(this).apply {
            text = displayText
            textSize = when {
                hasEmoji -> (tileSize / resources.displayMetrics.density / 5.5f).coerceIn(11f, 22f)
                else     -> (tileSize / resources.displayMetrics.density / 4.2f).coerceIn(14f, 30f)
            }
            setTextColor(if (isCorrect) t.correctTileTextColor else t.tileTextColor)
            typeface = when (t.typeface) {
                1    -> Typeface.SERIF
                2    -> Typeface.SANS_SERIF
                else -> Typeface.MONOSPACE
            }
            background = if (t.is3D) {
                Tile3DDrawable(
                    baseColor   = if (isCorrect) mixColor(t.tileBgColor, t.correctTileBorderColor, 0.25f)
                                  else t.tileBgColor,
                    borderColor = if (isCorrect) t.correctTileBorderColor else t.tileBorderColor,
                    cornerPx    = dp(t.tileCornerDp).toFloat()
                )
            } else {
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(t.tileBgColor)
                    setStroke(dp(1), if (isCorrect) t.correctTileBorderColor else t.tileBorderColor)
                    cornerRadius = dp(t.tileCornerDp).toFloat()
                }
            }
            elevation = dp(if (t.is3D) 1 else 3).toFloat()
            tag = pos
            gravity = Gravity.CENTER
            isAllCaps = false
            if (t.is3D) setPadding(0, 0, depth3d, depth3d)
            else        setPadding(0, 0, 0, 0)
            layoutParams = FrameLayout.LayoutParams(tileSize, tileSize).apply {
                leftMargin = tileX(pos)
                topMargin  = tileY(pos)
            }
            setOnClickListener { onTileClick(pos) }
        }
    }

    private fun mixColor(c1: Int, c2: Int, ratio: Float): Int {
        val r = (Color.red(c1)   * (1 - ratio) + Color.red(c2)   * ratio).toInt()
        val g = (Color.green(c1) * (1 - ratio) + Color.green(c2) * ratio).toInt()
        val b = (Color.blue(c1)  * (1 - ratio) + Color.blue(c2)  * ratio).toInt()
        return Color.rgb(r, g, b)
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

        PlayerManager.addRecord(this, moveCount, elapsedSeconds, currentTheme.id)

        val name = PlayerManager.getName(this).let { if (it.isNotEmpty()) "$it!\n" else "" }

        AlertDialog.Builder(this, R.style.WinDialog)
            .setTitle("◈  ПОБЕДА")
            .setMessage(
                "${name}Пазл собран!\n\n" +
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
