package com.manchuan.tools.utils

import android.app.Activity
import androidx.annotation.RequiresApi
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import android.content.Intent
import android.provider.DocumentsContract
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import java.io.*
import java.lang.Exception

class fileUriUtils(//内部操作Activity对象
    var context: Activity, requestCode: Int
) {
    private val fileName: String? = null
    var requestCode = 11 //请求标识

    /**
     * 构造方法
     *
     * @context # Activity对象
     * @requestCode #请求码
     */
    init {
        this.requestCode = requestCode
    }

    /**
     * 申请data访问权限请在onActivityResult事件中调用savePermissions方法保存权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun requestPermission() {
        var uri1 =
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
        uri1 = DocumentFile.fromTreeUri(context, uri1!!)!!.uri
        val intent1 = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent1.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri1)
        context.startActivityForResult(intent1, requestCode)
    }

    /**
     * 保存权限onActivityResult返回的参数全部传入即可
     *
     * @requestCode #onActivityResult
     * @resultCode #onActivityResult
     * @data #onActivityResult
     */
    @SuppressLint("WrongConstant")
    fun savePermissions(requestCode: Int, resultCode: Int, data: Intent) {
        if (this.requestCode != requestCode) return
        try {
            val uri = data.data ?: return
            context.contentResolver.takePersistableUriPermission(
                uri,
                data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 将sdcard中的文件拷贝至data目录中
     *
     * @return #返回一个boolean true成功 false 失败
     * @sourcePath #sdcard中的完整文件路径
     * @targetDir #拷贝至的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     */
    fun copyToData(
        sourcePath: String?,
        targetDir: String,
        targetName: String,
        fileType: String?
    ): Boolean {
        var targetDir = targetDir
        targetDir = textual(targetDir, targetName, "")
        return if (File(sourcePath).exists()) {
            try {
                val inStream: InputStream = FileInputStream(sourcePath)
                val buffer = ByteArray(inStream.available())
                val uri1 =
                    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
                var documentFile = DocumentFile.fromTreeUri(context, uri1)
                val list = targetDir.split("/").toTypedArray()
                var i = 0
                while (i < list.size) {
                    if (list[i] != "") {
                        val a = getDocumentFile1(documentFile, list[i])
                        documentFile = if (a == null) {
                            assert(documentFile != null)
                            documentFile!!.createDirectory(list[i])
                        } else {
                            a
                        }
                    }
                    i++
                }
                var newFile: DocumentFile? = null
                newFile = if (exists(documentFile, targetName)) {
                    assert(documentFile != null)
                    documentFile!!.findFile(targetName)
                } else {
                    assert(documentFile != null)
                    documentFile!!.createFile(fileType!!, targetName)
                }
                val excelOutputStream = context.contentResolver.openOutputStream(
                    newFile!!.uri
                )
                var byteread: Int
                while (inStream.read(buffer).also { byteread = it } != -1) {
                    excelOutputStream!!.write(buffer, 0, byteread)
                }
                inStream.close()
                excelOutputStream!!.close()
                true
            } catch (var8: Exception) {
                var8.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    /**
     * 将Android/data中的文件拷贝至sdcard
     *
     * @return #返回一个boolean true成功 false 失败
     * @sourceDir #文件原目录以data开始 如拷贝data/test/目录中的文件 那就是 /test
     * @sourceFilename #拷贝的文件名 如拷贝 data/test/1.txt 那就是1.txt
     * @targetPath #目标文件路径需提供完整的路径目录+文件名
     */
    fun copyToSdcard(sourceDir: String, sourceFilename: String?, targetPath: String?): Boolean {
        return try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = sourceDir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    val a = getDocumentFile1(documentFile, list[i])
                    documentFile = if (a == null) {
                        assert(documentFile != null)
                        documentFile!!.createDirectory(list[i])
                    } else {
                        a
                    }
                }
                i++
            }
            assert(documentFile != null)
            documentFile = documentFile!!.findFile(sourceFilename!!)
            val inputStream = context.contentResolver.openInputStream(
                documentFile!!.uri
            )
            val buffer = ByteArray(inputStream!!.available())
            val fs = FileOutputStream(targetPath)
            var byteread: Int
            while (inputStream.read(buffer).also { byteread = it } != -1) {
                fs.write(buffer, 0, byteread)
            }
            inputStream.close()
            fs.close()
            true
        } catch (var8: Exception) {
            var8.printStackTrace()
            false
        }
    }

    /**
     * 删除data目录中的指定路径的文件
     *
     * @return #返回一个boolean true成功 false 失败
     * @dir #删除文件的目录目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
     */
    fun delete(dir: String, fileName: String?): Boolean {
        return try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = dir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    val a = getDocumentFile1(documentFile, list[i])
                    documentFile = a ?: documentFile!!.createDirectory(list[i])
                }
                i++
            }
            documentFile = documentFile!!.findFile(fileName!!)
            documentFile!!.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 重命名文件
     *
     * @return #返回一个boolean true成功 false 失败
     * @dir #重命名文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
     * @targetName #重命名后的文件名
     */
    fun renameTo(dir: String, fileName: String?, targetName: String?): Boolean {
        return try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = dir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    val a = getDocumentFile1(documentFile, list[i])
                    documentFile = a ?: documentFile!!.createDirectory(list[i])
                }
                i++
            }
            documentFile = documentFile!!.findFile(fileName!!)
            documentFile!!.renameTo(targetName!!)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createDirectory(dir: String, targetName: String?) {
        try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = dir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    val a = getDocumentFile1(documentFile, list[i])
                    documentFile = a ?: documentFile!!.createDirectory(list[i])
                }
                i++
            }
            documentFile!!.createDirectory(targetName!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun delete(dir: String): Boolean {
        return try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = dir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    val a = getDocumentFile1(documentFile, list[i])
                    documentFile = a ?: documentFile!!.createDirectory(list[i])
                }
                i++
            }
            documentFile!!.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取目录下所有文件返回文本型数组
     *
     * @return #返回一个文本数组为该目录下所有的文件名
     * @dir #文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     */
    fun getList(dir: String): Array<String?>? {
        return try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = dir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    val a = getDocumentFile1(documentFile, list[i])
                    documentFile = a ?: documentFile!!.createDirectory(list[i])
                }
                i++
            }
            val documentFile1 = documentFile!!.listFiles()
            val res = arrayOfNulls<String>(documentFile1.size)
            var i1 = 0
            while (i1 < documentFile1.size) {
                res[i1] = documentFile1[i1].name
                i1++
            }
            res
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 将byte[] 写出到data目录的文件中如果没有这个文件会自动创建目录及文件
     *
     * @return #返回一个boolean true成功 false 失败
     * @Dir #写出的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     */
    fun write(dir: String, fileName: String, fileType: String?, bytes: ByteArray): Boolean {
        return try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = dir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    val a = getDocumentFile1(documentFile, list[i])
                    documentFile = a ?: documentFile!!.createDirectory(list[i])
                }
                i++
            }
            var newFile: DocumentFile? = null
            newFile = if (exists(documentFile, fileName)) {
                documentFile!!.findFile(fileName)
            } else {
                documentFile!!.createFile(fileType!!, fileName)
            }
            val excelOutputStream = context.contentResolver.openOutputStream(
                newFile!!.uri
            )
            doDataOutput2(bytes, excelOutputStream)
        } catch (var5: Exception) {
            var5.printStackTrace()
            false
        }
    }

    /**
     * 将byte[] 写出到data目录的文件中如果没有这个文件会自动创建目录及文件
     *
     * @return #返回一个byte[] 如文件为空或者不存在此返回可能为null请判断后使用
     * @Dir #写出的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     */
    fun read(dir: String, fileName: String?): ByteArray? {
        var buffer: ByteArray? = null
        val arrayOutputStream = ByteArrayOutputStream()
        var inputStream: InputStream? = null
        try {
            val uri1 =
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
            var documentFile = DocumentFile.fromTreeUri(context, uri1)
            val list = dir.split("/").toTypedArray()
            var i = 0
            while (i < list.size) {
                if (list[i] != "") {
                    documentFile = getDocumentFile1(documentFile, list[i])
                }
                i++
            }
            documentFile = documentFile!!.findFile(fileName!!)
            inputStream = context.contentResolver.openInputStream(documentFile!!.uri)
            buffer = ByteArray(inputStream!!.available())
            while (true) {
                val readLength = inputStream.read(buffer)
                if (readLength == -1) break
                arrayOutputStream.write(buffer, 0, readLength)
            }
            inputStream.close()
            arrayOutputStream.close()
        } catch (var5: Exception) {
            var5.printStackTrace()
            if (inputStream != null) {
                try {
                    inputStream.close()
                    arrayOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return buffer
    }

    private fun doDataOutput2(bytes: ByteArray, outputStream: OutputStream?): Boolean {
        return try {
            outputStream!!.write(bytes, 0, bytes.size)
            outputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                outputStream!!.close()
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
            false
        }
    }

    private fun exists(documentFile: DocumentFile?, name: String): Boolean {
        return try {
            documentFile!!.findFile(name)!!.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getDocumentFile(documentFile: DocumentFile?, dir: String): DocumentFile? {
        if (documentFile == null) return null
        val documentFiles = documentFile.listFiles()
        var res: DocumentFile? = null
        var i = 0
        while (i < documentFile.length()) {
            if (documentFiles[i].name == dir && documentFiles[i].isDirectory) {
                res = documentFiles[i]
                return res
            }
            i++
        }
        return res
    }

    private fun getDocumentFile1(documentFile: DocumentFile?, dir: String): DocumentFile? {
        if (documentFile == null) return null
        try {
            val documentFiles = documentFile.listFiles()
            var res: DocumentFile? = null
            var i = 0
            while (i < documentFile.length()) {
                if (documentFiles[i].name == dir && documentFiles[i].isDirectory) {
                    res = documentFiles[i]
                    return res
                }
                i++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private fun textual(str: String, find: String, replace: String): String {
            var find = find
            return if ("" != find && "" != str) {
                find = "\\Q$find\\E"
                str.replace(find.toRegex(), replace)
            } else {
                ""
            }
        }

        fun readDataFromInputStream(context: Context, fileName: String?): String? {
            var fileContent: String? = null
            try {
                val f = File(context.resources.assets.open(fileName!!).toString())
                if (f.isFile && f.exists()) {
                    fileContent = ""
                    val read = InputStreamReader(
                        FileInputStream(f), "UTF-8"
                    )
                    val reader = BufferedReader(read)
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        fileContent += line
                    }
                    read.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return fileContent
        }

        fun getFromAssets(context: Context, fileName: String?): String? {
            var Result: String? = ""
            try {
                val inputReader = InputStreamReader(
                    context.resources.assets.open(
                        fileName!!
                    )
                )
                val bufReader = BufferedReader(inputReader)
                var line: String? = ""
                while (bufReader.readLine().also { line = it } != null) Result += line
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return Result
        }

        fun readStringFromAssets(context: Context, fileName: String?): String {
            val assetManager = context.assets
            var inputStream: InputStream? = null
            var isr: InputStreamReader? = null
            var br: BufferedReader? = null
            val sb = StringBuffer()
            try {
                inputStream = assetManager.open(fileName!!)
                isr = InputStreamReader(inputStream)
                br = BufferedReader(isr)
                sb.append(br.readLine())
                var line: String? = null
                while (br.readLine().also { line = it } != null) {
                    sb.append(
                        """
    
    $line
    """.trimIndent()
                    )
                }
                br.close()
                isr.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    br?.close()
                    isr?.close()
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return sb.toString()
        }

        fun readFile(filePathAndName: String?): String? {
            var fileContent: String? = null
            try {
                val f = File(filePathAndName)
                if (f.isFile && f.exists()) {
                    fileContent = ""
                    val read = InputStreamReader(
                        FileInputStream(f), "UTF-8"
                    )
                    val reader = BufferedReader(read)
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        fileContent += line
                    }
                    read.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return fileContent
        }
    }
}