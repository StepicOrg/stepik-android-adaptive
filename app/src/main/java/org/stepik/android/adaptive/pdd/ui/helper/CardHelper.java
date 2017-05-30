package org.stepik.android.adaptive.pdd.ui.helper;

import android.content.res.Resources;
import android.view.View;
import android.widget.ScrollView;

import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.api.RecommendationsResponse;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.databinding.FragmentRecommendationsBinding;
import org.stepik.android.adaptive.pdd.ui.view.QuizCardView;

import io.reactivex.Observable;

public class CardHelper {

    private final static int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    public static Observable<RecommendationsResponse> createReactionObservable(final long lesson, final RecommendationReaction.Reaction reaction) {
        final Observable<RecommendationsResponse> responseObservable = API.getInstance().getNextRecommendations();

        if (lesson != 0) {
            return API.getInstance()
                    .createReaction(new RecommendationReaction(lesson, reaction, SharedPreferenceMgr.getInstance().getProfileId()))
                    .andThen(responseObservable);
        }
        return responseObservable;
    }

    public static void resetCard(final FragmentRecommendationsBinding binding) {
        binding.fragmentRecommendationsContainer.setEnabled(true);
        binding.fragmentRecommendationsContainer.setTranslationX(0);
        binding.fragmentRecommendationsContainer.setTranslationY(-screenHeight);

        resetSupplementalActions(binding);
    }

    public static void resetSupplementalActions(final FragmentRecommendationsBinding binding) {
        binding.fragmentRecommendationsNext.setVisibility(View.GONE);
        binding.fragmentRecommendationsCorrect.setVisibility(View.GONE);
        binding.fragmentRecommendationsWrong.setVisibility(View.GONE);
        binding.fragmentRecommendationsWrongRetry.setVisibility(View.GONE);
        binding.fragmentRecommendationsAnswersProgress.setVisibility(View.GONE);
        binding.fragmentRecommendationsHint.setVisibility(View.GONE);
        binding.fragmentRecommendationsSubmit.setVisibility(View.VISIBLE);
    }

    public static void scrollDown(final ScrollView view) {
        view.post(() -> view.fullScroll(View.FOCUS_DOWN));
    }

    public static void showCard(final QuizCardView view) {
        view.animate()
                .setStartDelay(AnimationHelper.ANIMATION_DURATION)
                .translationX(0)
                .translationY(0)
                .setDuration(AnimationHelper.ANIMATION_DURATION)
                .setInterpolator(AnimationHelper.OvershootInterpolator2F).start();
    }

}
