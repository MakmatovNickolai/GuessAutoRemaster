package kon4.sam.guessauto.adapter

import androidx.core.content.ContextCompat
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kon4.sam.guessauto.R
import kon4.sam.guessauto.network.model.User
import kon4.sam.guessauto.util.setImageFromUrl
import kotlinx.android.synthetic.main.score_item_recycler.view.*

class UserItem(val user: User, private val is_me: Boolean): Item<GroupieViewHolder>()  {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        setImageFromUrl(viewHolder.itemView.user_photo_icon, user.url)
        viewHolder.itemView.scoreTv.text = viewHolder.itemView.context.resources.getString(R.string.user_score_ph, user.score)
        viewHolder.itemView.nameTv.text = user.user_name
        if (is_me) {
            viewHolder.itemView.score_item_layout.setBackgroundColor(
                ContextCompat.getColor(viewHolder.itemView.context, R.color.bluePrimary))
            viewHolder.itemView.scoreTv.setTextColor(
                ContextCompat.getColor(viewHolder.itemView.context, R.color.greyPrimary))
            viewHolder.itemView.nameTv.setTextColor(
                ContextCompat.getColor(viewHolder.itemView.context, R.color.greyPrimary))
        }
    }

    override fun getLayout(): Int {
        return R.layout.score_item_recycler
    }
}