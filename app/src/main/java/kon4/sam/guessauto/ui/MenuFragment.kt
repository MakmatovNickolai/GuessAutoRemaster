package kon4.sam.guessauto.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.*
import kon4.sam.guessauto.R
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.android.synthetic.main.fragment_menu.view.*

class MenuFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBannerAd()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        view.start.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_gameFragment)
        }
        view.change.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_userFragment)
        }
        view.records.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_scoreFragment)
        }
        view.share.setOnClickListener {
            (this.activity as MainActivity).shareAppWithFriends(it)
        }
        view.rate_app.setOnClickListener {
            (this.activity as MainActivity).openPlayMarketToRateApp(it)
        }
        return view
    }

    private fun setBannerAd() {
        val mAdView = AdView(requireActivity().applicationContext)
        mAdView.adSize = AdSize.BANNER
        mAdView.adUnitId = resources.getString(R.string.adMobBannerId)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}