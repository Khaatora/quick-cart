package com.senseicoder.quickcart.features.main.ui.brand

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senseicoder.quickcart.R
import com.senseicoder.quickcart.core.dialogs.MyDialog
import com.senseicoder.quickcart.core.global.Constants
import com.senseicoder.quickcart.core.wrappers.NetworkConnectivity
import com.senseicoder.quickcart.core.wrappers.ApiState
import com.senseicoder.quickcart.databinding.FragmentBrandBinding
import com.senseicoder.quickcart.features.main.ui.brand.viewmodel.BrandViewModel
import com.senseicoder.quickcart.features.main.ui.main_activity.MainActivity
import com.senseicoder.quickcart.features.main.ui.main_activity.viewmodels.MainActivityViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class BrandFragment : Fragment(), OnItemProductClicked {

    private var _binding: FragmentBrandBinding? = null
    private val binding get() = _binding!!
    private lateinit var brandAdapter: BrandAdapter
    private lateinit var brandViewModel: BrandViewModel
    var priceClicked = false
    var categoryClicked = false
    var brand : String? = null

    private val networkConnectivity by lazy {
        NetworkConnectivity.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBrandBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        brandViewModel =
            ViewModelProvider(this)[BrandViewModel::class.java]

        brand = arguments?.getString("brand")

        binding.filterValue.visibility = View.GONE

        binding.swipeRefresher.setColorSchemeResources(R.color.primary_faint)

        seekBarConfigration()

        if (networkConnectivity.isOnline()) {
            binding.connectivity.visibility = View.VISIBLE
            binding.filterValue.visibility = View.GONE
            binding.noConnectivity.visibility = View.GONE
        } else {
            binding.connectivity.visibility = View.GONE
            binding.noConnectivity.visibility = View.VISIBLE
        }

        binding.swipeRefresher.setOnRefreshListener {
            refresh()
        }

        binding.priceFilter.setOnClickListener {
            if (!priceClicked) {
                priceClicked = true
                categoryClicked = false
                brandViewModel.filter = true
                brandViewModel.dataFiltered = brandViewModel.allData
                binding.priceFilter.setBackgroundResource(R.color.primary_faint)
                binding.categoryFilter.setBackgroundResource(R.color.white)
                binding.priceFilter.setTextColor(resources.getColor(R.color.white))
                binding.categoryFilter.setTextColor(resources.getColor(R.color.primary_faint))
                binding.filterValue.visibility = View.VISIBLE
                binding.seekBar.visibility = View.VISIBLE
                binding.categoryGroup.visibility = View.GONE
                binding.priceDisplay.visibility = View.VISIBLE
                brandViewModel.filterByPrice(
                    String.format(
                        "%.2f",
                        binding.seekBar.progress.toFloat()
                    )
                )
                brandViewModel.seekMax()
                binding.seekBar.max = brandViewModel.maxPrice
            } else {
                priceClicked = false
                categoryClicked = false
                brandViewModel.filter = false
                binding.priceFilter.setBackgroundResource(R.color.white)
                binding.categoryFilter.setBackgroundResource(R.color.white)
                binding.priceFilter.setTextColor(resources.getColor(R.color.primary_faint))
                binding.categoryFilter.setTextColor(resources.getColor(R.color.primary_faint))
                binding.seekBar.visibility = View.GONE
                binding.priceDisplay.visibility = View.GONE
                brandViewModel.displayAllProducts()
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int,
                fromUser: Boolean,
            ) {
                binding.priceDisplay.text = "${getString(R.string.price)}: $progress"
                if (progress != 0) {
                    val price = String.format("%.2f", progress.toFloat())
                    brandViewModel.filterByPrice(price)
                } else {
                    brandViewModel.displayAllProducts()
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                binding.swipeRefresher.isEnabled = false
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                binding.swipeRefresher.isEnabled = true
            }
        })

        binding.categoryFilter.setOnClickListener {
            if (!categoryClicked) {
                categoryClicked = true
                priceClicked = false
                brandViewModel.filter = true
                brandViewModel.dataFiltered = brandViewModel.allData
                binding.priceFilter.setBackgroundResource(R.color.white)
                binding.categoryFilter.setBackgroundResource(R.color.primary_faint)
                binding.priceFilter.setTextColor(resources.getColor(R.color.primary_faint))
                binding.categoryFilter.setTextColor(resources.getColor(R.color.white))
                binding.shoes.isChecked = true
                brandViewModel.filterByCategory(binding.shoes.text.toString())
                binding.filterValue.visibility = View.VISIBLE
                binding.categoryGroup.visibility = View.VISIBLE
                binding.seekBar.visibility = View.GONE
                binding.priceDisplay.visibility = View.GONE

            } else {
                categoryClicked = false
                priceClicked = false
                brandViewModel.filter = false
                brandViewModel.dataFiltered = brandViewModel.allData
                binding.priceFilter.setBackgroundResource(R.color.white)
                binding.categoryFilter.setBackgroundResource(R.color.white)
                binding.priceFilter.setTextColor(resources.getColor(R.color.primary_faint))
                binding.categoryFilter.setTextColor(resources.getColor(R.color.primary_faint))
                binding.categoryGroup.visibility = View.GONE
                brandViewModel.displayAllProducts()
            }
        }

        binding.shoes.setOnClickListener {
            brandViewModel.filterByCategory(binding.shoes.text.toString())
        }

        binding.accessories.setOnClickListener {
            brandViewModel.filterByCategory(binding.accessories.text.toString())
        }

        binding.shirt.setOnClickListener {
            brandViewModel.filterByCategory(binding.shirt.text.toString())
        }

        brandViewModel.getProductInBrand(brand ?: "")

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                brandViewModel.products.collectLatest {
                    when (it) {
                        is ApiState.Loading -> {
                            if (networkConnectivity.isOnline()) {
                                Log.d(TAG, "onViewCreated: ")
                                binding.noConnectivity.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                                binding.shimmerFrameLayoutBrand.startShimmer()
                            }else{
                                binding.connectivity.visibility = View.GONE
                                binding.noConnectivity.visibility = View.VISIBLE
                            }

                        }
                        is ApiState.Success -> {
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.shimmerFrameLayoutBrand.visibility = View.GONE
                            binding.shimmerFrameLayoutBrand.stopShimmer()
                            if (!brandViewModel.filter) {
                                brandViewModel.allData = it.data
                            }
                            brandAdapter = BrandAdapter(this@BrandFragment)
                            binding.recyclerView.apply {
                                adapter = brandAdapter
                                brandAdapter.submitList(it.data)
                                layoutManager = GridLayoutManager(context, 2).apply {
                                    orientation = RecyclerView.VERTICAL
                                }
                            }
                        }

                        else -> {
                            if (!networkConnectivity.isOnline()) {
                                binding.connectivity.visibility = View.GONE
                                binding.noConnectivity.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).hideBottomNavBar()
    }

    override fun productClicked(id: Long) {
        if (networkConnectivity.isOnline()) {
            ViewModelProvider(requireActivity())[MainActivityViewModel::class.java].setCurrentProductId(id.toString())
            findNavController().navigate(R.id.action_brandFragment_to_productDetailsFragment)
            }else{
            val dialog = MyDialog()
            dialog.showAlertDialog("Please, check your connection",requireContext())
        }
    }

    private fun seekBarConfigration() {
        binding.seekBar.visibility = View.GONE

        binding.seekBar.progressDrawable
            .setColorFilter(resources.getColor(R.color.primary_faint), PorterDuff.Mode.SRC_ATOP)

        binding.seekBar.thumb.setColorFilter(
            resources.getColor(R.color.primary_faint),
            PorterDuff.Mode.SRC_ATOP
        )

    }

    private fun refresh(){
        if (networkConnectivity.isOnline()) {
            binding.connectivity.visibility = View.VISIBLE
            binding.noConnectivity.visibility = View.GONE
            binding.filterValue.visibility = View.GONE
            brandViewModel.getProductInBrand(brand ?: "")

        } else {
            binding.connectivity.visibility = View.GONE
            binding.noConnectivity.visibility = View.VISIBLE
        }

        binding.swipeRefresher.isRefreshing = false
    }


    companion object{
        private const val TAG = "BrandFragment"
    }
}