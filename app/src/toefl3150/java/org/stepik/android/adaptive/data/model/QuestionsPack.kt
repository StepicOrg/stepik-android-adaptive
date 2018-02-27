package org.stepik.android.adaptive.data.model

enum class QuestionsPack(val id: String, val courseId: Long, val isFree: Boolean = false) {
    Basic("questions_pack_basic", 3150, isFree = true),
    Medium("questions_pack_medium", 6243),
    Pro("questions_pack_pro", 6312),
    Full("questions_pack_full", 6315);

    companion object {
        fun getById(id: String) = values().find { it.id == id }
    }
}