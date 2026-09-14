package com.example.blockblast.model

enum class AssistantMode(
    val title: String,
    val description: String,
    val prefKey: String
) {
    LOT(
        title = "Lot",
        description = "Veľká pomoc – aktívne vyberá dieliky na dokončenie línií a kombá.",
        prefKey = "HIGH_SCORE_LOT"
    ),
    MEDIUM(
        title = "Medium",
        description = "Stredná pomoc – častejšie ponúkne vhodný kúsok na vyčistenie dosky.",
        prefKey = "HIGH_SCORE_MEDIUM"
    ),
    LOW(
        title = "Low",
        description = "Mierna pomoc – občas jemne pomôže potrebným tvarom.",
        prefKey = "HIGH_SCORE_LOW"
    ),
    OFF(
        title = "Off",
        description = "Vypnutý – čisté náhodné generovanie dielikov bez asistencie.",
        prefKey = "HIGH_SCORE_OFF"
    );

    companion object {
        val ALL_MODES_ORDERED: List<AssistantMode> = listOf(LOT, MEDIUM, LOW, OFF)

        fun fromString(value: String?): AssistantMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OFF
        }
    }
}
