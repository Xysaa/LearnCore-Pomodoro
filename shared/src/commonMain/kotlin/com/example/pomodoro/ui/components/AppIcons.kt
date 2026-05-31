package com.example.pomodoro.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Centralized icon definitions — semua digambar dengan ImageVector.Builder + PathParser.
 * Tidak perlu dependency material-icons-extended.
 * Path data diambil dari Material Design Icons (Apache 2.0, viewport 24x24).
 */
object AppIcons {

    private fun svgIcon(name: String, vararg paths: String): ImageVector {
        val builder = ImageVector.Builder(
            name           = name,
            defaultWidth   = 24.dp,
            defaultHeight  = 24.dp,
            viewportWidth  = 24f,
            viewportHeight = 24f
        )
        for (d in paths) {
            val nodes = PathParser().parsePathString(d).toNodes()
            builder.addPath(
                pathData        = nodes,
                fill            = SolidColor(Color.Black),
                fillAlpha       = 1f,
                strokeAlpha     = 1f,
                strokeLineWidth = 0f,
                strokeLineCap   = StrokeCap.Butt,
                strokeLineJoin  = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType    = PathFillType.NonZero
            )
        }
        return builder.build()
    }

    // ── Navigation tabs ─────────────────────────────────────────
    val Focus: ImageVector get() = svgIcon("Focus",
        "M15 1H9v2h6V1zm-4 13h2V8h-2v6zm8.03-6.61l1.42-1.42c-.43-.51-.9-.99-1.41-1.41l-1.42 1.42C16.07 4.74 14.12 4 12 4c-4.97 0-9 4.03-9 9s4.02 9 9 9 9-4.03 9-9c0-2.12-.74-4.07-1.97-5.61zM12 20c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z"
    )
    val FocusFilled:   ImageVector get() = Focus
    val Timer:         ImageVector get() = Focus
    val TimerFilled:   ImageVector get() = Focus

    val Statistics: ImageVector get() = svgIcon("Statistics",
        "M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zM16.2 13h2.8v6h-2.8z"
    )
    val StatsFilled: ImageVector get() = Statistics

    val AirQuality: ImageVector get() = svgIcon("Air",
        "M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55zm7.45 3.91l-1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM20 10.5v2h3v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm-1 16.95h2V19.5h-2v2.95zm-7.45-3.91l1.41 1.41 1.79-1.8-1.41-1.41-1.79 1.8z"
    )
    val AirFilled:  ImageVector get() = AirQuality
    val AirGood:    ImageVector get() = AirQuality
    val AirBad: ImageVector get() = svgIcon("AirBad",
        "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z"
    )

    val Settings: ImageVector get() = svgIcon("Settings",
        "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94L14.4 2.81c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41L9.25 5.35c-.59.24-1.13.56-1.62.94L5.24 5.33c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.22-.07.47.12.61l2.03 1.58c-.05.3-.07.63-.07.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"
    )
    val SettingsFilled: ImageVector get() = Settings


    // ── BLE / Connectivity ───────────────────────────────────────
    val BleConnected: ImageVector get() = svgIcon("BleConnected",
        "M17.71 7.71L12 2h-1v7.59L6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 11 14.41V22h1l5.71-5.71-4.3-4.29 4.3-4.29zM13 5.83l1.88 1.88L13 9.59V5.83zm1.88 10.46L13 18.17v-3.76l1.88 1.88z"
    )
    val BleDisconnected: ImageVector get() = svgIcon("BleOff",
        "M13 5.83l1.88 1.88-1.88 1.88V5.83zM3.41 3L2 4.41l8.59 8.59L5 17.59 6.41 19 11 14.41V22h1l3.29-3.29 1.42 1.41L18.12 18.7l.88-.88L3.41 3zm9.47 12.59L11 20.17v-3.76l1.88 1.88v-.01z"
    )
    val BleScanning: ImageVector get() = BleConnected
    val NoConnection: ImageVector get() = svgIcon("WifiOff",
        "M2.28 3L1 4.27l2.1 2.1C1.91 7.28 1.14 8.06.46 9L2 11c.88-1.08 1.9-2.01 3.04-2.77L6.5 9.69C5.35 10.45 4.3 11.4 3.41 12.5L5 14.5c.88-1.1 1.93-2.06 3.15-2.82L9.69 13.2C8.59 13.9 7.61 14.83 6.83 15.92L8.5 18c.75-1.13 1.75-2.06 2.92-2.73l1.79 1.79C12.08 18.06 11 18.8 11 18.8v.01L12 22c0 0 2.49-1.16 4.2-2.84L4.31 7.31 2.28 3zM21 1.27l-2-2-3.09 3.09C14.89 1.55 13.48 1.24 12 1.24c-2.21 0-4.22.78-5.8 2.06L7.76 5c1.16-.9 2.6-1.45 4.2-1.45 1.6 0 3.07.57 4.24 1.5l1.43-1.43C18.38 4.36 17 5.82 17 5.82l1.5 1.5c.82-.91 1.56-1.88 2.5-2.7L21 4.27V3l-1 .27z"
    )

