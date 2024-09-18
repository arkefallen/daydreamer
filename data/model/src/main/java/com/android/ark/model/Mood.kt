package com.android.ark.model

import androidx.compose.ui.graphics.Color
import com.android.ark.ui.theme.AngryColor
import com.android.ark.ui.theme.AwfulColor
import com.android.ark.ui.theme.BoredColor
import com.android.ark.ui.theme.CalmColor
import com.android.ark.ui.theme.DepressedColor
import com.android.ark.ui.theme.DisappointedColor
import com.android.ark.ui.theme.HappyColor
import com.android.ark.ui.theme.HumorousColor
import com.android.ark.ui.theme.LonelyColor
import com.android.ark.ui.theme.MysteriousColor
import com.android.ark.ui.theme.NeutralColor
import com.android.ark.ui.theme.RomanticColor
import com.android.ark.ui.theme.ShamefulColor
import com.android.ark.ui.theme.SurprisedColor
import com.android.ark.ui.theme.SuspiciousColor
import com.android.ark.ui.theme.TenseColor

enum class Mood(
    val icon: Int,
    val containerColor: Color,
    val contentColor: Color
) {
    Neutral(
        icon = R.drawable.neutral,
        contentColor = Color.Black,
        containerColor = NeutralColor
    ),
    Happy(
        icon = R.drawable.happy,
        contentColor = Color.Black,
        containerColor = HappyColor
    ),
    Angry(
        icon = R.drawable.angry,
        contentColor = Color.White,
        containerColor = AngryColor
    ),
    Bored(
        icon = R.drawable.bored,
        contentColor = Color.Black,
        containerColor = BoredColor
    ),
    Calm(
        icon = R.drawable.calm,
        contentColor = Color.Black,
        containerColor = CalmColor
    ),
    Depressed(
        icon = R.drawable.depressed,
        contentColor = Color.Black,
        containerColor = DepressedColor
    ),
    Disappointed(
        icon = R.drawable.disappointed,
        contentColor = Color.White,
        containerColor = DisappointedColor
    ),
    Humorous(
        icon = R.drawable.humorous,
        contentColor = Color.Black,
        containerColor = HumorousColor
    ),
    Lonely(
        icon = R.drawable.lonely,
        contentColor = Color.White,
        containerColor = LonelyColor
    ),
    Mysterious(
        icon = R.drawable.mysterious,
        contentColor = Color.Black,
        containerColor = MysteriousColor
    ),
    Romantic(
        icon = R.drawable.romantic,
        contentColor = Color.White,
        containerColor = RomanticColor
    ),
    Shameful(
        icon = R.drawable.shameful,
        contentColor = Color.White,
        containerColor = ShamefulColor
    ),
    Awful(
        icon = R.drawable.awful,
        contentColor = Color.Black,
        containerColor = AwfulColor
    ),
    Surprised(
        icon = R.drawable.surprised,
        contentColor = Color.Black,
        containerColor = SurprisedColor
    ),
    Suspicious(
        icon = R.drawable.suspicious,
        contentColor = Color.Black,
        containerColor = SuspiciousColor
    ),
    Tense(
        icon = R.drawable.tense,
        contentColor = Color.Black,
        containerColor = TenseColor
    )
}