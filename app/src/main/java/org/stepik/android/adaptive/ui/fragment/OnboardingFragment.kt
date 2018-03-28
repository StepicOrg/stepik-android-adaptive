package org.stepik.android.adaptive.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.core.presenter.LoginPresenter
import org.stepik.android.adaptive.core.presenter.contracts.LoginView
import org.stepik.android.adaptive.data.Analytics
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.data.model.*
import org.stepik.android.adaptive.databinding.FragmentRecommendationsBinding
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.ui.activity.StudyActivity
import org.stepik.android.adaptive.ui.adapter.OnboardingQuizCardsAdapter
import org.stepik.android.adaptive.gamification.achievements.AchievementManager
import javax.inject.Inject

class OnboardingFragment : Fragment(), LoginView {
    companion object {
        private const val ONBOARDING_CARDS_COUNT = 4
    }

    private lateinit var binding : FragmentRecommendationsBinding
    private var completed = 0

    @Inject
    lateinit var achievementManager: AchievementManager

    @Inject
    lateinit var analytics: Analytics

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    @Inject
    @field:BackgroundScheduler
    lateinit var backgroundScheduler: Scheduler

    @Inject
    lateinit var presenter: LoginPresenter

    @Inject
    lateinit var sharedPreferenceMgr: SharedPreferenceMgr

    private val adapter = OnboardingQuizCardsAdapter {
        updateToolbar(true)
        if (it == 0) Completable.fromAction {
            sharedPreferenceMgr.isNotFirstTime = true
            achievementManager.onEvent(AchievementManager.Event.ONBOARDING, 1)
        }
                .observeOn(mainScheduler)
                .subscribe(this::onSuccess)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager()
                .studyComponent
                .inject(this)
        retainInstance = true
        initOnboardingCards()
        presenter.attachView(this)

        Observable.fromCallable(sharedPreferenceMgr::authResponseDeadline)
                .observeOn(mainScheduler)
                .subscribe {
                    if(it == 0L)
                        createMockAccount()
                    else
                        onSuccess()
                }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecommendationsBinding.inflate(inflater, container, false)
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
        binding.expInc.text = getString(R.string.exp_inc, 1)

        updateToolbar(false)

        binding.questionsPacks.visibility = View.GONE

        return binding.root
    }

    private fun updateToolbar(animate: Boolean) {
        val progress = ONBOARDING_CARDS_COUNT - adapter.getItemCount()
        binding.expProgress.progress = progress
        binding.expCounter.text =  progress.toString()

        if (animate) {
            binding.expInc.alpha = 1f
            binding.expInc.animate()
                    .alpha(0f)
                    .setInterpolator(DecelerateInterpolator())
                    .setStartDelay(1500)
                    .setDuration(200)
                    .start()
        }

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
            analytics.onBoardingFinished()
            startActivity(Intent(this@OnboardingFragment.context, StudyActivity::class.java))
            this@OnboardingFragment.activity.finish()
        }
    }

    private fun initOnboardingCards() {
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_1, R.string.onboarding_card_question_1))
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_2, R.string.onboarding_card_question_2))
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_3, R.string.onboarding_card_question_3))
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_4, R.string.onboarding_card_question_4))
    }

    private fun createMockCard(id: Long, @StringRes title_id: Int, @StringRes question_id: Int) : Card =
            Card(id, Lesson(getString(title_id)), Step(Block(getString(question_id))), Attempt(Dataset(listOf(), false)))


    private fun createMockAccount() {
        Observable.fromCallable(sharedPreferenceMgr::fakeUser)
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribe {
                    if (it.value != null) {
                        // we got here if on some reason server returns us 401, so we try to re-login with existing fake account
                        presenter.authWithLoginPassword(it.value.login, it.value.password, true)
                    } else {
                        presenter.createFakeUser()
                    }
                }
    }
}