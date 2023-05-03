package kon4.sam.guessauto.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.*
import kon4.sam.guessauto.R
import kon4.sam.guessauto.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBannerAd()
    }

    private lateinit var binding: FragmentMenuBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        binding.start.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_gameFragment)
        }
        binding.change.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_userFragment)
        }
        binding.records.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_scoreFragment)
        }
        binding.share.setOnClickListener {
            (this.activity as MainActivity).shareAppWithFriends(it)
        }
        binding.rateApp.setOnClickListener {
            (this.activity as MainActivity).openPlayMarketToRateApp(it)
        }
        return binding.root
    }

    @SuppressLint("VisibleForTests")
    private fun setBannerAd() {
        val mAdView = AdView(requireActivity().applicationContext)
        mAdView.setAdSize(AdSize.BANNER)
        mAdView.adUnitId = resources.getString(R.string.adMobBannerId)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }
}