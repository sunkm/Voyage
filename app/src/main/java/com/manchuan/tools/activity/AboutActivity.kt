package com.manchuan.tools.activity

import android.annotation.SuppressLint
import android.os.SystemClock
import android.widget.ImageView
import android.widget.TextView
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.R
import com.manchuan.tools.about.AbsAboutActivity
import com.manchuan.tools.about.Card
import com.manchuan.tools.about.Category
import com.manchuan.tools.about.Contributor
import com.manchuan.tools.about.License
import com.manchuan.tools.eggs.t.PlatLogoActivity


class AboutActivity : AbsAboutActivity() {

    private val counts = 7
    private fun continuousClick() {
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        //为数组最后一位赋值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] >= SystemClock.uptimeMillis() - DURATION) {
            mHits = LongArray(counts) //重新初始化数组
            startActivity<PlatLogoActivity>()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        icon.setImageResource(R.mipmap.ic_launcher)
        icon.doOnClick {
            continuousClick()
        }
        slogan.text = "远航工具箱"
        version.text = "v${BuildConfig.VERSION_NAME}"
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.add(Category("介绍与帮助"))
        items.add(Card("本应用是作者利用闲暇时间制作的，如有任何问题请联系作者反馈"))
        items.add(Category("开发组"))
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=3299699002&s=640",
                "航",
                "开发 & 设计",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=3299699002"
            )
        )
        items.add(
            Contributor(
                "https://q2.qlogo.cn/headimg_dl?dst_uin=2830430757&spec=100",
                "月下鱼猫",
                "宣传图制作",
                "https://www.coolapk.com/u/1362030"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=2899738115&s=640",
                "春秋",
                "接口提供",
                "https://ahsp.app/"
            )
        )
        items.add(Category("开源许可"))
        items.add(
            License(
                "MultiType",
                "drakeet",
                License.APACHE_2,
                "https://github.com/drakeet/MultiType"
            )
        )
        items.add(
            License(
                "about-page",
                "drakeet",
                License.APACHE_2,
                "https://github.com/drakeet/about-page"
            )
        )
        items.add(
            License(
                "DialogX",
                "Kongzue",
                License.APACHE_2,
                "https://github.com/kongzue/DialogX"
            )
        )
        items.add(
            License(
                "BRV",
                "liangjingkanji",
                License.APACHE_2,
                "https://github.com/liangjingkanji/BRV"
            )
        )
        items.add(
            License(
                "Net",
                "liangjingkanji",
                License.APACHE_2,
                "https://github.com/liangjingkanji/Net"
            )
        )
    }
}