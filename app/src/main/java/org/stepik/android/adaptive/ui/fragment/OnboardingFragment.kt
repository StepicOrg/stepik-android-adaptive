package org.stepik.android.adaptive.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.api.auth.AuthError
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.AuthPresenter
import org.stepik.android.adaptive.core.presenter.contracts.AuthView
import org.stepik.android.adaptive.data.analytics.AmplitudeAnalytics
import org.stepik.android.adaptive.data.analytics.Analytics
import org.stepik.android.adaptive.data.model.*
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.databinding.FragmentRecommendationsBinding
import org.stepik.android.adaptive.di.qualifiers.BackgroundScheduler
import org.stepik.android.adaptive.di.qualifiers.MainScheduler
import org.stepik.android.adaptive.gamification.achievements.AchievementManager
import org.stepik.android.adaptive.ui.adapter.OnboardingQuizCardsAdapter
import org.stepik.android.adaptive.util.addDisposable
import javax.inject.Inject

class OnboardingFragment : Fragment(), AuthView {
    companion object {
        private const val ONBOARDING_CARDS_COUNT = 4
    }

    private lateinit var binding: FragmentRecommendationsBinding
    private var completed = 0

    private val disposable = CompositeDisposable()

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
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var screenManager: ScreenManager

    private lateinit var presenter: AuthPresenter

    private val adapter = OnboardingQuizCardsAdapter {
        updateToolbar(true)

        if (it == 0) {
            Completable
                .fromAction {
                    sharedPreferenceHelper.isNotFirstTime = true
                    achievementManager.onEvent(AchievementManager.Event.ONBOARDING, 1)
                }
                .observeOn(mainScheduler)
                .subscribe(this::onSuccess)
        } else {
            analytics.logAmplitudeEvent(
                AmplitudeAnalytics.Onboarding.SCREEN_OPENED,
                mapOf(AmplitudeAnalytics.Onboarding.PARAM_SCREEN to ONBOARDING_CARDS_COUNT - it + 1)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager()
            .studyComponent
            .inject(this)
        retainInstance = true

        presenter = ViewModelProvider(this, viewModelFactory).get(AuthPresenter::class.java)
        initOnboardingCards()
        presenter.attachView(this)

        analytics.logAmplitudeEvent(
            AmplitudeAnalytics.Onboarding.SCREEN_OPENED,
            mapOf(AmplitudeAnalytics.Onboarding.PARAM_SCREEN to 1)
        )

        disposable addDisposable Observable.fromCallable(sharedPreferenceHelper::authResponseDeadline)
            .observeOn(mainScheduler)
            .subscribe {
                if (it == 0L)
                    createMockAccount()
                else
                    onSuccess()
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        disposable.dispose()
        presenter.detachView(this)
        super.onDestroy()
    }

    override fun onSuccess() {
        completed++
        onComplete()
    }

    override fun onError(authError: AuthError) {
        binding.error.visibility = View.VISIBLE

        binding.progress.visibility = View.GONE
        binding.cardsContainer.visibility = View.GONE
    }

    override fun onLoading() {
        if (completed == 1) {
            binding.progress.visibility = View.VISIBLE
        }
    }

    private fun onComplete() {
        if (completed == 2) {
            analytics.onBoardingFinished()
            analytics.logAmplitudeEvent(AmplitudeAnalytics.Onboarding.COMPLETED)

            disposable addDisposable sharedPreferenceHelper.isFakeUser()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe { isFake ->
                    screenManager.startStudy()
                    if (isFake) {
                        screenManager.showEmptyAuthScreen(requireContext())
                    }
                }
        }
    }

    private fun initOnboardingCards() {
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_1, R.string.onboarding_card_question_1))
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_2, R.string.onboarding_card_question_2))
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_3, R.string.onboarding_card_question_3))
        adapter.add(createMockCard(Card.MOCK_LESSON_ID, R.string.onboarding_card_title_4, R.string.onboarding_card_question_4))
    }

    private fun createMockCard(id: Long, @StringRes title_id: Int, @StringRes question_id: Int): Card =
        Card(id, Lesson(getString(title_id)), Step(Block(getString(question_id))), Attempt(0, 0, datasetWrapper = DatasetWrapper(Dataset(listOf(), false))))

    private fun createMockAccount() {
        presenter.authFakeUser()
    }
}
