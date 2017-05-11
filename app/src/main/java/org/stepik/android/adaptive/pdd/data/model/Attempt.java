package org.stepik.android.adaptive.pdd.data.model;

public final class Attempt {
    private long id;
    private long step;
    private Dataset dataset;
    private String dataset_url;
    private String time;
    private String status;
    private String time_left;
    private long user;

    public Attempt(final long step) {
        this.step = step;
    }

    public long getStep() {
        return step;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public long getId() {
        return id;
    }
}
