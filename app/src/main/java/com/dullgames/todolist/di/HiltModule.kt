package com.dullgames.todolist.di

import android.app.Application
import androidx.room.Room
import com.dullgames.todolist.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {

    @Provides
    @Singleton
    fun providesDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ) = Room.databaseBuilder(app,TaskDatabase::class.java, "task_Database")
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    fun providesDao(db: TaskDatabase) = db.taskDao()

    @ApplicationScope
    @Singleton
    @Provides
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope