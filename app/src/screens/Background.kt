package com.<your>.<application>
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
class Background : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_background)
		Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/nf5HEkTlmW/rdn6yian_expires_30_days.png").into(findViewById(R.id.r8u2h6ux9wvl))
	}
}