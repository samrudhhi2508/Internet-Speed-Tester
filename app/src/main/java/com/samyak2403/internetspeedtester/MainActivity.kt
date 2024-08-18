package com.samyak2403.internetspeedtester

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.ekn.gruzer.gaugelibrary.Range
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.samyak2403.internetspeedtester.AdsLab.AdUtils
import com.samyak2403.internetspeedtester.databinding.ActivityMainBinding
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import java.text.DecimalFormat
import kotlin.math.floor

class MainActivity : AppCompatActivity(), ISpeedTestListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var context: Context
    private var startTime: Long = 0
    private var test = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the Toolbar as the ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        context = this

        init()
        loadAds()  // Call the method to load native ads
    }

    private fun loadAds() {
        val adLoader = AdLoader.Builder(context, AdUtils.NATIVE_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                val styles = NativeTemplateStyle.Builder().build()
                val template = binding.myTemplate // Assuming you have a TemplateView in your layout with this ID
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
            }
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun init() {
        setupGauge()

        binding.startTestBT.setOnClickListener {
            binding.startTestBT.visibility = View.GONE
            Thread { getNetSpeed() }.start()
        }
    }

    private fun setupGauge() {
        val range = Range().apply {
            color = ContextCompat.getColor(context, R.color.red)
            from = 0.0
            to = 50.0
        }

        val range1 = Range().apply {
            color = ContextCompat.getColor(context, R.color.orange)
            from = 50.0
            to = 100.0
        }

        val range2 = Range().apply {
            color = ContextCompat.getColor(context, R.color.green)
            from = 100.0
            to = 150.0
        }

        with(binding.speedGauge) {
            addRange(range)
            addRange(range1)
            addRange(range2)

            minValue = 0.0
            maxValue = 150.0
            value = 0.0

            setValueColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
    }

    private fun getNetSpeed() {
        test = 99
        val speedTestSocket = SpeedTestSocket()

        binding.speedGauge.setValueColor(ContextCompat.getColor(context, R.color.colorWhite))

        startTime = System.currentTimeMillis()
        speedTestSocket.addSpeedTestListener(this)
        speedTestSocket.startDownload("http://ipv4.appliwave.testdebit.info/50M.iso", 100)
    }

    override fun onCompletion(report: SpeedTestReport) {
        val r = report.transferRateBit.toFloat() / 1000000
        runOnUiThread {
            binding.speedGauge.value = floor(r.toDouble())

            binding.startTestBT.visibility = View.VISIBLE
            binding.speedGauge.setValueColor(ContextCompat.getColor(context, android.R.color.transparent))

            binding.speedTxt.text = String.format("%s MB/s", DecimalFormat("##").format(r))
            binding.latencyTxt.text = String.format("%s ms", (System.currentTimeMillis() - startTime) / 600)
            binding.startTestBT.text = getString(R.string.start)
        }
        Log.d("SpeedTest", "onCompletion: $r")
    }

    override fun onProgress(percent: Float, report: SpeedTestReport) {
        val r = report.transferRateBit.toFloat() / 1000000
        runOnUiThread { binding.speedGauge.value = floor(r.toDouble()) }
        Log.d("SpeedTest", "onProgress: ${r / 1000000}")
    }

    override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
        Log.d("SpeedTest", "onError: $errorMessage")
    }
}
