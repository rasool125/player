package com.example.blutoothapp.Presentation

// Extensions.kt
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.exception != null) {
                cont.resumeWithException(task.exception!!)
            } else {
                cont.resume(task.result, null)
            }
        }
    }
}
