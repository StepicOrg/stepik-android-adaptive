package org.stepik.android.adaptive.api;

import org.stepik.android.adaptive.data.model.RecommendationReaction;


public final class RecommendationReactionsRequest {
    private RecommendationReaction recommendationReaction;

    public RecommendationReactionsRequest(final RecommendationReaction reaction) {
        this.recommendationReaction = reaction;
    }
}
