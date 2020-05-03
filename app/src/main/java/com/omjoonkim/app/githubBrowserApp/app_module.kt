package com.omjoonkim.app.githubBrowserApp

import androidx.databinding.library.BuildConfig
import com.omjoonkim.app.githubBrowserApp.ui.repo.RepoDetailPresenter
import com.omjoonkim.app.githubBrowserApp.ui.repo.RepoDetailView
import com.omjoonkim.app.githubBrowserApp.viewmodel.MainViewModel
import com.omjoonkim.app.githubBrowserApp.viewmodel.SearchViewModel
import com.omjoonkim.project.githubBrowser.data.interactor.ForkDataRepository
import com.omjoonkim.project.githubBrowser.data.interactor.RepoDataRepository
import com.omjoonkim.project.githubBrowser.data.interactor.UserDataRepository
import com.omjoonkim.project.githubBrowser.data.source.GithubBrowserRemote
import com.omjoonkim.project.githubBrowser.domain.interactor.usecases.GetRepoDetail
import com.omjoonkim.project.githubBrowser.domain.interactor.usecases.GetUserData
import com.omjoonkim.project.githubBrowser.domain.repository.ForkRepository
import com.omjoonkim.project.githubBrowser.domain.repository.RepoRepository
import com.omjoonkim.project.githubBrowser.domain.repository.UserRepository
import com.omjoonkim.project.githubBrowser.domain.schedulers.SchedulersProvider
import com.omjoonkim.project.githubBrowser.remote.GithubBrowserRemoteImpl
import com.omjoonkim.project.githubBrowser.remote.GithubBrowserServiceFactory
import com.omjoonkim.project.githubBrowser.remote.mapper.ForkEntityMapper
import com.omjoonkim.project.githubBrowser.remote.mapper.RepoEntityMapper
import com.omjoonkim.project.githubBrowser.remote.mapper.UserEntityMapper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val myModule: Module = module {
    //presentation레이어
    /**
     * 'viewModel' : Koin 지원 AAC ViewModel 키워드
     * - AAC ViewModel은
     *    Activity나 Fragment에서 ViewModel을 저장하는 인터페이스를 가질 수 있고,
     *   이런 것들에 따라 savedinstances나 앱이 다시 켜질 때, Map에 캐싱되어 있는 ViewModel을 가져오도록 안드로이드가 구현되어 있고
     *   이걸 도와주도록 하는 코드 ↓
     */
    viewModel { (id: String) -> MainViewModel(id, get(), get()) }
    viewModel { SearchViewModel(get()) }
    factory { (view: RepoDetailView) -> RepoDetailPresenter(view, get()) }

    //app레이어
    single { Logger() }
    single { AppSchedulerProvider() as SchedulersProvider }

    //domain레이어
    /**
     * 2) 이는 domain레이어에서 'SchedulersProvider'가 구현되어 있으며,
     *    이 객체는 바깥의 app레이어에서 구현한 'SchedulersProvider'를 넘겨받은 것이다.(<<의존의 역전 구현)
     */
    factory { GetUserData(get(), get(), get()) } // 1) GetUserData 생성자 파라미터 중 3번째 값이 'SchedulersProvider'이다.
    factory { GetRepoDetail(get(), get(), get()) }

    //data레이어
    single { UserDataRepository(get()) as UserRepository }
    single { RepoDataRepository(get()) as RepoRepository }
    single { ForkDataRepository(get()) as ForkRepository }

    //remote레이어
    single { GithubBrowserRemoteImpl(get(), get(), get(), get()) as GithubBrowserRemote }
    single { RepoEntityMapper() }
    single { UserEntityMapper() }
    single { ForkEntityMapper(get()) }
    single {
        GithubBrowserServiceFactory.makeGithubBrowserService(
            BuildConfig.DEBUG,
            "https://api.github.com"
        )
    }
}
