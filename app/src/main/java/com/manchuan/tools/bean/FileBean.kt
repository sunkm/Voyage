package com.manchuan.tools.bean

import java.io.Serializable


/**
 * date：2018/2/24 on 17:00
 * description: 文件详情
 */
class FileBean(
    var filePath: String, var fileLength: Long, //MD5码：保证文件的完整性
    var md5: String,
) :
    Serializable {
    companion object {
        const val serialVersionUID = "6321689524634663223356"
    }
}
