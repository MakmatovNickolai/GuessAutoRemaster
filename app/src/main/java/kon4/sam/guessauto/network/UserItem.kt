package kon4.sam.guessauto.network

import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kon4.sam.guessauto.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.score_item_recycler.view.*

class UserItem(val user: User, val is_me: Boolean): Item<GroupieViewHolder>()  {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView2Score.text = user.user_name +": " + user.score
        if (is_me) {
            viewHolder.itemView.textView2Score.setBackgroundColor(
                ContextCompat.getColor(viewHolder.itemView.context, R.color.colorPrimary))
        }

    }

    override fun getLayout(): Int {
        return R.layout.score_item_recycler
    }
}