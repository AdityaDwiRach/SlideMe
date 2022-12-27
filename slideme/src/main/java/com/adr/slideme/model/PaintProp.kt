package com.adr.slideme.model

import android.graphics.Paint
import android.graphics.Typeface

data class PaintProp(
    var color: Int? = null,
    var antiAlias: Boolean = false,
    var style: Paint.Style? = null,
    var strokeWidth: Float? = null,
    var strokeCap: Paint.Cap? = null,
    var typeface: Typeface? = null,
    var textSize: Float? = null
)
