package com.omjoonkim.app.githubBrowserApp.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omjoonkim.app.githubBrowserApp.databinding.ActivityMainBinding
import com.omjoonkim.app.githubBrowserApp.databinding.ViewholderUserInfoBinding
import com.omjoonkim.app.githubBrowserApp.databinding.ViewholderUserRepoBinding
import com.omjoonkim.app.githubBrowserApp.showToast
import com.omjoonkim.app.githubBrowserApp.ui.BaseActivity
import com.omjoonkim.app.githubBrowserApp.viewmodel.MainViewModel
import com.omjoonkim.project.githubBrowser.domain.entity.Repo
import com.omjoonkim.app.githubBrowserApp.R
import com.omjoonkim.app.githubBrowserApp.ui.repo.RepoDetailActivity
import com.omjoonkim.project.githubBrowser.domain.entity.User
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class  MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [syk][DataBiding]
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.setLifecycleOwner(this)

        val viewModel = getViewModel<MainViewModel> {
            parametersOf(intent.data.path.substring(1)) // [syk] parametersOf를 통해 스트림 넘겨줌 cf.SearchActivity
            // >> Koin에서 주입하는 객체를 가지고 올 때, 꼭 거기 정의된 값들로만 안의 생성자를 담을 수 있는 것이 아니므로
            //    여기서 내 해당 데이터 가지고 오려고 할 때 값을 넣어서 initialize가능 >> 현재 받은 username을 넘겨줌
        }

        binding.viewModel = viewModel

        /**
         * [람다함수를 넘겨서 값으로 Setter해주려고 했으나 DataBiding에서 안되므로 편의상 처리한 코드]
         * 1) onclick, homebutton이 표출되었을 때,  [syk]]
         */
        actionbarInit(binding.toolbar, onClickHomeButton = {
            viewModel.input.clickHomeButton() // 2) Viewmodel의 'onClickHomeButton'을 클릭 [syk]]
        })

        /**
         * Router쪽을 Biding시킨 것
         */
        with(viewModel.output) {
            // refreshListData()가 왔을 때, recyclerView.adapter를 갱신시켜라
            refreshListData().observe { (user, repos) ->
                binding.recyclerView.adapter = MainListAdapter(
                    user,
                    repos,
                    viewModel.input::clickUser,
                    viewModel.input::clickRepo
                )
            }

            // 에러토스트 왔을 때 토스트 띄워라
            showErrorToast().observe { showToast(it) }

            // ProfileActivity나 시그널 왔을 떄 액티비티 이동시켜라
            goProfileActivity().observe {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("githubbrowser://repos/$it")
                    )
                )
            }
            goRepoDetailActivity().observe {
                RepoDetailActivity.start(this@MainActivity, it.first, it.second)
            }

            // finish()란 게 왔을 떄 화면을 종료시켜라
            finish().observe {
                onBackPressed()
            }
        }
    }

    private inner class MainListAdapter(
        val user: User,
        val repos: List<Repo>,
        val onClickUser: (User) -> Unit,
        val onClickRepo: (User, Repo) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEWTYPE_USER_INFO = 0
        private val VIEWTYPE_USER_REPO = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                0 -> UserInfoViewHolder(ViewholderUserInfoBinding.inflate(layoutInflater)).apply {
                    itemView.setOnClickListener {
                        onClickUser.invoke(user)
                    }
                }
                1 -> UserRepoViewHolder(ViewholderUserRepoBinding.inflate(layoutInflater)).apply {
                    itemView.setOnClickListener {
                        onClickRepo.invoke(user, repos[adapterPosition - 1])
                    }
                }
                else -> throw IllegalStateException()
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is UserRepoViewHolder -> holder.bind(repos[position - 1])
                is UserInfoViewHolder -> holder.bind(user)
            }
        }

        override fun getItemViewType(position: Int): Int =
            if (position == 0)
                VIEWTYPE_USER_INFO
            else
                VIEWTYPE_USER_REPO

        override fun getItemCount(): Int = repos.size + 1

        private inner class UserInfoViewHolder(
            private val binding: ViewholderUserInfoBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: User) {
                binding.data = item
            }
        }

        private inner class UserRepoViewHolder(
            private val binding: ViewholderUserRepoBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: Repo) {
                binding.data = item
            }
        }
    }
}
