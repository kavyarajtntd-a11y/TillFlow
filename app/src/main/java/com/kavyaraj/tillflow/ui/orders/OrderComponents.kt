package com.kavyaraj.tillflow.ui.orders

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kavyaraj.tillflow.R

/** Shows whether an order has reached the server yet. Every order is unsynced until step 4. */
@Composable
internal fun SyncLabel(isSynced: Boolean) {
    if (isSynced) {
        Text(stringResource(R.string.synced), color = MaterialTheme.colorScheme.secondary)
    } else {
        Text(stringResource(R.string.not_synced), color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
internal fun itemCountText(count: Int): String =
    LocalContext.current.resources.getQuantityString(R.plurals.item_count, count, count)

@Composable
internal fun pendingSyncText(count: Int): String =
    if (count == 0) {
        stringResource(R.string.all_synced)
    } else {
        LocalContext.current.resources.getQuantityString(R.plurals.pending_sync, count, count)
    }
