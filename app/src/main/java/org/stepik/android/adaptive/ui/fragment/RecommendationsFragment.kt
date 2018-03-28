package org.stepik.android.adaptive.ui.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepik.android.adaptive.App
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.Util
import org.stepik.android.adaptive.configuration.RemoteConfig
import org.stepik.android.adaptive.content.questions.QuestionsPacksManager
import org.stepik.android.adaptive.core.ScreenManager
import org.stepik.android.adaptive.core.presenter.BasePresenterFragment
import org.stepik.android.adaptive.core.presenter.RecommendationsPresenter
import org.stepik.android.adaptive.core.presenter.contracts.RecommendationsView
import org.stepik.android.adaptive.data.Analytics
import org.stepik.android.adaptive.data.SharedPreferenceMgr
import org.stepik.android.adaptive.databinding.FragmentRecommendationsBinding
import org.stepik.android.adaptive.ui.activity.PaidInventoryItemsActivity
import org.stepik.android.adaptive.ui.adapter.QuizCardsAdapter
import org.stepik.android.adaptive.ui.animation.CardsFragmentAnimations
import org.stepik.android.adaptive.ui.dialog.DailyRewardDialog
import org.stepik.android.adaptive.ui.dialog.ExpLevelDialog
import org.stepik.android.adaptive.ui.dialog.QuestionsPacksDialog
import org.stepik.android.adaptive.ui.dialog.RateAppDialog
import org.stepik.android.adaptive.ui.helper.dpToPx
import org.stepik.android.adaptive.gamification.InventoryManager
import org.stepik.android.adaptive.util.PopupHelper
import org.stepik.android.adaptive.util.changeVisibillity
import javax.inject.Inject
import javax.inject.Provider

class RecommendationsFragment : BasePresenterFragment<RecommendationsPresenter, RecommendationsView>(), RecommendationsView {
    companion object {
        const val STREAK_RESTORE_REQUEST_CODE = 3423
        const val PAID_CONTENT_REQUEST_CODE = 113
        const val STREAK_RESTORE_KEY = "streak"

        private const val LEVEL_DIALOG_TAG = "level_dialog"
//        private const val STREAK_RESTORE_DIALOG_TAG = "streak_restore_dialog"
        private const val RATE_APP_DIALOG_TAG = "rate_app_dialog"
        private const val DAILY_REWARD_DIALOG_TAG = "daily_reward_dialog"
        private const val QUESTIONS_PACKS_DIALOG_TAG = "questions_packs_dialog"

        const val INVENTORY_DIALOG_TAG = "inventory_dialog"

        private val TOOLBAR_TOOLTIPS_OFF_Y_PX = dpToPx(6).toInt()
        private val TOOLBAR_TOOLTIPS_OFF_X_PX = dpToPx(-12).toInt()
    }

    private val loadingPlaceholders by lazy { resources.getStringArray(R.array.recommendation_loading_placeholders) }
    private val streakRestoreViewOffsetX = Resources.getSystem().displayMetrics.widthPixels.toFloat() / 4

    private var streakRestorePopup: PopupWindow? = null
    private var streakToRestore: Long? = null

    private var questionsPacksTooltip: PopupWindow? = null

    private val isQuestionsPackSupported by lazy { questionsPacksManager.isQuestionsPacksSupported }

    private lateinit var binding: FragmentRecommendationsBinding

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    @Inject
    lateinit var sharedPreferenceMgr: SharedPreferenceMgr

    @Inject
    lateinit var inventoryManager: InventoryManager

    @Inject
    lateinit var recommendationsPresenterProvider: Provider<RecommendationsPresenter>

    @Inject
    lateinit var questionsPacksManager: QuestionsPacksManager

