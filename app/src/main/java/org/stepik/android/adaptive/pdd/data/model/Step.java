package org.stepik.android.adaptive.pdd.data.model;

import java.util.List;

public final class Step {
    private long id;
    private long lesson;
    private long position;
    private String status;
    private Block block;
    // actions
    private String progress;
    private List<String> subscriptions;
    private long viewed_by;
    private long passed_by;
    private double correct_ratio;
    private long worth;
    private String create_date;
    private String update_date;

    private long discussions_count;
    private String discussion_proxy;
    private List<String> discussion_threads;

    public Block getBlock() {
        return block;
    }

    public long getId() {
        return id;
    }

    public long getLesson() {
        return lesson;
    }

    public Step(Block block) {
        this.block = block;
    }
}
