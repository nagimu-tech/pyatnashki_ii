package com.example.pyatnashki

import android.content.Context
import android.graphics.Color

object ThemeManager {

    val themes: List<GameTheme> = listOf(

        // 1. Хайтек (default)
        GameTheme(
            id = "hightech",
            name = "Хайтек",
            bgColor               = Color.parseColor("#FF050510"),
            boardBgColor          = Color.parseColor("#FF0A0A20"),
            boardBorderColor      = Color.parseColor("#FF0066FF"),
            tileBgColor           = Color.parseColor("#FF08082A"),
            tileBorderColor       = Color.parseColor("#3300F5FF"),
            tileTextColor         = Color.parseColor("#FF00F5FF"),
            correctTileBorderColor = Color.parseColor("#6600FF80"),
            correctTileTextColor  = Color.parseColor("#FF00FF80"),
            titleColor            = Color.parseColor("#FF00F5FF"),
            statsColor            = Color.parseColor("#FF00F5FF"),
            mutedColor            = Color.parseColor("#4400F5FF"),
            btnStrokeColor        = Color.parseColor("#FF00F5FF"),
            titleText             = "ПЯТНАШКИ",
            subtitleText          = "// SLIDING PUZZLE // HIGHTECH //",
            tileCornerDp          = 4,
            typeface              = 0,
        ),

        // 2. Synthwave
        GameTheme(
            id = "synthwave",
            name = "Synthwave",
            bgColor               = Color.parseColor("#FF120020"),
            boardBgColor          = Color.parseColor("#FF1A0030"),
            boardBorderColor      = Color.parseColor("#FFFF00CC"),
            tileBgColor           = Color.parseColor("#FF200035"),
            tileBorderColor       = Color.parseColor("#99AA00FF"),
            tileTextColor         = Color.parseColor("#FFFF00FF"),
            correctTileBorderColor = Color.parseColor("#99FFFF00"),
            correctTileTextColor  = Color.parseColor("#FFFFD700"),
            titleColor            = Color.parseColor("#FFFF00CC"),
            statsColor            = Color.parseColor("#FFFF00FF"),
            mutedColor            = Color.parseColor("#66FF00FF"),
            btnStrokeColor        = Color.parseColor("#FFFF00CC"),
            titleText             = "ПЯТНАШКИ",
            subtitleText          = "▸ SLIDING PUZZLE ▸ SYNTHWAVE ▸",
            tileCornerDp          = 2,
            typeface              = 0,
        ),

        // 3. Glassmorphism
        GameTheme(
            id = "glass",
            name = "Glassmorphism",
            bgColor               = Color.parseColor("#FFD0E8FF"),
            boardBgColor          = Color.parseColor("#CCE8F4FF"),
            boardBorderColor      = Color.parseColor("#FF8899BB"),
            tileBgColor           = Color.parseColor("#AAB8D4F8"),
            tileBorderColor       = Color.parseColor("#FF7799CC"),
            tileTextColor         = Color.parseColor("#FF334466"),
            correctTileBorderColor = Color.parseColor("#FF0055CC"),
            correctTileTextColor  = Color.parseColor("#FF0044AA"),
            titleColor            = Color.parseColor("#FF334466"),
            statsColor            = Color.parseColor("#FF334466"),
            mutedColor            = Color.parseColor("#88334466"),
            btnStrokeColor        = Color.parseColor("#FF7799CC"),
            titleText             = "Пятнашки",
            subtitleText          = "· sliding puzzle · glass ·",
            tileCornerDp          = 14,
            typeface              = 2,
        ),

        // 4. Pixel / Аркада
        GameTheme(
            id = "pixel",
            name = "Pixel / Аркада",
            bgColor               = Color.parseColor("#FF000000"),
            boardBgColor          = Color.parseColor("#FF001100"),
            boardBorderColor      = Color.parseColor("#FF00FF00"),
            tileBgColor           = Color.parseColor("#FF001800"),
            tileBorderColor       = Color.parseColor("#FF00CC00"),
            tileTextColor         = Color.parseColor("#FF00FF00"),
            correctTileBorderColor = Color.parseColor("#FFFFFF00"),
            correctTileTextColor  = Color.parseColor("#FFFFFF00"),
            titleColor            = Color.parseColor("#FF00FF00"),
            statsColor            = Color.parseColor("#FF00FF00"),
            mutedColor            = Color.parseColor("#6600FF00"),
            btnStrokeColor        = Color.parseColor("#FF00FF00"),
            titleText             = "ПЯТНАШКИ",
            subtitleText          = ">> SLIDING PUZZLE << ARCADE <<",
            tileCornerDp          = 0,
            typeface              = 0,
        ),

        // 5. Минимализм
        GameTheme(
            id = "minimal",
            name = "Минимализм",
            bgColor               = Color.parseColor("#FFF5F5F5"),
            boardBgColor          = Color.parseColor("#FFFFFFFF"),
            boardBorderColor      = Color.parseColor("#FFCCCCCC"),
            tileBgColor           = Color.parseColor("#FFFFFFFF"),
            tileBorderColor       = Color.parseColor("#FFDDDDDD"),
            tileTextColor         = Color.parseColor("#FF222222"),
            correctTileBorderColor = Color.parseColor("#FF0077AA"),
            correctTileTextColor  = Color.parseColor("#FF0077AA"),
            titleColor            = Color.parseColor("#FF222222"),
            statsColor            = Color.parseColor("#FF222222"),
            mutedColor            = Color.parseColor("#88444444"),
            btnStrokeColor        = Color.parseColor("#FF888888"),
            titleText             = "пятнашки",
            subtitleText          = "sliding puzzle",
            tileCornerDp          = 8,
            typeface              = 2,
        ),

        // 6. Дерево / Зен
        GameTheme(
            id = "zen",
            name = "Дерево / Зен",
            bgColor               = Color.parseColor("#FF3D2B1A"),
            boardBgColor          = Color.parseColor("#FF4A3520"),
            boardBorderColor      = Color.parseColor("#FF8B6040"),
            tileBgColor           = Color.parseColor("#FF6B4423"),
            tileBorderColor       = Color.parseColor("#FFA0693A"),
            tileTextColor         = Color.parseColor("#FFF5DEB3"),
            correctTileBorderColor = Color.parseColor("#FF90EE90"),
            correctTileTextColor  = Color.parseColor("#FF90EE90"),
            titleColor            = Color.parseColor("#FFF5DEB3"),
            statsColor            = Color.parseColor("#FFF5DEB3"),
            mutedColor            = Color.parseColor("#88F5DEB3"),
            btnStrokeColor        = Color.parseColor("#FFA0693A"),
            titleText             = "Пятнашки",
            subtitleText          = "～ скользящий пазл ～",
            tileCornerDp          = 6,
            typeface              = 1,
        ),

        // 7. Античность
        GameTheme(
            id = "antique",
            name = "Античность",
            bgColor               = Color.parseColor("#FF2C1810"),
            boardBgColor          = Color.parseColor("#FF3D2515"),
            boardBorderColor      = Color.parseColor("#FFC8A050"),
            tileBgColor           = Color.parseColor("#FF4A2E0A"),
            tileBorderColor       = Color.parseColor("#FFB8860B"),
            tileTextColor         = Color.parseColor("#FFFFD700"),
            correctTileBorderColor = Color.parseColor("#FFFF8C00"),
            correctTileTextColor  = Color.parseColor("#FFFF8C00"),
            titleColor            = Color.parseColor("#FFFFD700"),
            statsColor            = Color.parseColor("#FFFFD700"),
            mutedColor            = Color.parseColor("#88FFD700"),
            btnStrokeColor        = Color.parseColor("#FFC8A050"),
            titleText             = "ПЯТНАШКИ",
            subtitleText          = "⊕ PVZZLE ANTIQVVS ⊕",
            tileCornerDp          = 2,
            typeface              = 1,
        ),
    )

    fun save(ctx: Context, id: String) {
        ctx.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            .edit().putString("theme_id", id).apply()
    }

    fun load(ctx: Context): GameTheme {
        val id = ctx.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            .getString("theme_id", "hightech") ?: "hightech"
        return themes.firstOrNull { it.id == id } ?: themes.first()
    }
}
