package com.omjoonkim.app.githubBrowserApp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.omjoonkim.app.githubBrowserApp.Logger
import com.omjoonkim.app.githubBrowserApp.call
import com.omjoonkim.app.githubBrowserApp.error.Error
import com.omjoonkim.app.githubBrowserApp.error.UnExpected
import com.omjoonkim.app.githubBrowserApp.rx.Parameter
import com.omjoonkim.app.githubBrowserApp.rx.neverError
import com.omjoonkim.project.githubBrowser.domain.entity.Repo
import com.omjoonkim.project.githubBrowser.domain.entity.User
import com.omjoonkim.project.githubBrowser.domain.interactor.usecases.GetUserData
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject


class MainViewModel(
    searchedUserName: String, // [syk] 이 ViewModel이 가지고 있어야할 String값
    private val getUserData: GetUserData, // [syk] Usecase
    logger: Logger
) : BaseViewModel() {

    private val clickUser = PublishSubject.create<User>()
    private val clickRepo = PublishSubject.create<Pair<User, Repo>>()
    private val clickHomeButton = PublishSubject.create<Parameter>()
    val input: MainViewModelInputs = object : MainViewModelInputs {
        override fun clickUser(user: User) = clickUser.onNext(user)
        override fun clickRepo(user: User, repo: Repo) = clickRepo.onNext(user to repo)
        override fun clickHomeButton() = clickHomeButton.onNext(Parameter.CLICK)
    }

    private val state = MutableLiveData<MainViewState>()
    private val refreshListData = MutableLiveData<Pair<User, List<Repo>>>()
    private val showErrorToast = MutableLiveData<String>()
    private val goProfileActivity = MutableLiveData<String>()
    private val goRepoDetailActivity = MutableLiveData<Pair<String, String>>()
    private val finish = MutableLiveData<Unit>()
    val output = object : MainViewModelOutPuts {
        override fun state() = state
        override fun refreshListData() = refreshListData
        override fun showErrorToast() = showErrorToast
        override fun goProfileActivity() = goProfileActivity
        override fun goRepoDetailActivity() = goRepoDetailActivity
        override fun finish() = finish
    }

    init {
        val error = PublishSubject.create<Throwable>() // [syk] 에러발생 시 에러 방출해줄 스트림이 필요하여 생성
        val userName = Observable.just(searchedUserName).share() // [syk] ViewModel이 받은 searchedUserName란 String값을 스트림으로 만들기 위해 생성
        val requestListData = userName.flatMapMaybe { // TODO study flatMapMaybe
            // [syk] 1) 서버에서 필요한 데이터를 받아오기 위한 Usecase에 대한 스트림 생성
            // >> 2) 로딩이 끝났을 떄 또는 이 데이터를 받아왔을 때, 리스트를 화면에보여주는 Route 코드를 실행시키기 위해
            getUserData.get(it).neverError(error)
        }.share() // [syk] 3) 여러군데 사용할 수 있으므로 share()로 스트림 생성 // TODO study share()

        /**
         * 로직 구성 [syk]
         * - BaseViewModel의 compositeDisposable에 내가 짜놓은 모든 로직 추가하여 cleared 시 clear되도록 함
         */
        compositeDisposable.addAll(
                /**
                 * 로직1) State를 담당 [syk]
                 */
            Observables
                .combineLatest(
                    Observable.merge(
                        requestListData.map { false },
                        error.map { false }
                    ).startWith(true), // 여기까지는 언제로딩을 띄워줄거냐에 관한 스트림을 하나로 만들어 처리한 것 [syk]
                    userName, // title에 필요한 값으로 스트림조합해서 넘겨줌
                    ::MainViewState
                ).subscribe(state::setValue, logger::d),

                /**
                 * 로직2) Viewmodel 생성되고 바인딩되어 서버에서 데이터 불러왔을 때
                 *        해당 데이터 방출되면 Router에게 해당 데이터를 RecyclerView에 넘겨주는 행위를 하기위한 output [syk]
                 */
            requestListData.subscribe(refreshListData::setValue, logger::d),

                /**
                 * 로직3) 에러발생 시 핸들링 [syk]
                 */
            error.map {
                if (it is Error)
                    it.errorText
                else UnExpected.errorText
            }.subscribe(showErrorToast::setValue, logger::d),

                /**
                 * 로직4) 에러발생 시 핸들링 [syk]
                 */
            clickUser.map { it.name }.subscribe(goProfileActivity::setValue, logger::d),
                /**
                 * 로직5) 사용자 클릭시 프로필 화면으로 이동 [syk]
                 */
            clickRepo.map { it.first.name to it.second.name }.subscribe(goRepoDetailActivity::setValue, logger::d),
                /**
                 * 로직4) back버튼 클릭 시 앱 종료 [syk]
                 */
            clickHomeButton.subscribe(finish::call, logger::d)
        )
    }
}
/**
 * Input 인터페이스 [syk]
 */
interface MainViewModelInputs : Input {
    fun clickUser(user: User)
    fun clickRepo(user: User, repo: Repo)
    fun clickHomeButton()
}
/**
 * OutPut 인터페이스 [syk]
 */
interface MainViewModelOutPuts : Output {
    fun state(): LiveData<MainViewState>
    fun refreshListData(): LiveData<Pair<User, List<Repo>>>
    fun showErrorToast(): LiveData<String>
    fun goProfileActivity(): LiveData<String>
    fun goRepoDetailActivity(): LiveData<Pair<String, String>>
    fun finish(): LiveData<Unit>
}
/**
 * Sate 데이터 클래스 [syk]
 */
data class MainViewState(
    val showLoading: Boolean,
    val title: String
)
