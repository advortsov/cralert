package com.cralert.app

import android.content.SharedPreferences
import com.cralert.app.data.DiagnosticsState
import com.cralert.app.data.local.DiagnosticsRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticsRepositoryTest {

    @Test
    fun savesAndLoadsDiagnosticsState() {
        val prefs = FakeSharedPreferences()
        val repository = DiagnosticsRepository(prefs)
        val state = DiagnosticsState(
            lastAlarmScheduledAt = 10L,
            nextAlarmAt = 20L,
            lastReceiverFiredAt = 30L,
            lastServiceStartedAt = 40L,
            lastServiceFinishedAt = 50L,
            lastFetchStartedAt = 60L,
            lastFetchSuccess = true,
            lastFetchHttpCode = 200,
            lastFetchError = "ok",
            lastAssetsCount = 5,
            lastPricesCount = 7,
            lastTriggeredAlerts = 2,
            lastSkippedNoPrice = 1,
            lastSkippedDisabled = 3,
            lastNotificationSentAt = 70L
        )

        repository.setState(state)
        val loaded = repository.getState()

        assertEquals(state, loaded)
    }

    private class FakeSharedPreferences : SharedPreferences {
        private val data = mutableMapOf<String, Any?>()
        private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

        override fun getAll(): MutableMap<String, *> = data.toMutableMap()

        override fun getString(key: String, defValue: String?): String? {
            return data[key] as? String ?: defValue
        }

        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
            @Suppress("UNCHECKED_CAST")
            return data[key] as? MutableSet<String> ?: defValues
        }

        override fun getInt(key: String, defValue: Int): Int {
            return data[key] as? Int ?: defValue
        }

        override fun getLong(key: String, defValue: Long): Long {
            return data[key] as? Long ?: defValue
        }

        override fun getFloat(key: String, defValue: Float): Float {
            return data[key] as? Float ?: defValue
        }

        override fun getBoolean(key: String, defValue: Boolean): Boolean {
            return data[key] as? Boolean ?: defValue
        }

        override fun contains(key: String): Boolean = data.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor()

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            listeners.add(listener)
        }

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            listeners.remove(listener)
        }

        private inner class Editor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private var clear = false

            override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = apply {
                pending[key] = values
            }

            override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun remove(key: String): SharedPreferences.Editor = apply {
                pending[key] = null
            }

            override fun clear(): SharedPreferences.Editor = apply {
                clear = true
            }

            override fun commit(): Boolean {
                applyChanges()
                return true
            }

            override fun apply() {
                applyChanges()
            }

            private fun applyChanges() {
                if (clear) {
                    data.clear()
                }
                pending.forEach { (key, value) ->
                    if (value == null) {
                        data.remove(key)
                    } else {
                        data[key] = value
                    }
                }
                pending.clear()
            }
        }
    }
}
