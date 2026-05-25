package com.kryptonsensi.optimization

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var btnOptimize: Button
    private lateinit var btnOpenFF: Button
    private lateinit var tvRAMInfo: TextView
    private lateinit var tvDeviceInfo: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var ivLogo: ImageView

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.GET_TASKS,
        android.Manifest.permission.KILL_BACKGROUND_PROCESSES,
        android.Manifest.permission.WRITE_SETTINGS,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        requestPermissions()
        updateRAMInfo()
        updateDeviceInfo()

        btnOptimize.setOnClickListener { optimizeDevice() }
        btnOpenFF.setOnClickListener { openFreeFire() }
    }

    private fun initViews() {
        btnOptimize = findViewById(R.id.btn_optimize)
        btnOpenFF = findViewById(R.id.btn_open_ff)
        tvRAMInfo = findViewById(R.id.tv_ram_info)
        tvDeviceInfo = findViewById(R.id.tv_device_info)
        progressBar = findViewById(R.id.progress_bar)
        ivLogo = findViewById(R.id.iv_logo)
    }

    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 101)
        }
    }

    private fun updateRAMInfo() {
        thread {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)

            val totalRAM = memInfo.totalMem / (1024 * 1024 * 1024)
            val availableRAM = memInfo.availMem / (1024 * 1024 * 1024)
            val usedRAM = totalRAM - availableRAM

            runOnUiThread {
                tvRAMInfo.text = "📊 RAM: ${usedRAM}GB / ${totalRAM}GB\n💾 Disponível: ${availableRAM}GB"
            }
        }
    }

    private fun updateDeviceInfo() {
        val deviceName = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        val processor = Build.HARDWARE

        tvDeviceInfo.text = "📱 $deviceName | Android $androidVersion\n⚙️ Processador: $processor"
    }

    private fun optimizeDevice() {
        progressBar.visibility = android.view.View.VISIBLE
        btnOptimize.isEnabled = false

        thread {
            try {
                // Limpar cache
                clearCache()

                // Matar apps em background
                killBackgroundApps()

                // Reduzir animações
                reduceAnimations()

                // Limpar memória
                cleanRAM()

                Thread.sleep(2000)

                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    btnOptimize.isEnabled = true
                    tvRAMInfo.text = "✅ Otimização Completa!"
                    updateRAMInfo()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    btnOptimize.isEnabled = true
                }
            }
        }
    }

    private fun clearCache() {
        try {
            val dir = cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: java.io.File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.listFiles()
            if (children != null) {
                for (child in children) {
                    deleteDir(child)
                }
            }
        }
        return dir?.delete() ?: false
    }

    private fun killBackgroundApps() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses

        for (process in runningProcesses) {
            if (process.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                try {
                    activityManager.killBackgroundProcesses(process.processName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun reduceAnimations() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.Global.putFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0.5f)
                Settings.Global.putFloat(contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 0.5f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanRAM() {
        val runtime = Runtime.getRuntime()
        runtime.gc()
    }

    private fun openFreeFire() {
        try {
            startActivity(packageManager.getLaunchIntentForPackage("com.dts.freefireth")!!)
        } catch (e: Exception) {
            // Se não estiver instalado, abrir na Play Store
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.dts.freefireth")
            })
        }
    }
}
