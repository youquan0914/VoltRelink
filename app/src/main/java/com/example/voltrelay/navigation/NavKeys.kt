package com.example.voltrelay.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey

@Serializable
data object DashboardKey : Destination

@Serializable
data object RelayManagementKey : Destination

@Serializable
data object SettingsKey : Destination
