package com.omjoonkim.app.githubBrowserApp.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

/**
 * Input, Output을 인터페이스로 선언하고 각 ViewModel(AAC)에 맞는 Input, Output 객체 생성
 * +) State는 kotlin data class로 생성 [syk]
 */
interface Input
interface Output

/**
 * 모든 ViewModel의 Base 클래스 [syk]
 */
abstract class BaseViewModel : ViewModel(){
    /**
     * 'CompositeDisposable'을 만들어서 ViewModel(BaseViewModel)자신이 cleared될 때, 내가 가진 모든 Disposable들을 clear시킴 [syk]
     */
    protected val compositeDisposable : CompositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

