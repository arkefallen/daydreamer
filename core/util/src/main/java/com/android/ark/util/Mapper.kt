package com.android.ark.util

import io.realm.kotlin.types.RealmInstant
import java.time.Instant

fun RealmInstant.toInstant() : Instant {
    val sec: Long = epochSeconds
    val nano: Int = nanosecondsOfSecond
    return if (sec >= 0) {
        Instant.ofEpochSecond(sec, nano.toLong())
    } else {
        Instant.ofEpochSecond(sec - 1, nano.toLong() + 1_000_000_000)
    }
}

fun Instant.toRealmInstant() : RealmInstant {
    val sec: Long = this.epochSecond
    val nano: Int = this.nano
    return if (sec >= 0) {
        RealmInstant.from(sec, nano)
    } else {
        RealmInstant.from(sec + 1, -1_000_000_000 + nano)
    }
}