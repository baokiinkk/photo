package com.amb.photo.ui.activities.editor.adjust

import android.util.Log


fun getBrightness(brightness: Float): Float {
    val result = calcIntensity(
        intensity = brightness / 100f,
        minValue = -1f,
        maxValue = 1f,
        originValue = 0f
    )
    Log.d("BRIGHTNESS", "$result")
    return result
}

fun getContrast(contrast: Float): Float {
    val result = calcIntensity(
        intensity = contrast / 100f,
        minValue = 0.1f,
        maxValue = 5.0f,
        originValue = 1.0f
    )
    Log.d("CONTRAST", "$result")
    return result
}

fun getSaturation(
    saturation: Float
): Float {
    val result = calcIntensity(
        intensity = saturation / 100f,
        minValue = 0.0f,
        maxValue = 5.0f,
        originValue = 1.0f
    )
    Log.d("SATURATION", "$result")
    return result
}

fun getWarmth(
    warmth: Float
): Float {
    val result = calcIntensity(
        intensity = warmth / 100f,
        minValue = -1f,
        maxValue = 1f,
        originValue = 0.0f
    )
    Log.d("WARMTH", "$result")
    return result
}

fun getFade(
    fade: Float
): Float {
    val result = calcIntensity(
        intensity = fade / 100f,
        minValue = 0.0f,
        maxValue = 2.0f,
        originValue = 1.0f
    )
    Log.d("FADE", "$result")
    return result
}

fun getHighlight(
    highlight: Float
): Float {
    val result = calcIntensity(
        intensity = highlight / 100f,
        minValue = -100f,
        maxValue = 200f,
        originValue = 0.0f
    )
    Log.d("HIGHLIGHT", "$result")
    return result
}

fun getShadow(
    shadow: Float
): Float {
    val result = calcIntensity(
        intensity = shadow / 100f,
        minValue = -200f,
        maxValue = 100f,
        originValue = 0.0f
    )
    Log.d("SHADOW", "$result")
    return result
}

fun getHue(
    hue: Float
): Float {
    val result = calcIntensity(
        intensity = hue / 100f,
        minValue = 0.0f,
        maxValue = 6f,
        originValue = 0.0f
    )
    Log.d("HUE", "$result")
    return result
}

fun getVignette(
    vignette: Float
): Float {
    val result = calcIntensity(
        intensity = vignette / 100f,
        minValue = 0.0f,
        maxValue = 1f,
        originValue = 0.5f
    )
    Log.d("VIGNETTE", "$result")
    return result
}

fun getSharpen(
    sharpen: Float
): Float {
    val result = calcIntensity(
        intensity = sharpen / 100f,
        minValue = 0.0f,
        maxValue = 10f,
        originValue = 0.0f
    )
    Log.d("SHARPEN", "$result")
    return result
}

fun getGrain(
    grain: Float
): Float {
    val result = calcIntensity(
        intensity = grain / 100f,
        minValue = 0.0f,
        maxValue = 10f,
        originValue = 0.0f
    )
    Log.d("GRAIN", "$result")
    return result
}
