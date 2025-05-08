package com.<your>.<application>
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
class Group3 : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_group3)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/nf5HEkTlmW/7th1jbd0_expires_30_days.png").into(findViewById(R.id.ryy0i0f64w4))
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/nf5HEkTlmW/gwscmcpo_expires_30_days.png").into(findViewById(R.id.rhtq742y1wkn))
		val button1: View = findViewById(R.id.rbw1jqwiltta)
		button1.setOnClickListener {
			println("Pressed")
		}
	}
}