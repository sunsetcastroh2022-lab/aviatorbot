package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val phoneAngola: String,
    val planName: String,
    val priceKwanzas: Int,
    val expiresAtMillis: Long,
    val isActive: Boolean = true,
    val allowedBookmakers: String = "BANTUBET,ELEPHANTBET,KWANZABET",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastLoginAtMillis: Long? = null,
    val loginCount: Int = 0,
    val lastActiveBookmaker: String = "BANTUBET"
)

@Entity(tableName = "subscriber_activity")
data class SubscriberActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val fullName: String,
    val actionType: String, // LOGIN, BOOKMAKER_SWITCH, VELA_SIGNAL, ACCOUNT_CREATED, LICENSE_RENEWED, STATUS_CHANGED
    val bookmakerId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vela_history")
data class VelaHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookmakerId: String,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AviatorDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAtMillis DESC")
    fun observeAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun findUserByUsername(username: String): UserAccountEntity?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Query("DELETE FROM user_accounts WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)

    @Query("SELECT * FROM subscriber_activity ORDER BY timestamp DESC LIMIT 60")
    fun observeSubscriberActivities(): Flow<List<SubscriberActivityEntity>>

    @Query("SELECT COUNT(*) FROM subscriber_activity")
    suspend fun getActivityCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: SubscriberActivityEntity)

    @Query("DELETE FROM subscriber_activity")
    suspend fun clearAllActivities()

    @Query("SELECT * FROM vela_history WHERE bookmakerId = :bookmakerId ORDER BY timestamp DESC LIMIT 30")
    fun observeVelasForBookmaker(bookmakerId: String): Flow<List<VelaHistoryEntity>>

    @Query("SELECT COUNT(*) FROM vela_history WHERE bookmakerId = :bookmakerId")
    suspend fun getVelaCountForBookmaker(bookmakerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVela(vela: VelaHistoryEntity)

    @Query("DELETE FROM vela_history WHERE id = (SELECT id FROM vela_history WHERE bookmakerId = :bookmakerId ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLatestVelaForBookmaker(bookmakerId: String)

    @Query("DELETE FROM vela_history WHERE bookmakerId = :bookmakerId")
    suspend fun clearVelasForBookmaker(bookmakerId: String)
}

@Database(
    entities = [
        UserAccountEntity::class,
        SubscriberActivityEntity::class,
        VelaHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AviatorDatabase : RoomDatabase() {
    abstract fun aviatorDao(): AviatorDao

    companion object {
        @Volatile
        private var INSTANCE: AviatorDatabase? = null

        fun getInstance(context: Context): AviatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AviatorDatabase::class.java,
                    "aviator_ao_bot.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
