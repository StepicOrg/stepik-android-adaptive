package org.stepik.android.adaptive.pdd.ui.fragment

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.stepik.android.adaptive.pdd.Config
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.core.presenter.LoginPresenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.pdd.data.AnalyticMgr
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr
import org.stepik.android.adaptive.pdd.data.model.*
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding
import org.stepik.android.adaptive.pdd.ui.activity.StudyActivity
import org.stepik.android.adaptive.pdd.ui.adapter.OnboardingQuizCardsAdapter

class OnboardingFragment : Fragment(), LoginView {
    companion object {
        private val ONBOARDING_CARDS_COUNT = 4
    }

    private lateinit var binding : FragmentRecommendationsBinding
    private val presenter = LoginPresenter()
    private var completed = 0

    private val adapter = OnboardingQuizCardsAdapter {
        updateToolbar()
        if (it == 0) Completable.fromAction { SharedPreferenceMgr.getInstance().isNotFirstTime = true }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        initOnboardingCards()
        presenter.attachView(this)

        Observable.fromCallable(SharedPreferenceMgr.getInstance()::getAuthResponseDeadline)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it == 0L)
                        createMockAccount()
                    else
                        onSuccess()
                }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentRecommendationsBinding>(inflater, R.layout.fragment_recommendations, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.cardsContainer.setAdapter(adapter)
        binding.progress.visibility = View.GONE

        binding.tryAgain.setOnClickListener {
            binding.error.visibility = View.GONE
            binding.cardsContainer.visibility = View.VISIBLE
            createMockAccount()
        }

        binding.expLevel.text = getString(R.string.tutorial)

        binding.expProgress.max = ONBOARDING_CARDS_COUNT

        updateToolbar()

        return binding.root
    }

    private fun updateToolbar() {
        val progress = ONBOARDING_CARDS_COUNT - adapter.getItemCount()
        binding.expProgress.progress = progress
        binding.expCounter.text =  progress.toString()
        if (adapter.getItemCount() == 0) {
            binding.expLevelNext.visibility = View.GONE
        } else {
            binding.expLevelNext.visibility = View.VISIBLE
            binding.expLevelNext.text = resources.getQuantityString(R.plurals.steps_in_tutorial, adapter.getItemCount(), adapter.getItemCount())
        }
    }

    override fun onDestroyView() {
        adapter.detach()
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter.detachView(this)
        super.onDestroy()
    }

    override fun onSuccess() {
        completed++
        onComplete()
    }

    override fun onNetworkError() {
        binding.error.visibility = View.VISIBLE

        binding.progress.visibility = View.GONE
        binding.cardsContainer.visibility = View.GONE
    }

    override fun onError(errorBody: String) {
        onNetworkError()
    }

    override fun onLoading() {
        if (completed == 1) {
            binding.progress.visibility = View.VISIBLE
        }
    }

    private fun onComplete() {
        if (completed == 2) {
            AnalyticMgr.getInstance().onBoardingFinished()
            startActivity(Intent(this@OnboardingFragment.context, StudyActivity::class.java))
            this@OnboardingFragment.activity.finish()
        }
    }

    private fun initOnboardingCards() {
        adapter.add(createMockCard(-1, R.string.onboarding_card_title_1, R.string.onboarding_card_question_1))
        adapter.add(createMockCard(-2, R.string.onboarding_card_title_2, R.string.onboarding_card_question_2))
        adapter.add(createMockCard(-3, R.string.onboarding_card_title_3, R.string.onboarding_card_question_3))
        adapter.add(createMockCard(-4, R.string.onboarding_card_title_4, R.string.onboarding_card_question_4))
    }

    private fun createMockCard(id: Long, @StringRes title_id: Int, @StringRes question_id: Int) : Card =
            Card(id, Lesson(getString(title_id)), Step(Block(getString(question_id))), Attempt(Dataset(listOf(), false)))


    private fun createMockAccount() {
        val email = "adaptive_${Config.getInstance().courseId}_android_${System.currentTimeMillis()}${Util.randomString(5)}@stepik.org"
        val password = Util.randomString(16)
        val firstName = Util.randomString(10)
        val lastName = Util.randomString(10)
        presenter.createAccount(firstName, lastName, email, password)
    }
}