package com.thesis.inspestor_mobileapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import com.thesis.inspestor_mobileapp.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        val splash = binding.root
        setContentView(splash)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)


        Handler(Looper.myLooper()!!).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 5000)

        val sideAnim = AnimationUtils.loadAnimation(this, R.anim.side_anim)
        binding.logoImage.animation = sideAnim
        binding.introSlogan.animation = sideAnim

        val botAnim = AnimationUtils.loadAnimation(this, R.anim.bot_anim)
        binding.createdBy.animation = botAnim
    }
}