package com.example.blutoothapp.DI


import com.example.blutoothapp.Data.Repositiry.BluetoothRepositoryImpl
import com.example.blutoothapp.Domain.Repository.BluetoothRepository
import com.example.blutoothapp.Domain.useCase.BluetoothUseCases
import com.example.blutoothapp.Domain.useCase.GetPairedDevicesUseCase
import com.example.blutoothapp.Domain.useCase.GetScannedDevicesUseCase
import com.example.blutoothapp.Domain.useCase.ReleaseBluetoothResourcesUseCase
import com.example.blutoothapp.Domain.useCase.StartDeviceDiscoveryUseCase
import com.example.blutoothapp.Domain.useCase.StopDeviceDiscoveryUseCase
import com.example.blutoothapp.Domain.useCase.connectToDeviceUseCase
import com.example.blutoothapp.Presentation.BluetoothViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {

    fun getModule(app: App): Module {
        return module {



            single<BluetoothRepository> {BluetoothRepositoryImpl(androidContext()) }



            single {
                BluetoothUseCases(
                    getPairedDevicesUseCase = GetPairedDevicesUseCase(get()),
                    getScannedDevicesUseCase = GetScannedDevicesUseCase(get()),
                    startDeviceDiscoveryUseCase = StartDeviceDiscoveryUseCase(get()),
                    StopDeviceDiscoveryUseCase = StopDeviceDiscoveryUseCase(get()),
                    releaseBluetoothResourcesUseCase = ReleaseBluetoothResourcesUseCase(get()),
                    connectToDeviceUseCase = connectToDeviceUseCase(get())
                )
            }


            viewModel { BluetoothViewModel(get()) }



        }

    }

}