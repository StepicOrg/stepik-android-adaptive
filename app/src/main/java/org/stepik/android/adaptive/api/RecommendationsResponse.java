package org.stepik.android.adaptive.api;


import org.stepik.android.adaptive.data.model.Recommendation;

import java.util.List;

public class RecommendationsResponse {
    private List<Recommendation> recommendations;

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public Recommendation getFirstRecommendation() {
        if (recommendations != null && recommendations.size() > 0) {
            return recommendations.get(0);
        } else {
            return null;
        }
    }
}
