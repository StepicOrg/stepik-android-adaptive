package org.stepik.android.adaptive.di.network

import dagger.Module

@Module(includes = [AuthModule::class, RemoteStorageModule::class])
class NetworkModule