    override fun injectComponent() {
        App.componentManager().studyComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecommendationsBinding.inflate(inflater, container, false)

        binding.tryAgain.setOnClickListener { presenter?.retry() }
        binding.courseCompletedText.movementMethod = LinkMovementMethod.getInstance()

        binding.streakSuccessContainer.nestedTextView = binding.streakSuccess
        binding.streakSuccessContainer.setGradientDrawableParams(ContextCompat.getColor(context, R.color.colorAccent), 0f)

        binding.toolbar.setOnClickListener { ScreenManager.showStatsScreen(context, analytics, 0) }

        savedInstanceState?.getLong(STREAK_RESTORE_KEY, -1)?.let {
            if (it != -1L) {
                showStreakRestoreDialog(it)
            }
        }

        binding.questionsPacks.changeVisibillity(isQuestionsPackSupported)
        binding.questionsPacks.setOnClickListener {
            questionsPacksTooltip?.dismiss()
            ScreenManager.showQuestionsPacksScreen(context, analytics)
        }

        return binding.root
    }

    private fun resolveQuestionsPackIcon() {
        @DimenRes val paddingRes: Int
        @DrawableRes val iconRes: Int
        if (remoteConfig.getBoolean(RemoteConfig.QUESTIONS_PACKS_ICON_EXPERIMENT)) {
            iconRes = questionsPacksManager.currentPack.icon // small icon of current pack
            paddingRes = R.dimen.action_bar_icon_padding_small

            val badgeCount = questionsPacksManager.unviewedPacksCount
            binding.questionsPacksBadge.text = badgeCount.toString()
            binding.questionsPacksBadge.changeVisibillity(badgeCount > 0 && isQuestionsPackSupported)
        } else {
            iconRes = R.drawable.ic_packs
            paddingRes = R.dimen.action_bar_icon_padding
        }
        val padding = resources.getDimensionPixelSize(paddingRes)
        binding.questionsPacks.setImageResource(iconRes)
        binding.questionsPacks.setPadding(padding, padding, padding, padding)
    }

    override fun onAdapter(cardsAdapter: QuizCardsAdapter) =
        binding.cardsContainer.setAdapter(cardsAdapter)


    override fun onLoading() {
        binding.progress.visibility = View.VISIBLE
        binding.error.visibility = View.GONE
        binding.loadingPlaceholder.text = loadingPlaceholders[Util.getRandomNumberBetween(0, 3)]
    }

    override fun onCardLoaded() {
        binding.progress.visibility = View.GONE
        binding.cardsContainer.visibility = View.VISIBLE
    }

    private fun onError() {
        binding.cardsContainer.visibility = View.GONE
        binding.error.visibility = View.VISIBLE
        binding.progress.visibility = View.GONE
    }

    override fun onConnectivityError() {
        binding.errorMessage.setText(R.string.connectivity_error)
        onError()
    }

    override fun onRequestError() {
        binding.errorMessage.setText(R.string.request_error)
        onError()
    }

    override fun onCourseCompleted() {
        binding.cardsContainer.visibility = View.GONE
        binding.progress.visibility = View.GONE
        binding.courseCompleted.visibility = View.VISIBLE
    }

    override fun updateExp(exp: Long,
                           currentLevelExp: Long,
                           nextLevelExp: Long,

                           level: Long) {

        binding.expProgress.progress = (exp - currentLevelExp).toInt()
        binding.expProgress.max = (nextLevelExp - currentLevelExp).toInt()

        binding.expCounter.text = exp.toString()
        binding.expLevel.text = getString(R.string.exp_title, level)
        binding.expLevelNext.text = getString(R.string.exp_subtitle, nextLevelExp - exp)
    }

    override fun onStreak(streak: Long) {
        binding.expInc.text = getString(R.string.exp_inc, streak)
        binding.streakSuccess.text = resources.getQuantityString(R.plurals.streak_success, streak.toInt(), streak)

        if (streak > 1) {
            CardsFragmentAnimations.playStreakSuccessAnimationSequence(binding)
        } else {
            CardsFragmentAnimations.playStreakBubbleAnimation(binding.expInc)
        }
    }

