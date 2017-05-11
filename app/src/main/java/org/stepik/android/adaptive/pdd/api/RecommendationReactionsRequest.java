package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;


public final class RecommendationReactionsRequest {
    private RecommendationReaction recommendationReaction;

    public RecommendationReactionsRequest(final RecommendationReaction reaction) {
        this.recommendationReaction = reaction;
    }
}
