package com.android.ark.daydreamer.data.repository

import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.utils.Constants
import com.android.ark.daydreamer.utils.RequestState
import com.android.ark.daydreamer.utils.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.BsonObjectId
import java.time.ZoneId

object MongoDB : MongoRepository {
    private val app = App.create(Constants.APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureRealm()
    }

    override fun configureRealm() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { subscribe ->
                    add(
                        query = subscribe.query<Diary>("ownerId == $0", user.id),
                        name = "User's Diaries Subscription"
                    )
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        flow {
            emit(RequestState.Loading)
        }
        return if (user != null) {
            try {
                // Take all diaries from the realm database then sort based on date in descending order.
                // The diaries is grouped by its date.
                realm.query<Diary>(query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map {  result ->
                        RequestState.Success(
                            data = result.list.groupBy { diary ->
                                diary.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                flow { emit(RequestState.Error(e.message.toString())) }
            }
        } else {
            flow {
                emit(RequestState.Loading)
                emit(RequestState.Error(UserNotAuthenticatedMongoDBException().toString()))
            }
        }
    }

    override fun getSelectedDiary(diaryId: BsonObjectId): Flow<RequestState<Diary>> = flow {
        if (user != null) {
            try {
                val diary = realm.query<Diary>(query = "_id == $0", diaryId).find().first()
                emit(RequestState.Success(data = diary))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(RequestState.Error(e.message.toString()))
            }
        } else {
            emit(RequestState.Error(UserNotAuthenticatedMongoDBException().toString()))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun insertDiary(diary: Diary): Flow<RequestState<Diary>> {
        return if (user != null) {
            try {
                realm.write {
                    copyToRealm(diary.apply { ownerId = user.id })
                }
                flow {
                    emit(RequestState.Success(data = diary))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                flow {
                    emit(RequestState.Error(e.message.toString()))
                }
            }
        } else {
            flow {
                emit(RequestState.Error(UserNotAuthenticatedMongoDBException().toString()))
            }
        }
    }

    override suspend fun updateDiary(updatedDiary: Diary): Flow<RequestState<Diary>> {
        return if (user != null) {
            try {
                realm.write {
                    val queriedDiary = query<Diary>(query = "_id == $0", updatedDiary._id)
                        .find()
                        .first()
                    if (queriedDiary != null) {
                        queriedDiary.title = updatedDiary.title
                        queriedDiary.description = updatedDiary.description
                        queriedDiary.mood = updatedDiary.mood
                        queriedDiary.images = updatedDiary.images
                        queriedDiary.date = updatedDiary.date
                        flow {
                            emit(RequestState.Success(data = updatedDiary))
                        }
                    } else {
                        flow {
                            emit(RequestState.Error(message = "Queried diary does not exist."))
                        }
                    }
                }
            } catch (e: Exception) {
                flow {
                    emit(RequestState.Error(e.message.toString()))
                }
            }
        } else {
            flow {
                emit(RequestState.Error(UserNotAuthenticatedMongoDBException().toString()))
            }
        }
    }
}

private class UserNotAuthenticatedMongoDBException : Exception("User is not logged in")