package com.manchuan.tools.utils

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import kotlin.math.roundToInt

/**
 * 获取硬件信息工具类，如：CPU参数、内存参数等
 */
object HardwareInfoUtils {

    //获取CPU整体温度，单位：°C
    suspend fun getCPUTemperature() : Float{
        var temperature : String
        withContext(Dispatchers.IO){
            val temperatureOfCPUFileName = "/sys/class/thermal/thermal_zone0/temp"
            temperature = readCpuInfo(temperatureOfCPUFileName)
        }
        return (temperature.toFloat() / 1000.0F)
    }

    /**
     * 获取CPU整体使用率，返回是"百分比"
     * 如返回：22.0，表示CPU占用率是22.0%
     */
    internal suspend fun getCPUUsage() : Float{
        var rate = 0.00f
        withContext(Dispatchers.IO){
            var process : Process? = null
            try{
                process = Runtime.getRuntime().exec("top -n 1")
                val reader : BufferedReader = BufferedReader( InputStreamReader(process.inputStream))
                var line : String ?= null
                var cpuIndex = -1
                while((reader.readLine().also { line = it }) != null){
                    line = line!!.trim()
                    if(line == null){
                        continue
                    }
                    val tempIndex = getCPUIndex(line!!)
                    if(tempIndex != -1){
                        cpuIndex = tempIndex
                        continue
                    }
                    if(line!!.startsWith(android.os.Process.myPid().toString())){
                        if(cpuIndex == -1){
                            continue
                        }
                        val param = line!!.split("\\s+".toRegex())
                        if(param.size <= cpuIndex){
                            continue
                        }
                        var cpu = param[cpuIndex]
                        if(cpu.endsWith("%")){
                            cpu = cpu.substring(0,cpu.lastIndexOf("%"))
                        }
                        rate =  cpu.toFloat()/ Runtime.getRuntime().availableProcessors()
                        break
                    }
                }
            }catch (e : IOException){
                e.printStackTrace()
            }finally {
                process?.destroy()
            }
        }
        return (((rate * 100).roundToInt())/100).toFloat()
    }

    //获取BaseFrequency
    suspend fun getBaseFrequency() : List<String>? {
        var cpuFreqs: List<String>? = null
        withContext(Dispatchers.IO) {
            val cpufreq0 =
                readCpuInfo("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            val cpufreq1 =
                readCpuInfo("/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq")
            val cpufreq2 =
                readCpuInfo("/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq")
            val cpufreq3 =
                readCpuInfo("/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq")
            val cpufreq4 =
                readCpuInfo("/sys/devices/system/cpu/cpu4/cpufreq/scaling_cur_freq")
            val cpufreq5 =
                readCpuInfo("/sys/devices/system/cpu/cpu5/cpufreq/scaling_cur_freq")
            cpuFreqs = listOf(cpufreq0, cpufreq1, cpufreq2, cpufreq3, cpufreq4, cpufreq5)
        }
        return cpuFreqs
    }

    //获取系统总内存大小,单位：MB
    suspend fun getTotalMemory() : Int{
        val fileNameMemInfo = "/proc/meminfo"      //系统内存信息
        val str2 : String
        var total = 0
        withContext(Dispatchers.IO){
            try {
                val localFileReader : FileReader = FileReader(fileNameMemInfo)
                val localBufferedReader : BufferedReader = BufferedReader(localFileReader,8192);
                str2 = localBufferedReader.readLine();          //读取meminfo第一行：系统总内存大小
                val arrayOfString = str2.split(" ")
                localBufferedReader.close()
                total = arrayOfString[8].toInt()/1024
            }catch (e : IOException){
                e.printStackTrace()
            }
        }
        return total
    }

    //获取当前可用（剩余）运行内存大小，单位：MB
    suspend fun getAvailMemory(context: Context) : Int{
        var availMem : Int = 0
        withContext(Dispatchers.IO){
            val am : ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi : ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
            try {
                am.getMemoryInfo(mi)
                availMem = (mi.availMem/1048576).toInt()         //转换成MB 1048576=1024*1024
            }catch (e : IOException){
                e.printStackTrace()
            }
        }
        return availMem
    }

    //获取当前应用内存占用，单位：MB
    suspend fun getAppOccupyMemory(context: Context) : Int {
        var memSize : Long = 0
        withContext(Dispatchers.IO){
            val am : ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val pid = android.os.Process.myPid()
            val pids = intArrayOf(pid)
            try {
                val memInfo = am.getProcessMemoryInfo(pids)
                memSize = memInfo[0].totalPss.toLong()
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
        return (memSize/1024).toInt()
    }

    //获取内存当前频率，单位：Mhz
    suspend fun getMemBaseFrequency() : Float{
        var fre = 0
        withContext(Dispatchers.IO){
            val memFreFileName = "/sys/devices/platform/dmc/devfreq/dmc/cur_freq"
            val dmcFreq = readCpuInfo(memFreFileName)
            val memFre = dmcFreq.split("\\s+".toRegex())
            fre = memFre[0].toLong().toInt()
        }
        return (fre/1000000.0).toFloat()                            //转换为Mhz
    }

    //根据文件路径读取CPU信息
    private fun readCpuInfo(path: String): String {
        val tempFile = File(path)
        if (!tempFile.exists()) return ""
        var input : FileInputStream ?= null
        try {
            input = FileInputStream(tempFile)
            val bytes = ByteArray(64)
            val leng = input.read(bytes)
            val temp = String(bytes, 0, if (leng == -1) 0 else leng)
            input.close()
            return temp
        }catch (e : IOException){
            e.printStackTrace()
        }finally {
            input?.close()
        }
        return ""
    }

    //获取CPU数值所在的位置
    private fun getCPUIndex(line : String) : Int{
        if(line.contains("CPU")){
            val titles = line.split("\\s+".toRegex())
            for((index, str) in titles.withIndex()){
                if(str.contains("CPU"))
                    return index
            }
        }
        return -1
    }


}
