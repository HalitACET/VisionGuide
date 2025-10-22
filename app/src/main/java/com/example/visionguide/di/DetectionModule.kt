package com.example.visionguide.di

import com.example.visionguide.data.DetectionRepository
import com.example.visionguide.data.DetectionRepositoryImpl
import com.example.visionguide.domain.AnalyzeImageUseCase
import com.example.visionguide.network.ApiClient
import com.example.visionguide.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DetectionModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiClient.api

    @Provides
    @Singleton
    fun provideRepository(api: ApiService): DetectionRepository = DetectionRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideAnalyzeImageUseCase(repo: DetectionRepository): AnalyzeImageUseCase = AnalyzeImageUseCase(repo)
}
