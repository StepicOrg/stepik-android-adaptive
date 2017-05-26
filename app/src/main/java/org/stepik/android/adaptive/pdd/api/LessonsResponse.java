package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.Lesson;

import java.util.List;

public class LessonsResponse {
    private List<Lesson> lessons;

    public Lesson getFirstLesson() {
        if (lessons != null && lessons.size() > 0) {
            return lessons.get(0);
        } else {
            return null;
        }
    }
}
