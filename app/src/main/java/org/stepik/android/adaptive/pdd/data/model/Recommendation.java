package org.stepik.android.adaptive.pdd.data.model;


import java.util.List;

public final class Recommendation {
    private long id;
    private long lesson;
    private List<String> reasons;

    public long getId() {
        return id;
    }

    public long getLesson() {
        return lesson;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
