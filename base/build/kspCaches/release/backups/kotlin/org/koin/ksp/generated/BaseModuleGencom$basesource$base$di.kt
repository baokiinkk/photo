package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_basesource_base_di_BaseModule : Module get() = module {
	single() { _ -> com.basesource.base.network.AuthInterceptor(tokenProvider=get()) } bind(com.basesource.base.network.AuthInterceptor::class)
	single() { _ -> com.basesource.base.network.mock.MockInterceptor(context=get()) } bind(com.basesource.base.network.mock.MockInterceptor::class)
}
public val com.basesource.base.di.BaseModule.module : org.koin.core.module.Module get() = com_basesource_base_di_BaseModule
