package com.omjoonkim.app.githubBrowserApp.ui.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.omjoonkim.app.githubBrowserApp.R
import com.omjoonkim.app.githubBrowserApp.databinding.ActivitySearchBinding
import com.omjoonkim.app.githubBrowserApp.ui.BaseActivity
import com.omjoonkim.app.githubBrowserApp.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SearchActivity : BaseActivity() {

    private val keyboardController by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [syk][DataBiding]
        val binding = DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)
        binding.lifecycleOwner = this // [syk] lifecycleOwner를 바인딩한 객체에게 줌 --이를 통해-->> 데이터 변화에 따라 View갱신 & View가 종료 시 해당 stream도 종료됨

        actionbarInit(binding.toolbar, isEnableNavi = false)

        val viewModel = getViewModel<SearchViewModel>() // [syk] Koin을 통해 ViewModel을 얻어옮
        binding.viewModel = viewModel

        // [syk] Router 부분 : 검색시 이름값을 전달받아 ResultActivity로 이동
        viewModel.output.goResultActivity() // [syk] LiveData를 Observe하므로 ViewModel이 clear || View가 종료 시 해당 stream도 종료됨
            .observe {
                keyboardController.hideSoftInputFromWindow(binding.editText.windowToken, 0)
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("githubbrowser://repos/$it")
                    )
                )
            }
    }
}
