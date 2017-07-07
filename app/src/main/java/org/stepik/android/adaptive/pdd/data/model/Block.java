package org.stepik.android.adaptive.pdd.data.model;

public final class Block {
    private String name;
    private String text;
//    private

    public Block(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}
