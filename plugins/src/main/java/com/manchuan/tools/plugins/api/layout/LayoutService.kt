package com.manchuan.tools.plugins.api.layout
import androidx.lifecycle.Lifecycle
import com.manchuan.tools.plugins.api.Layout

interface LayoutService {

        fun createLayoutArgument(): LayoutArgument

    fun <T : Layout<*>> registerLayout(actionClass: Class<T>, key: LayoutKey)

    /**
     * Register an action,when lifecycle destroy,will automatic unregister the action
     */
    fun <T : Layout<*>> registerLayout(actionClass: Class<T>, lifecycle: Lifecycle, key: LayoutKey)

    /**
     * Unregister an action
     */
    fun unregisterLayout(actionClass: Class<Layout<*>>, key: LayoutKey)

    fun clearLayout(key: LayoutKey)

    fun <T> callLayout(actionArgument: LayoutArgument, key: LayoutKey): T?

    fun forwardLayoutArgument(actionArgument: LayoutArgument, key: LayoutKey): LayoutArgument

    /**
     * Unregister one or more forwarding event
     */
    fun unregisterForwardArgument(vararg key: LayoutKey, block: (LayoutArgument) -> LayoutArgument)

    /**
     *  Register forward argument event,when lifecycle destroy,will automatic unregister the forward argument
     */
    fun registerForwardArgument(
        vararg key: LayoutKey,
        lifecycle: Lifecycle,
        block: (LayoutArgument) -> LayoutArgument
    )


    fun registerForwardArgument(key: LayoutKey, block: (LayoutArgument) -> LayoutArgument)

    fun registerForwardArgument(vararg key: LayoutKey, block: (LayoutArgument) -> LayoutArgument)

    /**
     * Unregister a forwarding event
     */
    fun unregisterForwardArgument(key: LayoutKey, block: (LayoutArgument) -> LayoutArgument)

    /**
     *  Register forward argument event,when lifecycle destroy,will automatic unregister the forward argument
     */
    fun registerForwardArgument(
        key: LayoutKey,
        lifecycle: Lifecycle,
        block: (LayoutArgument) -> LayoutArgument
    )

}