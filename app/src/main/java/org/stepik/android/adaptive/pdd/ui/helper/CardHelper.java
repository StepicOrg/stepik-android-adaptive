package org.stepik.android.adaptive.pdd.ui.helper;

import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.api.RecommendationsResponse;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;
import org.stepik.android.adaptive.pdd.databinding.QuizCardViewBinding;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class CardHelper {

    private final static int CARDS_IN_CACHE = 10;
    private final static int MIN_CARDS_IN_CACHE = 5;

    public static Observable<RecommendationsResponse> createReactionObservable(final long lesson, final RecommendationReaction.Reaction reaction, final int cacheSize) {
        final Observable<RecommendationsResponse> responseObservable = API.getInstance().getNextRecommendations(CARDS_IN_CACHE);

        if (lesson != 0) {
            final Completable reactionCompletable = API.getInstance()
                    .createReaction(new RecommendationReaction(lesson, reaction, SharedPreferenceMgr.getInstance().getProfileId()));
            if (cacheSize <= MIN_CARDS_IN_CACHE) {
                return reactionCompletable.andThen(responseObservable);
            } else {
                return reactionCompletable.toObservable();
            }
        }
        return responseObservable;
    }

    public static void resetSupplementalActions(final QuizCardViewBinding binding) {
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
}
