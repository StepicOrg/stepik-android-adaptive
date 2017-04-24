package org.stepik.droid.adaptive.pdd.data.model;


public final class RecommendationReaction {

    public enum Reaction {
        SOLVED(2), INTERESTING(1), MAYBE_LATER(0), NEVER_AGAIN(-1);
        private final int value;

        private Reaction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private long lesson;
    private long user;
    private int reaction;

    public RecommendationReaction(final long lesson, final Reaction reaction, final long user) {
        this.lesson = lesson;
        this.reaction = reaction.getValue();
        this.user = user;
    }

    public RecommendationReaction(final long lesson, final Reaction reaction) {
        this(lesson, reaction, 0);
    }

    public void setUser(final long user) {
        this.user = user;
    }

    public long getLesson() {
        return lesson;
    }
}
