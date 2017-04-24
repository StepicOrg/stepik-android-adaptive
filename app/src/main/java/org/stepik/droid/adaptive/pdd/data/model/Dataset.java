package org.stepik.droid.adaptive.pdd.data.model;


import java.util.List;

public final class Dataset {

    private List<String> options;
    private boolean is_multiple_choice;

    public List<String> getOptions() {
        return options;
    }

    public boolean is_multiple_choice() {
        return is_multiple_choice;
    }
}
