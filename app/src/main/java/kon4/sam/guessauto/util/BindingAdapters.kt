package kon4.sam.guessauto.util

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import kon4.sam.guessauto.R

@SuppressLint("UseCompatLoadingForDrawables")
@BindingAdapter("setImageFromUrl")
fun setImageFromUrl(view: ImageView, url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(url)
            .into(view)
    } else {
        view.scaleType = ImageView.ScaleType.CENTER_CROP
        view.setImageDrawable(view.context.resources.getDrawable(R.drawable.avatar_default))
    }
}