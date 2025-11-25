package com.yashvant.swipe_assignment.di

import androidx.room.Room
import com.yashvant.swipe_assignment.data.local.AppDatabase
import com.yashvant.swipe_assignment.data.remote.ApiService
import com.yashvant.swipe_assignment.data.repository.ProductRepository
import com.yashvant.swipe_assignment.ui.viewmodel.ProductViewModel
import com.yashvant.swipe_assignment.utils.NetworkConnectivityObserver
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single { "https://app.getswipe.in/api/" }

    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(get<String>())
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(ApiService::class.java)
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        get<AppDatabase>().productDao()
    }


    single {
        NetworkConnectivityObserver(androidContext())
    }

    single {
        ProductRepository(
            apiService = get(),
            productDao = get(),
            context = androidContext()
        )
    }

    viewModel {
        ProductViewModel(
            repository = get(),
            networkObserver = get()
        )
    }
}
