package org.stepik.android.adaptive.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.adaptive.core.presenter.AuthPresenter
import org.stepik.android.adaptive.core.presenter.BookmarksPresenter
import org.stepik.android.adaptive.core.presenter.EditProfileFieldPresenter
import org.stepik.android.adaptive.core.presenter.PaidInventoryItemsPresenter
import org.stepik.android.adaptive.core.presenter.ProfilePresenter
import org.stepik.android.adaptive.core.presenter.ProgressPresenter
import org.stepik.android.adaptive.core.presenter.QuestionsPacksPresenter
import org.stepik.android.adaptive.core.presenter.RatingPresenter
import org.stepik.android.adaptive.core.presenter.RecommendationsPresenter
import org.stepik.android.adaptive.core.presenter.RegisterPresenter

@Module
abstract class PresenterModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProfilePresenter::class)
    internal abstract fun bindProfilePresenter(profilePresenter: ProfilePresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthPresenter::class)
    internal abstract fun bindAuthPresenter(auth: AuthPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BookmarksPresenter::class)
    internal abstract fun bindBookmarksPresenter(bookmarksPresenter: BookmarksPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileFieldPresenter::class)
    internal abstract fun bindEditProfileFieldPresenter(editProfileFieldPresenter: EditProfileFieldPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaidInventoryItemsPresenter::class)
    internal abstract fun bindPaidInventoryItemsPresenter(paidInventoryItemsPresenter: PaidInventoryItemsPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProgressPresenter::class)
    internal abstract fun bindProgressPresenter(progressPresenter: ProgressPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QuestionsPacksPresenter::class)
    internal abstract fun bindQuestionPacksPresenter(questionsPacksPresenter: QuestionsPacksPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RatingPresenter::class)
    internal abstract fun bindRatingPresenter(ratingPresenter: RatingPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecommendationsPresenter::class)
    internal abstract fun bindRecommendationsPresenter(recommendationsPresenter: RecommendationsPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegisterPresenter::class)
    internal abstract fun bindRegisterPresenter(registerPresenter: RegisterPresenter): ViewModel
}
