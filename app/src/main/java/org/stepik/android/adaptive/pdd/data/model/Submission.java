package org.stepik.android.adaptive.pdd.data.model;

import com.google.gson.annotations.SerializedName;

public final class Submission {
    public enum Status {

        @SerializedName("correct")
        CORRECT("correct"),

        @SerializedName("wrong")
        WRONG("wrong"),

        @SerializedName("evaluation")
        EVALUATION("evaluation"),

        @SerializedName("local")
        LOCAL("local");


        private final String scope;

        public String getScope() {
            return scope;
        }

        Status(String value) {
            this.scope = value;
        }

    }

    private long id;

    private Status status;
    private String score;
    private String hint;
    private String time;
    private Reply reply;
    private long attempt;
    private String session;
    private String eta;

    public Submission(final Reply reply, final long attempt) {
        this.reply = reply;
        this.attempt = attempt;
    }

    public long getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public long getAttempt() {
        return attempt;
    }
}