    override fun onStreakLost() =
            CardsFragmentAnimations.playStreakFailedAnimation(binding.streakFailed, binding.expProgress)

    override fun onStreakRestored() =
            CardsFragmentAnimations.playStreakRestoreAnimation(binding.streakSuccessContainer)

    override fun showDailyRewardDialog(progress: Long) =
            DailyRewardDialog.newInstance(progress).show(childFragmentManager, DAILY_REWARD_DIALOG_TAG)

    override fun showNewLevelDialog(level: Long) =
            ExpLevelDialog.newInstance(level).show(childFragmentManager, LEVEL_DIALOG_TAG)

    override fun showRateAppDialog() =
            RateAppDialog.newInstance().show(childFragmentManager, RATE_APP_DIALOG_TAG)

    override fun showStreakRestoreDialog(streak: Long, withTooltip: Boolean) {
        refreshStreakRestoreDialog()
        streakToRestore = streak
        CardsFragmentAnimations
                .createShowStreakRestoreWidgetAnimation(binding.ticketsContainer, streakRestoreViewOffsetX)
                .apply {
                    if (withTooltip) {
                        val tooltipText = getString(if (inventoryManager.hasTickets()) {
                            R.string.streak_restore_text
                        } else {
                            R.string.paid_content_tooltip
                        })
                        withEndAction {
                            streakRestorePopup = PopupHelper.showPopupAnchoredToView(context, binding.ticketsContainer, tooltipText)
                        }
                    }
                }
                .start()
        binding.ticketsContainer.setOnClickListener {
            if (inventoryManager.hasTickets()) {
                if (inventoryManager.useItem(InventoryManager.Item.Ticket)) {
                    presenter?.restoreStreak(streak)
                }
                hideStreakRestoreDialog()
            } else {
                openPaidContentList()
            }
        }
    }

    private fun refreshStreakRestoreDialog() {
        binding.ticketItem.counter.text = getString(R.string.amount, inventoryManager.getItemsCount(InventoryManager.Item.Ticket))
    }

    override fun showQuestionsPacksTooltip() {
        if (isQuestionsPackSupported) {
            if (remoteConfig.getBoolean(RemoteConfig.QUESTIONS_PACKS_DIALOG_EXPERIMENT)) {
                QuestionsPacksDialog.newInstance().show(childFragmentManager, QUESTIONS_PACKS_DIALOG_TAG)
            } else {
                questionsPacksTooltip = PopupHelper.showPopupAnchoredToView(
                        context, binding.questionsPacks, getString(R.string.questions_tooltip),
                        TOOLBAR_TOOLTIPS_OFF_X_PX, TOOLBAR_TOOLTIPS_OFF_Y_PX)
            }
        }
    }

    override fun hideStreakRestoreDialog() {
        streakToRestore = null
        if (streakRestorePopup?.isShowing == true) {
            streakRestorePopup?.dismiss()
        }
        CardsFragmentAnimations.playHideStreakRestoreWidgetAnimation(binding.ticketsContainer, streakRestoreViewOffsetX)
    }

    private fun openPaidContentList() {
        analytics.paidContentOpened()
        startActivityForResult(Intent(context, PaidInventoryItemsActivity::class.java), PAID_CONTENT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == STREAK_RESTORE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            presenter?.restoreStreak(data?.getLongExtra(STREAK_RESTORE_KEY, 0) ?: 0)
        }

        if (requestCode == PAID_CONTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshStreakRestoreDialog()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        streakToRestore?.let {
            outState?.putLong(STREAK_RESTORE_KEY, it)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        resolveQuestionsPackIcon() // here in order to sync changes
    }

    override fun onStart() {
        super.onStart()
        presenter?.attachView(this)
    }

    override fun onStop() {
        presenter?.detachView(this)
        super.onStop()
    }

    override fun getPresenterProvider() = recommendationsPresenterProvider
}