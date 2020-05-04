package com.omjoonkim.app.githubBrowserApp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.omjoonkim.app.githubBrowserApp.Logger
import com.omjoonkim.app.githubBrowserApp.rx.Parameter
import com.omjoonkim.app.githubBrowserApp.rx.takeWhen
import io.reactivex.subjects.PublishSubject

class SearchViewModel(
    logger: Logger
) : BaseViewModel() {
    /**
     * Propertiest 구성 [syk]
     * - 'input'이라는 InPut 인터페이스를 구현한 객체
     * - name, clickSearchButton : 누군가 자신을 subscribe하지 않아도 결과를 발행하는 'Subject' (cf. Observable)이며, subscribe()한 시점 이후 결과만 받는 'PublishSubject'
     * - 'output'이라는 OutPut 인터페이스를 구현한 객체
     * - state, goResultActivity : private로 가려져 있고, output의 함수들의 리턴타입은 'LiveData'이다. >> 내가 가진 데이터를 외부에서 변경하지 않도록 막은 조치 [syk]
     */
    private val name = PublishSubject.create<String>()
    private val clickSearchButton = PublishSubject.create<Parameter>()
    val input = object : SearchViewModelInPuts {
        // input 메서드 호출 시 내가가진 subject의 해당 input값을 방출???? >> ViewModel이 Biding될 떄 모든 로직이 정해져 있다.
        // 이를 위해 RxJava를 이용해서 코드 작성 >> 여러 Input스트림을 받고 조합하고 이를 통해 최종 결과물 Output내뿜게 하는 모든 로직이 사전에 결정(여기 명시)
        override fun name(name: String) =
            this@SearchViewModel.name.onNext(name)
        override fun clickSearchButton() =
            this@SearchViewModel.clickSearchButton.onNext(Parameter.CLICK)
    }

    private val state = MutableLiveData<SearchViewState>()
    private val goResultActivity = MutableLiveData<String>()
    val output = object : SearchViewModelOutPuts {
        override fun state() = state //[syk][fp] 함수 몸체가 식인 경우 return 생략 가능, return type이 추론됨
        override fun goResultActivity() = goResultActivity
    }

    /**
     * 로직 구성 [syk]
     * - BaseViewModel의 compositeDisposable에 내가 짜놓은 모든 로직 추가하여 cleared 시 clear되도록 함
     */
    init {
        compositeDisposable.addAll(
            /**
             * 로직1) 이름값 존재여부에 따른 버튼 활설화여부 변경 [syk]
             */
            name.map { SearchViewState(it.isNotEmpty()) } // 1) name이라는 위에서 생성한 subject로 만든 스트림 방출 시, name이 빈값이냐 아니냐에 따른 항상 새로운 'SearchViewState'객체 생성하고
                .subscribe(state::setValue, logger::d), // 2) state(MutableLiveData)의 값을 변경시켜서 View에게 Notify해줌

            /**
             * 로직2) 검색 버튼 클릭 시 name값을 Result화면으로 이동시켜주는 Route인 'goResultActivity'에 담아 넘겨줌  [syk]
             */
            name.takeWhen(clickSearchButton) { _, t2 -> t2 }
                .subscribe(goResultActivity::setValue, logger::d)
        )
    }
}

/**
 * Input 인터페이스 [syk]
 */
interface SearchViewModelInPuts : Input {
    fun name(name: String)
    fun clickSearchButton()
}
/**
 * OutPut 인터페이스 [syk]
 */
interface SearchViewModelOutPuts : Output {
    fun state(): LiveData<SearchViewState>
    fun goResultActivity(): LiveData<String>
}

/**
 * Sate 데이터 클래스 [syk]
 */
data class SearchViewState(
    val enableSearchButton: Boolean
)