    // ── Timer Controls ───────────────────────────────────────────
    val Play:  ImageVector get() = svgIcon("Play",  "M8 5v14l11-7z")
    val Pause: ImageVector get() = svgIcon("Pause", "M6 19h4V5H6v14zm8-14v14h4V5h-4z")
    val Reset: ImageVector get() = svgIcon("Reset",
        "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"
    )
    val Stop: ImageVector get() = svgIcon("Stop", "M6 6h12v12H6z")

    // ── Sensors ──────────────────────────────────────────────────
    val Sensor: ImageVector get() = svgIcon("Sensor",
        "M12 12c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm6-1.8C18 6.57 15.35 4 12 4s-6 2.57-6 6.2c0 2.34 1.95 5.44 6 9.14 4.05-3.7 6-6.8 6-9.14zM12 22C7 17.36 4 13.46 4 10.2 4 5.22 7.8 2 12 2s8 3.22 8 8.2c0 3.26-3 7.16-8 11.8z"
    )
    val SensorFilled: ImageVector get() = Sensor
    val Warning: ImageVector get() = svgIcon("Warning",
        "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"
    )
    val CheckCircle: ImageVector get() = svgIcon("CheckCircle",
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
    )
    val Error: ImageVector get() = svgIcon("Error",
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
    )
    val Info: ImageVector get() = svgIcon("Info",
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"
    )

    // ── User Presence ─────────────────────────────────────────────
    val PersonPresent: ImageVector get() = svgIcon("PersonPin",
        "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"
    )
    val PersonAbsent: ImageVector get() = svgIcon("PersonOff",
        "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
    )
    val Person: ImageVector get() = PersonAbsent

    // ── Misc ──────────────────────────────────────────────────────
    val Notification: ImageVector get() = svgIcon("Notifications",
        "M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"
    )
    val NotificationFilled: ImageVector get() = Notification
    val Send:  ImageVector get() = svgIcon("Send",  "M2.01 21L23 12 2.01 3 2 10l15 2-15 2z")
    val Close: ImageVector get() = svgIcon("Close",
        "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
    )
    val Done:  ImageVector get() = svgIcon("Done",  "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
    val Flame: ImageVector get() = svgIcon("Flame",
        "M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z"
    )
    val Speaker: ImageVector get() = svgIcon("Speaker",
        "M17 2H7c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-5 2c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .89-2 2-2zm0 16c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
    )
    val History: ImageVector get() = svgIcon("History",
        "M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"
    )
    val Delete: ImageVector get() = svgIcon("Delete",
        "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
    )
    val Edit: ImageVector get() = svgIcon("Edit",
        "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
    )
    val Save: ImageVector get() = svgIcon("Save",
        "M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"
    )
    val Coffee: ImageVector get() = svgIcon("Coffee",
        "M20 3H4v10c0 2.21 1.79 4 4 4h6c2.21 0 4-1.79 4-4v-3h2c1.11 0 2-.89 2-2V5c0-1.11-.89-2-2-2zm0 5h-2V5h2v3zM4 19h16v2H4z"
    )
    val Chart: ImageVector get() = svgIcon("ShowChart",
        "M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"
    )
    val Download: ImageVector get() = svgIcon("Download",
        "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"
    )
    val Bolt: ImageVector get() = svgIcon("Bolt",
        "M11 21h-1l1-7H7.5c-.58 0-.57-.32-.38-.66.19-.34.05-.08.07-.12C8.48 10.94 10.42 7.54 13 3h1l-1 7h3.5c.49 0 .56.33.47.51l-.07.15C12.96 17.55 11 21 11 21z"
    )
    val DfPlayer:    ImageVector get() = Speaker
    val Link: ImageVector get() = svgIcon("Link",
        "M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z"
    )
    val LinkOff: ImageVector get() = svgIcon("LinkOff",
        "M17 7h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1 0 1.43-.98 2.63-2.31 2.98l1.46 1.46C20.6 15.55 22 13.93 22 12c0-2.76-2.24-5-5-5zm-1 4h-2.19l2 2H16v-2zM2 4.27l3.11 3.11A4.991 4.991 0 0 0 2 12c0 2.76 2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1 0-1.59 1.21-2.9 2.76-3.07L8.73 11H8v2h2.73L13 15.27V17h1.73l2.27 2.27L18.41 18 3.41 3 2 4.27z"
    )
    val Accessibility: ImageVector get() = svgIcon("Accessibility",
        "M12 2c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2zm9 7h-6v13h-2v-6h-2v6H9V9H3V7h18v2z"
    )
}
