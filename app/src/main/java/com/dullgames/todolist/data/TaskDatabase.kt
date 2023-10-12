package com.dullgames.todolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dullgames.todolist.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun taskDao():TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ): RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()
            applicationScope.launch {
                dao.insert(Task("Apple"))
                dao.insert(Task("Banana"))
                dao.insert(Task("Orange", important = true))
                dao.insert(Task("Melon"))
                dao.insert(Task("Papaya"))
                dao.insert(Task("Lemon", completed = true))
                dao.insert(Task("Tomato"))
                dao.insert(Task("Papaya"))
                dao.insert(Task("Lemon", completed = true))
                dao.insert(Task("Tomato"))
            }
        }
    }

}