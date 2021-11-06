package kon4.sam.guessauto.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kon4.sam.guessauto.App
import kon4.sam.guessauto.R
import kon4.sam.guessauto.adapter.UserItem
import kon4.sam.guessauto.databinding.FragmentScoreBinding
import kon4.sam.guessauto.view_model.ScoreViewModel
import kotlinx.android.synthetic.main.fragment_score.*
import kotlinx.android.synthetic.main.fragment_score.toolbar2

@AndroidEntryPoint
class ScoreFragment : Fragment() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val viewModel: ScoreViewModel by viewModels()
    private lateinit var binding: FragmentScoreBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerViewAdapter()
        setupObservers()
        getInitialTopUsers()
    }

    private fun setupToolbar() {
        toolbar2.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_new_24)
        toolbar2.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.getAllScoreCompleted.observe(viewLifecycleOwner, { users ->
            topLoadingProgress.visibility = View.GONE
            val scoreText = resources.getString(R.string.your_record) + " " + App.user.score
            val text =   if (App.user.user_name.isEmpty()) scoreText else App.user.user_name + ", " + scoreText
            myRecordTextView.text = text
            topLoadingProgress.visibility = View.GONE
            users?.forEach {
                if (it.user_name == App.user.user_name) {
                    adapter.add(UserItem(it, true))
                } else {
                    adapter.add(UserItem(it,false))
                }
            }
        })
    }

    private fun getInitialTopUsers() {
        topLoadingProgress.visibility = View.VISIBLE
        viewModel.getInitialTopUsers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_score, container, false)
        return binding.root
    }

    private fun setupRecyclerViewAdapter() {
        binding.scoreRecycler.adapter = adapter
        (binding.scoreRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false // disable rerendering animation
        val layoutManager = LinearLayoutManager(activity)
        binding.scoreRecycler.layoutManager = layoutManager
        binding.scoreRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (viewModel.page < 6) {
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = linearLayoutManager.itemCount
                    val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    if (!recyclerView.canScrollVertically(1) && totalItemCount == lastVisibleItem + 1) {
                        viewModel.getScoreUsersNextPage()
                    }
                }
            }
        })
    }
}