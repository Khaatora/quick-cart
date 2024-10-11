package com.senseicoder.quickcart.features.main.ui.currency

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.senseicoder.quickcart.R
import com.senseicoder.quickcart.core.global.Constants
import com.senseicoder.quickcart.core.model.CurrencyResponse
import com.senseicoder.quickcart.core.network.currency.CurrencyRemoteImpl
import com.senseicoder.quickcart.core.repos.currency.CurrencyRepoImpl
import com.senseicoder.quickcart.core.services.SharedPrefsService
import com.senseicoder.quickcart.core.wrappers.ApiState
import com.senseicoder.quickcart.databinding.FragmentCurrencyBinding
import com.senseicoder.quickcart.features.main.ui.main_activity.MainActivity
import com.senseicoder.quickcart.features.main.ui.main_activity.viewmodels.MainActivityViewModel
import com.senseicoder.quickcart.features.main.ui.main_activity.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.launch

class CurrencyFragment : Fragment() {
    companion object{
        private const val TAG = "CurrencyFragment"
    }
    private val buttons:MutableList<RadioButton> = mutableListOf()
    private lateinit var binding: FragmentCurrencyBinding
     var code : String = SharedPrefsService.getSharedPrefString(
        Constants.CURRENCY,
        Constants.CURRENCY_DEFAULT
    )
    private val mainViewmodel: MainActivityViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            MainActivityViewModelFactory(
                CurrencyRepoImpl(
                    CurrencyRemoteImpl
                )
            )
        )[MainActivityViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCurrencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        code = SharedPrefsService.getSharedPrefString(
        Constants.CURRENCY,
        Constants.CURRENCY_DEFAULT
        )
        Log.d(TAG, "onViewCreated: ${SharedPrefsService.getSharedPrefString(
            Constants.CURRENCY,
            Constants.CURRENCY_DEFAULT
        )}")
        startCollect()
        binding.apply {
            imgBtnBack.setOnClickListener {
                Navigation.findNavController(it).popBackStack()
            }
            Constants.Currency.currencyMap.forEach {
                rdGroup.addView(
                    RadioButton(binding.root.context).apply {
                        buttons.add(this)
                        text = String.format("${it.key} - ${it.value}")
                        textSize = 24f
                        buttonTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.primary
                                )
                            )
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 16, 0, 16)
                            setPadding(16, 0, 0, 0)
                        }
                        layoutParams = params // Apply the LayoutParams to the RadioButton
                    }
                )
            }
            buttons.forEach{
                if (it.text.toString().substring(0,4) == code){
                    it.isChecked = true
                }
            }
            rdGroup.setOnCheckedChangeListener { l, p ->
                val rdBtnChecked = l.findViewById<RadioButton>(p)
                buttons.forEach{
                    it.isChecked = false
                }
                rdBtnChecked.isChecked = true
                code = rdBtnChecked.text.toString().substring(0, 4)
                mainViewmodel.getCurrencyRate(code)
            }
        }
    }

    fun setChangeInCurrency(response: CurrencyResponse) {
        SharedPrefsService.apply {
            Constants.apply {
                setSharedPrefString(CURRENCY, code)
                response.data.get(code)?.value?.toFloat()
                    ?.let { setSharedPrefFloat(PERCENTAGE_OF_CURRENCY_CHANGE, it) }
            }
        }
    }
    private fun startCollect(){
        lifecycleScope.launch {
            mainViewmodel.currency.collect {
                when (it) {
                    is ApiState.Success -> {
                        setChangeInCurrency(it.data)
                        Snackbar.make(
                            requireView(),
                            "Success Changed Currency",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    is ApiState.Failure -> {
                        Snackbar.make(requireView(), it.msg, Snackbar.LENGTH_SHORT).show()
                    }

                    else -> Snackbar.make(requireView(), "Waiting . . . .", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).hideBottomNavBar()
    }

}