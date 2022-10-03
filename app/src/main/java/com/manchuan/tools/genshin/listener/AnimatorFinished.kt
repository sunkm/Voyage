package com.manchuan.tools.genshin.listener
import android.animation.Animator

//animation监听器
class AnimatorFinished(val block: () -> Unit): Animator.AnimatorListener{

    override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
    }

    override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
        block()
    }

    override fun onAnimationEnd(animation: Animator) {
        block()
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationRepeat(animation: Animator) {
    }

    override fun onAnimationStart(animation: Animator) {

    }
}