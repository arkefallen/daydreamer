package com.android.ark.daydreamer.data.repository

import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.utils.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureRealm()
    fun getAllDiaries() : Flow<Diaries>
}