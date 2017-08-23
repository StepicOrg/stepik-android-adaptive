package org.stepik.android.adaptive.ui.listener;

import org.stepik.android.adaptive.data.model.RecommendationReaction;

public interface AdaptiveReactionListener {
    void createReaction(long lessonId, RecommendationReaction.Reaction reaction);
}
