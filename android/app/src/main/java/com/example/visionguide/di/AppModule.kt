package com.example.visionguide.di

import com.example.visionguide.data.repository.AuthRepositoryImpl
import com.example.visionguide.data.repository.InMemoryPostRepository
import com.example.visionguide.domain.repository.AuthRepository
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

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(impl: com.example.visionguide.data.repository.CommunityRepositoryImpl): com.example.visionguide.domain.repository.CommunityRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
