package com.fss.fsswebrtc_01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        var btnLive : Button = findViewById(R.id.btn_live)
        btnLive.setOnClickListener {
            val intent = Intent(this@ContainerActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}