package org.stepik.android.adaptive.pdd.ui.listener;

import org.stepik.android.adaptive.pdd.data.model.RecommendationReaction;

public interface AdaptiveReactionListener {
    void createReaction(long lessonId, RecommendationReaction.Reaction reaction);
}
