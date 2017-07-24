package org.stepik.android.adaptive.pdd.data.model;

import java.util.List;

public class Unit {
    private List<Long> assignments;
    private long id;

    public long getTopAssignment() {
        if (assignments != null && assignments.size() > 0)
            return assignments.get(0);
        else
            return 0;
    }
}
