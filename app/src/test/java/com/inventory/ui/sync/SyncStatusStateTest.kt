package com.inventory.ui.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncStatusStateTest {

    @Test
    fun `offline with pending operations shows OFFLINE_PENDING`() {
        val state = SyncStatusState(isOnline = false, pendingCount = 5)
        assertEquals(SyncDisplayMode.OFFLINE_PENDING, state.displayMode)
    }

    @Test
    fun `offline with zero pending shows OFFLINE`() {
        val state = SyncStatusState(isOnline = false, pendingCount = 0)
        assertEquals(SyncDisplayMode.OFFLINE, state.displayMode)
    }

    @Test
    fun `online and syncing shows SYNCING`() {
        val state = SyncStatusState(isOnline = true, isSyncing = true, pendingCount = 3)
        assertEquals(SyncDisplayMode.SYNCING, state.displayMode)
    }

    @Test
    fun `online with pending operations shows ONLINE_PENDING`() {
        val state = SyncStatusState(isOnline = true, pendingCount = 2)
        assertEquals(SyncDisplayMode.ONLINE_PENDING, state.displayMode)
    }

    @Test
    fun `online with zero pending shows SYNCED`() {
        val state = SyncStatusState(isOnline = true, pendingCount = 0)
        assertEquals(SyncDisplayMode.SYNCED, state.displayMode)
    }

    @Test
    fun `error takes priority over other states`() {
        val state = SyncStatusState(
            isOnline = true,
            isSyncing = false,
            pendingCount = 5,
            errorMessage = "Connection refused"
        )
        assertEquals(SyncDisplayMode.ERROR, state.displayMode)
    }

    @Test
    fun `error takes priority even when offline`() {
        val state = SyncStatusState(
            isOnline = false,
            pendingCount = 3,
            errorMessage = "Timeout"
        )
        assertEquals(SyncDisplayMode.ERROR, state.displayMode)
    }

    @Test
    fun `default state is offline with no pending`() {
        val state = SyncStatusState()
        assertEquals(SyncDisplayMode.OFFLINE, state.displayMode)
    }

    @Test
    fun `syncing takes priority over pending count`() {
        val state = SyncStatusState(isOnline = true, isSyncing = true, pendingCount = 10)
        assertEquals(SyncDisplayMode.SYNCING, state.displayMode)
    }

    @Test
    fun `last sync timestamp is preserved in synced state`() {
        val ts = 1711900000000L
        val state = SyncStatusState(isOnline = true, pendingCount = 0, lastSyncTimestamp = ts)
        assertEquals(SyncDisplayMode.SYNCED, state.displayMode)
        assertEquals(ts, state.lastSyncTimestamp)
    }
}
