package org.stepik.android.adaptive.pdd.ui.fragment

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepik.android.adaptive.pdd.Config
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.Util
import org.stepik.android.adaptive.pdd.core.presenter.LoginPresenter
import org.stepik.android.adaptive.pdd.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.pdd.data.AnalyticMgr
import org.stepik.android.adaptive.pdd.data.model.*
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding
import org.stepik.android.adaptive.pdd.ui.activity.StudyActivity
import org.stepik.android.adaptive.pdd.ui.adapter.OnboardingQuizCardsAdapter

class OnboardingFragment : Fragment(), LoginView {
    private lateinit var binding : FragmentRecommendationsBinding
    private val presenter = LoginPresenter()
    private var completed = 0

    private val adapter = OnboardingQuizCardsAdapter { onSuccess() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        initOnboardingCards()
        presenter.attachView(this)
        createMockAccount()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentRecommendationsBinding>(inflater, R.layout.fragment_recommendations, container, false)

        (activity as AppCompatActivity).setSupportActionBar(binding.fragmentRecommendationsToolbar)

        
        binding.fragmentRecommendationsCardsContainer.setAdapter(adapter)
        binding.fragmentRecommendationsProgress.visibility = View.GONE

        binding.fragmentRecommendationsTryAgain.setOnClickListener {
            binding.fragmentRecommendationsError.visibility = View.GONE
            binding.fragmentRecommendationsCardsContainer.visibility = View.VISIBLE
            createMockAccount()
        }

        return binding.root
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
        binding.fragmentRecommendationsError.visibility = View.VISIBLE

        binding.fragmentRecommendationsProgress.visibility = View.GONE
        binding.fragmentRecommendationsCardsContainer.visibility = View.GONE
    }

    override fun onError(errorBody: String) {
        onNetworkError()
    }

    override fun onLoading() {
        if (completed == 1) {
            binding.fragmentRecommendationsProgress.visibility = View.VISIBLE
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
        adapter.add(createMockCard(-3, R.string.onboarding_card_title_3, R.string.onboarding_card_question_1))
        adapter.add(createMockCard(-4, R.string.onboarding_card_title_4, R.string.onboarding_card_question_1))
    }

    private fun createMockCard(id: Long, @StringRes title_id: Int, @StringRes  question_id: Int) : Card =
            Card(id, Lesson(getString(title_id)), Step(Block(getString(question_id))), Attempt(Dataset(listOf(), false)))


    private fun createMockAccount() {
        val email = "adaptive_${Config.getInstance().courseId}_android_${System.currentTimeMillis()}${Util.randomString(5)}@stepik.org"
        val password = Util.randomString(16)
        val firstName = Util.randomString(10)
        val lastName = Util.randomString(10)
        presenter.createAccount(firstName, lastName, email, password)
    }
}