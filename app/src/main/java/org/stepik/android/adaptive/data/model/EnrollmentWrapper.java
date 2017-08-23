package org.stepik.android.adaptive.data.model;

public class EnrollmentWrapper {
    private Enrollment enrollment;

    public EnrollmentWrapper(long courseId) {
        enrollment = new Enrollment(courseId);
    }
}