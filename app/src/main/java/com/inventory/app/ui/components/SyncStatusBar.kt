package com.inventory.app.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.inventory.app.R
import com.inventory.app.sync.SyncState

/**
 * Custom view component displaying persistent sync status indicator.
 *
 * Provides three visual states:
 * - **Offline**: Amber bar showing "Offline — N changes pending sync"
 * - **Syncing**: Blue bar with progress animation showing "Syncing..."
 * - **Online**: Collapsed to small green dot (8dp)
 *
 * Updates reactively via [updateState] method when sync status changes.
 *
 * Usage in XML:
 * ```xml
 * <com.inventory.app.ui.components.SyncStatusBar
 *     android:id="@+id/syncStatusBar"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 *
 * Usage in code:
 * ```kotlin
 * syncStatusBar.updateState(SyncState.Offline(pendingCount = 5))
 * syncStatusBar.updateState(SyncState.Syncing(progress = 50))
 * syncStatusBar.updateState(SyncState.Online)
 * ```
 */
class SyncStatusBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // UI components from layout
    private val expandedContainer: LinearLayout
    private val statusText: TextView
    private val progressBar: ProgressBar
    private val collapsedDot: View

    init {
        // Inflate the layout
        LayoutInflater.from(context).inflate(
            R.layout.component_sync_status_bar,
            this,
            true
        )

        // Get references to UI components
        expandedContainer = findViewById(R.id.expandedContainer)
        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)
        collapsedDot = findViewById(R.id.collapsedDot)
    }

    /**
     * Updates the visual state of the sync status bar.
     *
     * Handles transitions between three states with appropriate colors,
     * text, and visibility changes.
     *
     * @param state The new [SyncState] to display
     */
    fun updateState(state: SyncState) {
        when (state) {
            is SyncState.Offline -> showOfflineState(state.pendingCount)
            is SyncState.Syncing -> showSyncingState(state.progress)
            is SyncState.Online -> showOnlineState()
        }
    }

    /**
     * Shows offline state: amber bar with pending operations count.
     *
     * @param pendingCount Number of operations waiting to sync
     */
    private fun showOfflineState(pendingCount: Int) {
        // Show expanded container, hide collapsed dot
        expandedContainer.visibility = View.VISIBLE
        collapsedDot.visibility = View.GONE

        // Set amber background color
        expandedContainer.setBackgroundColor(
            ContextCompat.getColor(context, R.color.sync_offline_amber)
        )

        // Update text with pending count
        val countText = if (pendingCount == 1) {
            "Offline — 1 change pending sync"
        } else {
            "Offline — $pendingCount changes pending sync"
        }
        statusText.text = countText

        // Hide progress bar (not syncing)
        progressBar.visibility = View.GONE
    }

    /**
     * Shows syncing state: blue bar with progress animation.
     *
     * @param progress Sync progress percentage (0-100), or null for indeterminate
     */
    private fun showSyncingState(progress: Int?) {
        // Show expanded container, hide collapsed dot
        expandedContainer.visibility = View.VISIBLE
        collapsedDot.visibility = View.GONE

        // Set blue background color
        expandedContainer.setBackgroundColor(
            ContextCompat.getColor(context, R.color.sync_syncing_blue)
        )

        // Update text for syncing state
        statusText.text = if (progress != null) {
            "Syncing... $progress%"
        } else {
            "Syncing..."
        }

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Set progress bar mode based on progress value
        if (progress != null) {
            progressBar.isIndeterminate = false
            progressBar.progress = progress
        } else {
            progressBar.isIndeterminate = true
        }
    }

    /**
     * Shows online state: collapsed to small green dot.
     *
     * Indicates all operations are synced and device is online.
     */
    private fun showOnlineState() {
        // Hide expanded container, show collapsed dot
        expandedContainer.visibility = View.GONE
        collapsedDot.visibility = View.VISIBLE
    }
}
