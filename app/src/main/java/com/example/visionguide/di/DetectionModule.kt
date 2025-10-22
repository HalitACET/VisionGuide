package com.example.visionguide.di

import com.example.visionguide.domain.repository.DetectionRepository
import com.example.visionguide.data.repository.DetectionRepositoryImpl
import com.example.visionguide.domain.usecase.AnalyzeImageUseCase
import com.example.visionguide.data.network.ApiClient
import com.example.visionguide.data.network.ApiService
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
