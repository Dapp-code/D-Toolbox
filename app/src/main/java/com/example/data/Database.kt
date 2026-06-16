package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val email: String,
    val passwordHash: String,
    val securityQuestion: String,
    val securityAnswerHash: String,
    val isLoggedIn: Boolean = false,
    val gridLayoutPreference: Boolean = true,
    val isAdmin: Boolean = false
)

@Entity(tableName = "tool_submissions")
data class ToolSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val toolName: String,
    val category: String,
    val description: String,
    val codeSnippet: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // "Pending", "Approved", "Rejected"
)

@Entity(tableName = "favorites", primaryKeys = ["username", "toolId"])
data class FavoriteToolEntity(
    val username: String,
    val toolId: String,
    val name: String,
    val category: String,
    val isFavorite: Boolean = true
)

@Entity(tableName = "recent_activities")
data class RecentActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val toolId: String,
    val toolName: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionDetail: String = ""
)

@Entity(tableName = "input_autosaves", primaryKeys = ["username", "toolId"])
data class InputAutosaveEntity(
    val username: String,
    val toolId: String,
    val inputValue: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ToolboxDao {
    // Users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    // Submissions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(sub: ToolSubmissionEntity)

    @Query("SELECT * FROM tool_submissions ORDER BY timestamp DESC")
    fun getAllSubmissionsFlow(): Flow<List<ToolSubmissionEntity>>

    @Query("DELETE FROM tool_submissions WHERE id = :subId")
    suspend fun deleteSubmission(subId: Int)

    @Query("UPDATE tool_submissions SET status = :status WHERE id = :subId")
    suspend fun updateSubmissionStatus(subId: Int, status: String)

    // Favorites
    @Query("SELECT * FROM favorites WHERE username = :username AND isFavorite = 1")
    fun getFavoritesFlow(username: String): Flow<List<FavoriteToolEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE username = :username AND toolId = :toolId AND isFavorite = 1)")
    suspend fun isFavorite(username: String, toolId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteToolEntity)

    @Delete
    suspend fun deleteFavorite(entity: FavoriteToolEntity)

    // Recent Activities
    @Query("SELECT * FROM recent_activities WHERE username = :username ORDER BY timestamp DESC LIMIT 50")
    fun getRecentActivitiesFlow(username: String): Flow<List<RecentActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(entity: RecentActivityEntity)

    @Query("DELETE FROM recent_activities WHERE username = :username")
    suspend fun clearAllActivity(username: String)

    // Autosaves
    @Query("SELECT * FROM input_autosaves WHERE username = :username AND toolId = :toolId")
    suspend fun getAutosave(username: String, toolId: String): InputAutosaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInput(entity: InputAutosaveEntity)

    @Query("DELETE FROM input_autosaves WHERE username = :username AND toolId = :toolId")
    suspend fun clearAutosave(username: String, toolId: String)
}

@Database(
    entities = [
        UserEntity::class,
        ToolSubmissionEntity::class,
        FavoriteToolEntity::class,
        RecentActivityEntity::class,
        InputAutosaveEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ToolboxDatabase : RoomDatabase() {
    abstract fun toolboxDao(): ToolboxDao

    companion object {
        @Volatile
        private var INSTANCE: ToolboxDatabase? = null

        fun getDatabase(context: Context): ToolboxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToolboxDatabase::class.java,
                    "d_toolbox_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ToolboxRepository(private val dao: ToolboxDao) {
    // Users
    suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
    suspend fun getUser(username: String): UserEntity? = dao.getUser(username)
    suspend fun getActiveUser(): UserEntity? = dao.getActiveUser()
    suspend fun logoutAllUsers() = dao.logoutAllUsers()

    // Submissions
    val allSubmissions: Flow<List<ToolSubmissionEntity>> = dao.getAllSubmissionsFlow()
    suspend fun addSubmission(sub: ToolSubmissionEntity) = dao.insertSubmission(sub)
    suspend fun deleteSubmission(subId: Int) = dao.deleteSubmission(subId)
    suspend fun updateSubmissionStatus(subId: Int, status: String) = dao.updateSubmissionStatus(subId, status)

    // Favorites
    fun getFavorites(username: String): Flow<List<FavoriteToolEntity>> = dao.getFavoritesFlow(username)
    suspend fun isFavorite(username: String, toolId: String): Boolean = dao.isFavorite(username, toolId)
    suspend fun toggleFavorite(username: String, toolId: String, name: String, category: String) {
        if (dao.isFavorite(username, toolId)) {
            dao.deleteFavorite(FavoriteToolEntity(username, toolId, name, category))
        } else {
            dao.insertFavorite(FavoriteToolEntity(username, toolId, name, category, true))
        }
    }

    // Recent Activity
    fun getRecentActivities(username: String): Flow<List<RecentActivityEntity>> = dao.getRecentActivitiesFlow(username)
    suspend fun addActivity(username: String, toolId: String, name: String, category: String, actionDetail: String) {
        dao.insertActivity(RecentActivityEntity(username = username, toolId = toolId, toolName = name, category = category, actionDetail = actionDetail))
    }
    suspend fun clearActivity(username: String) {
        dao.clearAllActivity(username)
    }

    // Input Autosaves
    suspend fun getAutosave(username: String, toolId: String): String? {
        return dao.getAutosave(username, toolId)?.inputValue
    }
    suspend fun saveInput(username: String, toolId: String, value: String) {
        dao.saveInput(InputAutosaveEntity(username, toolId, value))
    }
    suspend fun clearAutosave(username: String, toolId: String) {
        dao.clearAutosave(username, toolId)
    }
}
