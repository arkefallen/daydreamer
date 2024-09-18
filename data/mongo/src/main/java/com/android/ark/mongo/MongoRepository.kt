package com.android.ark.mongo

import com.android.ark.model.Diary
import com.android.ark.util.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureRealm()
    fun getAllDiaries() : Flow<Diaries>
    fun getSelectedDiary(diaryId: ObjectId) : Flow<RequestState<Diary>>
    suspend fun insertDiary(diary: Diary) : Flow<RequestState<Diary>>
    suspend fun updateDiary(updatedDiary: Diary) : Flow<RequestState<Diary>>
    suspend fun deleteDiary(diaryId: ObjectId) : Flow<RequestState<Boolean>>
    suspend fun deleteAllDiaries() : Flow<RequestState<Boolean>>
}