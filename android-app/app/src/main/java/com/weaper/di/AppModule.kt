package com.weaper.di

import com.google.firebase.firestore.FirebaseFirestore
import com.weaper.data.firebase.FirebasePlaylistRepository
import com.weaper.data.firebase.FirebaseReaperTrackRepository
import com.weaper.data.firebase.FirebaseSetlistRepository
import com.weaper.data.firebase.FirebaseSoundboardRepository
import com.weaper.data.local.LocalSyncRepository
import com.weaper.domain.repository.PlaylistRepository
import com.weaper.domain.repository.ReaperTrackRepository
import com.weaper.domain.repository.SetlistRepository
import com.weaper.domain.repository.SoundboardRepository
import com.weaper.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSetlistRepository(impl: FirebaseSetlistRepository): SetlistRepository

    @Binds
    @Singleton
    abstract fun bindSoundboardRepository(impl: FirebaseSoundboardRepository): SoundboardRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: LocalSyncRepository): SyncRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: FirebasePlaylistRepository): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindReaperTrackRepository(impl: FirebaseReaperTrackRepository): ReaperTrackRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
