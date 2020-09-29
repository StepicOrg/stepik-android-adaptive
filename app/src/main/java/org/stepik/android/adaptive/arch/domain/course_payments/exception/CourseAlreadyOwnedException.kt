package org.stepik.android.adaptive.arch.domain.course_payments.exception

class CourseAlreadyOwnedException(courseId: Long) : Exception("Course $courseId already owned")
