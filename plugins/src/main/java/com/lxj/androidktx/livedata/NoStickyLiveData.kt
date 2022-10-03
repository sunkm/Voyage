package com.lxj.androidktx.livedata

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by cxzheng on 2018/8/27.
 *
 *
 * Customize a LiveData that controls whether you need sticky.Default is not sticky.
 *
 *
 * LiveData's own sticky feature makes it easy to cause many problems when LiveData is static global, which is annoying.
 */
open class NoStickyLiveData<T : Any> {
    private val handler = Handler(Looper.getMainLooper())
    private var mObservers: MutableMap<Observer<T>, ObserverWrapper> =
        ConcurrentHashMap<Observer<T>, ObserverWrapper>()

    @Volatile
    private var mData = NOT_SET
    private var mVersion = START_VERSION
    private var mDispatchingValue = false
    private var mDispatchInvalidated = false
    @MainThread
    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        this.observe(owner, observer, false)
    }

    @MainThread
    fun observeForever(observer: Observer<T>) {
        this.observeForever(observer, false)
    }

    @MainThread
    fun observe(owner: LifecycleOwner, observer: Observer<T>, isSticky: Boolean) {
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val wrapper: LifecycleBoundObserver = LifecycleBoundObserver(owner, observer, isSticky)
        var existing: ObserverWrapper? = mObservers[observer]
        if (existing == null) {
            existing = mObservers.put(observer, wrapper)
        }
        if (existing != null && !existing.isAttachedTo(owner)) {
//            throw new IllegalArgumentException("Cannot add the same observer"
//                    + " with different lifecycles");
            //出现于Activity重启，Observer{}对象是同一个，但是绑定了2个生命周期
            return
        } else {
            owner.lifecycle.addObserver(wrapper)
        }
    }

    @MainThread
    fun observeForever(observer: Observer<T>, isSticky: Boolean) {
        val wrapper: AlwaysActiveObserver = AlwaysActiveObserver(observer, isSticky)
        var existing: ObserverWrapper? = mObservers[observer]
        if (existing == null) {
            existing = mObservers.put(observer, wrapper)
        }
        if (existing != null && existing is LifecycleBoundObserver) {
//            throw new IllegalArgumentException("Cannot add the same observer"
//                    + " with different lifecycles");
            return
        }
        if (existing != null) {
            return
        }
        wrapper.activeStateChanged(true)
    }

    @JvmName("setValue1")
    @MainThread
    fun setValue(value: T) {
        assertMainThread("setValue")
        mVersion++
        mData = value
        dispatchingValue(null)
    }

    fun postValue(value: Any?) {
        mVersion++
        if (value != null) {
            mData = value
        }
        handler.post { dispatchingValue(null) }
    }

    var value: T?
        get() {
            val data = mData
            return if (data !== NOT_SET) {
                data as T
            } else null
        }
        set(value) {}

    internal inner class LifecycleBoundObserver(
        val mOwner: LifecycleOwner,
        observer: Observer<T>?,
        isSticky: Boolean
    ) : ObserverWrapper(observer, isSticky), LifecycleEventObserver {
        override fun shouldBeActive(): Boolean {
            return mOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (mOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                if (mObserver != null) {
                    removeObserver(mObserver)
                }
                return
            }
            activeStateChanged(shouldBeActive())
        }

        override fun isAttachedTo(owner: LifecycleOwner): Boolean {
            return mOwner === owner
        }

        override fun detachObserver() {
            mOwner.lifecycle.removeObserver(this)
        }
    }

    private inner class AlwaysActiveObserver internal constructor(
        observer: Observer<T>?,
        isSticky: Boolean
    ) : ObserverWrapper(observer, isSticky) {
        override fun shouldBeActive(): Boolean {
            return true
        }
    }

    abstract inner class ObserverWrapper internal constructor(
        val mObserver: Observer<T>?,
        mIsSticky: Boolean
    ) {
        var mActive = false
        var mLastVersion: Int

        init {
            mLastVersion = if (mIsSticky) START_VERSION else mVersion
        }

        abstract fun shouldBeActive(): Boolean
        open fun isAttachedTo(owner: LifecycleOwner): Boolean {
            return false
        }

        open fun detachObserver() {}
        fun activeStateChanged(newActive: Boolean) {
            if (newActive == mActive) {
                return
            }
            mActive = newActive
            if (mActive) {
                dispatchingValue(this)
            }
        }
    }

    private fun dispatchingValue(initiator: ObserverWrapper?) {
        var initiator: ObserverWrapper? = initiator
        if (mDispatchingValue) {
            mDispatchInvalidated = true
            return
        }
        mDispatchingValue = true
        do {
            mDispatchInvalidated = false
            if (initiator != null) {
                considerNotify(initiator)
                initiator = null
            } else {
                for ((_, value1) in mObservers) {
                    considerNotify(value1)
                    if (mDispatchInvalidated) {
                        break
                    }
                }
            }
        } while (mDispatchInvalidated)
        mDispatchingValue = false
    }

    private fun considerNotify(observer: ObserverWrapper) {
        if (!observer.mActive) {
            return
        }
        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false)
            return
        }
        if (observer.mLastVersion >= mVersion) {
            return
        }
        observer.mLastVersion = mVersion
        observer.mObserver?.onChanged(mData as T)
    }

    @MainThread
    fun removeObserver(observer: Observer<T>) {
        assertMainThread("removeObserver")
        val removed: ObserverWrapper = mObservers.remove(observer)
            ?: return
        removed.detachObserver()
        removed.activeStateChanged(false)
    }

    @MainThread
    fun removeObservers(owner: LifecycleOwner) {
        assertMainThread("removeObservers")
        for ((key, value1) in mObservers) {
            if (value1.isAttachedTo(owner)) {
                removeObserver(key)
            }
        }
    }

    companion object {
        private val TAG = NoStickyLiveData::class.java.simpleName
        const val START_VERSION = -1
        private val NOT_SET = Any()
        private fun assertMainThread(methodName: String) {
            check(Looper.myLooper() == Looper.getMainLooper()) {
                ("Cannot invoke " + methodName + " on a background"
                        + " thread")
            }
        }
    }
}