package com.example.visionguide.data.di

import com.example.visionguide.data.repository.InMemoryPostRepository
import com.example.visionguide.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: InMemoryPostRepository): PostRepository
}
