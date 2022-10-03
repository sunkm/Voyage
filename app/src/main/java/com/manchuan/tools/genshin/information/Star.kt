package com.manchuan.tools.genshin.information

import com.manchuan.tools.R
import com.manchuan.tools.databinding.ItemMaterialBinding
import com.manchuan.tools.genshin.ext.loadImage

class Star {
    companion object {
        fun getStarResourcesByStarNum(star: Int, small: Boolean): Int {
            return if (small) {
                when (star) {
                    105 -> R.drawable.icon_star_105s
                    5 -> R.drawable.icon_star_5s
                    4 -> R.drawable.icon_star_4s
                    3 -> R.drawable.icon_star_3s
                    2 -> R.drawable.icon_star_2s
                    else -> R.drawable.icon_star_1s
                }
            } else {
                when (star) {
                    105 -> R.drawable.icon_star_105
                    5 -> R.drawable.icon_star_5
                    4 -> R.drawable.icon_star_4
                    3 -> R.drawable.icon_star_3
                    2 -> R.drawable.icon_star_2
                    else -> R.drawable.icon_star_1
                }
            }
        }

        fun getStarSymbolByStarNum(star: Int): String {
            return when (star) {
                5 -> "★★★★★"
                4 -> "★★★★"
                3 -> "★★★"
                2 -> "★★"
                else -> "★"
            }
        }

        fun setStarBackgroundAndIcon(bind: ItemMaterialBinding, icon: String, star: Int) {
            bind.starBackground.setImageResource(getStarResourcesByStarNum(star, false))
            loadImage(bind.avatar, icon)
        }

    }
}