package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;

import java.util.List;

/**
 * Created by ruslandavletshin on 01/04/2017.
 */

public final class RecommendationReactionsResponse {
    private List<RecommendationReaction> recommendationReactions;

    public List<RecommendationReaction> getRecommendationReactions() {
        return recommendationReactions;
    }
}
