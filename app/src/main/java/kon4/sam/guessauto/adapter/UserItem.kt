package kon4.sam.guessauto.adapter



import android.view.View
import androidx.core.content.ContextCompat
import com.xwray.groupie.databinding.BindableItem
import kon4.sam.guessauto.R
import kon4.sam.guessauto.data.model.User
import kon4.sam.guessauto.databinding.ScoreItemRecyclerBinding

import kon4.sam.guessauto.util.setImageFromUrl

class UserItem(val user: User, private val is_me: Boolean): BindableItem<ScoreItemRecyclerBinding>() {
    fun initializeViewBinding(view: View): ScoreItemRecyclerBinding {
        return ScoreItemRecyclerBinding.bind(view)
    }

    override fun bind(binding: ScoreItemRecyclerBinding, position: Int) {
        setImageFromUrl( binding.userPhotoIcon, user.picture_url)
        binding.scoreTv.text = binding.root.context.resources.getString(R.string.user_score_ph, user.score)
        binding.nameTv.text = user.user_name
        if (is_me) {
            binding.root.context
            binding.scoreItemLayout.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, R.color.bluePrimary))
            binding.scoreTv.setTextColor(
                ContextCompat.getColor(binding.root.context, R.color.greyPrimary))
            binding.nameTv.setTextColor(
                ContextCompat.getColor(binding.root.context, R.color.greyPrimary))
        }
    }

    override fun getLayout(): Int {
        return R.layout.score_item_recycler
    }
}