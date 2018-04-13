package org.stepik.android.adaptive.core.presenter

import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.stepik.android.adaptive.api.profile.ProfileRepository
import org.stepik.android.adaptive.core.presenter.contracts.RegisterView
import org.stepik.android.adaptive.data.model.AccountCredentials
import org.stepik.android.adaptive.data.model.Profile
import org.stepik.android.adaptive.data.preference.ProfilePreferences
import java.net.SocketException

@RunWith(RobolectricTestRunner::class)
@Config(manifest= Config.NONE)
class RegisterPresenterTest {

    private lateinit var testScheduler: TestScheduler

    private lateinit var profileRepository: ProfileRepository
    private lateinit var profilePreferences: ProfilePreferences

    private lateinit var registerView: RegisterView

    private lateinit var newCredentials: AccountCredentials
    private lateinit var oldCredentials: AccountCredentials

    @Before
    fun prepare() {
        registerView = mock()
        profileRepository = mock()
        profilePreferences = mock()
        testScheduler = TestScheduler()

        newCredentials = AccountCredentials("admin@stepik.org", "123", "123", "123")
        oldCredentials = AccountCredentials("admin@stepik.org", "1234", "123", "123")
    }

    @Test
    fun successfulRegistrationTest() {
        val profile = Profile(id = 123)
        whenever(profileRepository.fetchProfile()) doReturn Single.just(profile)
        whenever(profileRepository.updateProfile(any())) doReturn Completable.complete()
        whenever(profileRepository.updateEmail(any())) doReturn Completable.complete()
        whenever(profileRepository.updatePassword(any(), any(), any())) doReturn Completable.complete()

        whenever(profilePreferences.fakeUser) doReturn oldCredentials

        val presenter = RegisterPresenter(profileRepository, profilePreferences, testScheduler, testScheduler)
        presenter.attachView(registerView)
        verify(registerView).setState(RegisterView.State.Idle)

        presenter.register(newCredentials.firstName, newCredentials.lastName, newCredentials.login, newCredentials.password)
        testScheduler.triggerActions()

        verify(registerView).setState(RegisterView.State.Loading)
        verify(profileRepository).fetchProfile()
        verify(profileRepository).updateProfile(argThat {
            this.firstName == newCredentials.firstName && this.lastName == newCredentials.lastName
        })
        verify(profilePreferences)::profile.set(profile)
        verify(profileRepository).updateEmail(newCredentials.login)
        verify(profilePreferences)::fakeUser.get()
        verify(profileRepository).updatePassword(profile.id, oldCredentials.password, newCredentials.password)
        verify(profilePreferences).removeFakeUser()

        verify(registerView).setState(RegisterView.State.Success)

        verifyNoMoreInteractions(registerView)
        verifyNoMoreInteractions(profileRepository)
        verifyNoMoreInteractions(profilePreferences)
    }

    @Test
    fun failOnProfileFetch() {
        whenever(profileRepository.fetchProfile()) doReturn Single.error(SocketException())

        val presenter = RegisterPresenter(profileRepository, profilePreferences, testScheduler, testScheduler)
        presenter.attachView(registerView)
        verify(registerView).setState(RegisterView.State.Idle)

        presenter.register(newCredentials.firstName, newCredentials.lastName, newCredentials.login, newCredentials.password)
        testScheduler.triggerActions()
        verify(registerView).setState(RegisterView.State.Loading)

        verify(profileRepository).fetchProfile()
        verify(registerView).setState(RegisterView.State.NetworkError)

        verifyNoMoreInteractions(registerView)
        verifyNoMoreInteractions(profileRepository)
        verifyNoMoreInteractions(profilePreferences)
    }

    @Test
    fun failOnProfileUpdate() {
        val profile = Profile(id = 123)
        whenever(profileRepository.fetchProfile()) doReturn Single.just(profile)
        whenever(profileRepository.updateProfile(any())) doReturn Completable.error(SocketException())

        val presenter = RegisterPresenter(profileRepository, profilePreferences, testScheduler, testScheduler)
        presenter.attachView(registerView)
        verify(registerView).setState(RegisterView.State.Idle)

        presenter.register(newCredentials.firstName, newCredentials.lastName, newCredentials.login, newCredentials.password)
        testScheduler.triggerActions()
        verify(registerView).setState(RegisterView.State.Loading)

        verify(profileRepository).fetchProfile()
        verify(profileRepository).updateProfile(argThat {
            this.firstName == newCredentials.firstName && this.lastName == newCredentials.lastName
        })
        verify(registerView).setState(RegisterView.State.NetworkError)

        verifyNoMoreInteractions(registerView)
        verifyNoMoreInteractions(profileRepository)
        verifyNoMoreInteractions(profilePreferences)
    }

    @Test
    fun failOnEmailUpdate() {
        val profile = Profile(id = 123)
        whenever(profileRepository.fetchProfile()) doReturn Single.just(profile)
        whenever(profileRepository.updateProfile(any())) doReturn Completable.complete()
        whenever(profileRepository.updateEmail(any()))doReturn Completable.error(SocketException())

        val presenter = RegisterPresenter(profileRepository, profilePreferences, testScheduler, testScheduler)
        presenter.attachView(registerView)
        verify(registerView).setState(RegisterView.State.Idle)

        presenter.register(newCredentials.firstName, newCredentials.lastName, newCredentials.login, newCredentials.password)
        testScheduler.triggerActions()
        verify(registerView).setState(RegisterView.State.Loading)

        verify(profileRepository).fetchProfile()
        verify(profileRepository).updateProfile(argThat {
            this.firstName == newCredentials.firstName && this.lastName == newCredentials.lastName
        })
        verify(profilePreferences)::profile.set(profile)
        verify(profileRepository).updateEmail(newCredentials.login)
        verify(registerView).setState(RegisterView.State.NetworkError)

        verifyNoMoreInteractions(registerView)
        verifyNoMoreInteractions(profileRepository)
        verifyNoMoreInteractions(profilePreferences)
    }

    @Test
    fun failOnPasswordUpdate() {
        val profile = Profile(id = 123)
        whenever(profileRepository.fetchProfile()) doReturn Single.just(profile)
        whenever(profileRepository.updateProfile(any())) doReturn Completable.complete()
        whenever(profileRepository.updateEmail(any())) doReturn Completable.complete()
        whenever(profileRepository.updatePassword(any(), any(), any())) doReturn Completable.error(SocketException())

        whenever(profilePreferences.fakeUser) doReturn oldCredentials

        val presenter = RegisterPresenter(profileRepository, profilePreferences, testScheduler, testScheduler)
        presenter.attachView(registerView)
        verify(registerView).setState(RegisterView.State.Idle)

        presenter.register(newCredentials.firstName, newCredentials.lastName, newCredentials.login, newCredentials.password)
        testScheduler.triggerActions()
        verify(registerView).setState(RegisterView.State.Loading)

        verify(profileRepository).fetchProfile()
        verify(profileRepository).updateProfile(argThat {
            this.firstName == newCredentials.firstName && this.lastName == newCredentials.lastName
        })
        verify(profilePreferences)::profile.set(profile)
        verify(profileRepository).updateEmail(newCredentials.login)
        verify(profilePreferences)::fakeUser.get()
        verify(profileRepository).updatePassword(profile.id, oldCredentials.password, newCredentials.password)
        verify(registerView).setState(RegisterView.State.NetworkError)

        verifyNoMoreInteractions(registerView)
        verifyNoMoreInteractions(profileRepository)
        verifyNoMoreInteractions(profilePreferences)
    }
}