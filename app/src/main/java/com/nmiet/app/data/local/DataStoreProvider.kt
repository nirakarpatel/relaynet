package com.nmiet.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.relaynetDataStore: DataStore<Preferences> by preferencesDataStore(name = "relaynet_preferences")
