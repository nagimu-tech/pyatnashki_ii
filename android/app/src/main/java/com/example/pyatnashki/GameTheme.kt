package com.example.pyatnashki

data class GameTheme(
    val id: String,
    val name: String,
    val bgColor: Int,
    val boardBgColor: Int,
    val boardBorderColor: Int,
    val tileBgColor: Int,
    val tileBorderColor: Int,
    val tileTextColor: Int,
    val correctTileBorderColor: Int,
    val correctTileTextColor: Int,
    val titleColor: Int,
    val statsColor: Int,
    val mutedColor: Int,
    val btnStrokeColor: Int,
    val titleText: String,
    val subtitleText: String,
    val tileCornerDp: Int,
    val typeface: Int,   // 0=MONOSPACE, 1=SERIF, 2=SANS_SERIF
)
