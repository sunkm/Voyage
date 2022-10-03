package com.manchuan.tools.bean

import com.manchuan.tools.bean.DataBean
import java.util.*

class DataBean {
    var imageRes: Int? = null
    var imageUrl: String? = null
    var title: String?
    var viewType: Int

    constructor(imageRes: Int?, title: String?, viewType: Int) {
        this.imageRes = imageRes
        this.title = title
        this.viewType = viewType
    }

    constructor(imageUrl: String?, title: String?, viewType: Int) {
        this.imageUrl = imageUrl
        this.title = title
        this.viewType = viewType
    }

    companion object {
        val testData3: List<*>
            get() {
                val list: MutableList<DataBean> = ArrayList()
                for (i in 0..9) {
                    list.add(DataBean("http://101.43.82.164:9987/ads.png", null, 1))
                }
                return list
            }

        fun getColors(size: Int): List<String> {
            val list: MutableList<String> = ArrayList()
            for (i in 0 until size) {
                list.add(randColor)
            }
            return list
        }

        /**
         * 获取十六进制的颜色代码.例如  "#5A6677"
         * 分别取R、G、B的随机值，然后加起来即可
         *
         * @return String
         */
        private val randColor: String
            get() {
                var R: String
                var G: String
                var B: String
                val random = Random()
                R = Integer.toHexString(random.nextInt(256)).toUpperCase()
                G = Integer.toHexString(random.nextInt(256)).toUpperCase()
                B = Integer.toHexString(random.nextInt(256)).toUpperCase()
                R = if (R.length == 1) "0$R" else R
                G = if (G.length == 1) "0$G" else G
                B = if (B.length == 1) "0$B" else B
                return "#$R$G$B"
            }
    }
}