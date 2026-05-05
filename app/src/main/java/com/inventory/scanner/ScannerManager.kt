package com.inventory.scanner

import android.view.KeyEvent
import kotlinx.coroutines.flow.SharedFlow

/**
 * Спільний інтерфейс для всіх реалізацій сканера штрих-коду.
 * Приховує специфіку конкретного виробника (Newland, Honeywell, generic).
 */
interface ScannerManager {
    /** Потік подій сканування. Колектори отримують ScanResult при кожному зчитуванні. */
    val scanEvents: SharedFlow<ScanResult>

    /** Реєструє ресивер/SDK сканера. Викликати в onStart/onResume Activity. */
    fun register()

    /** Скасовує реєстрацію ресивера/SDK. Викликати в onStop/onPause. */
    fun unregister()

    /** Повна зупинка — скасовує реєстрацію і закриває CoroutineScope. */
    fun destroy()

    /**
     * Обробляє апаратний KeyEvent кнопки сканування.
     * @return true якщо подія оброблена (не передавати далі)
     */
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean

    /** Програмно ініціює сканування. */
    fun triggerScan()
}
