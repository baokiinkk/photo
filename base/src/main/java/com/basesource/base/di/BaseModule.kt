package com.basesource.base.di

import android.content.Context
import com.basesource.base.network.NetworkUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module


@Module
@ComponentScan("com.basesource.base")
class BaseModule
