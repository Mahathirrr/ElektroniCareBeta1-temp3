package com.example.elektronicarebeta1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val page = pages[position]
        holder.bind(page)
    }

    override fun getItemCount() = pages.size

    class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.onboardingImage)
        private val titleView: TextView = view.findViewById(R.id.onboardingTitle)
        private val descriptionView: TextView = view.findViewById(R.id.onboardingDescription)

        fun bind(page: OnboardingPage) {
            imageView.setImageResource(page.imageRes)
            titleView.text = page.title
            descriptionView.text = page.description
        }
    }
}
