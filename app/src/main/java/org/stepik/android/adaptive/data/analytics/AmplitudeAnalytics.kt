package org.stepik.android.adaptive.data.analytics

object AmplitudeAnalytics {

    object Properties {
        const val APPLICATION_ID = "application_id"
    }

    object Launch {
        const val FIRST_TIME = "Launch first time"
        const val SESSION_START = "Session start"
    }

    object Onboarding {
        const val SCREEN_OPENED = "Onboarding screen opened"
        const val COMPLETED = "Onboarding completed"

        const val PARAM_SCREEN = "screen"
    }

    object Auth {
        const val AUTH_POPUP_OPENED = "Auth screen popup opened"
        const val AUTH_SKIPED = "Sign in not now pressed"

        const val LOGGED_ID = "Logged in"
        const val REGISTERED = "Registered"

        const val PARAM_SOURCE = "source"
        const val VALUE_SOURCE_EMAIL = "email"
    }

    object Stats {
        const val SCREEN_OPENED = "Stats screen opened"

        const val PARAM_SCREEN = "screen"
        object ScreenValues {
            const val PROFILE = "profile"
            const val PROGRESS = "progress"
            const val ACHIEVEMENTS = "achievements"
            const val BOOKMARKS = "bookmarks"
            const val RATING = "rating"
        }
    }

    object Submissions {
        const val SUBMISSION_CREATED = "Submission created"

        const val PARAM_PACK_ID = "pack_id"
        const val PARAM_PACK_NAME = "pack_name"

        const val REACTION_PERFORMED = "Reaction performed"

        const val PARAM_COMPLEXITY = "complexity"
        object ComplexityValues {
            const val EASY = "easy"
            const val HARD = "hard"
        }
    }

    object GamificationDescription {
        const val SCREEN_OPENED = "GamificationDescriptionScreen popup opened"
        const val MY_STATS_CLICKED = "GamificationDescriptionScreen popup my stats pressed"
        const val QUESTIONS_PACKS_CLICKED = "GamificationDescriptionScreen popup question packs pressed"
    }

    object QuestionPacks {
        const val SCREEN_OPENED = "Question packs screen opened"
        const val POPUP_OPENED = "Question packs popup opened"
        const val POPUP_ACTION_PRESSED = "Question packs popup take a look pressed"

        const val PACK_PURCHASED = "Question pack purchased"

        const val PARAM_PACK_ID = "pack_id"
        const val PARAM_PACK_NAME = "pack_name"
    }

    object Tickets {
        const val PURCHASE_POPUP_SHOWN = "Ticket purchase popup shown"
        const val BANNER_SHOWN = "Ticket banner shown"
        const val TICKETS_PURCHASED = "Ticket purchased"

        const val PARAM_TICKETS_COUNT = "tickets_count"
    }
}