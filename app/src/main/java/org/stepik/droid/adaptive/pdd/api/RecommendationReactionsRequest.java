package org.stepik.droid.adaptive.pdd.api;

import org.stepik.droid.adaptive.pdd.data.model.RecommendationReaction;


public final class RecommendationReactionsRequest {
    private RecommendationReaction recommendationReaction;

    public RecommendationReactionsRequest(final RecommendationReaction reaction) {
        this.recommendationReaction = reaction;
    }
}
