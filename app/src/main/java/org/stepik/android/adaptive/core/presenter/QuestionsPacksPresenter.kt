package org.stepik.android.adaptive.core.presenter

class QuestionsPacksPresenter
// @Inject
// constructor(
//    private val api: Api,
//    private val analytics: Analytics,
//    @BackgroundScheduler
//    private val backgroundScheduler: Scheduler,
//    @MainScheduler
//    private val mainScheduler: Scheduler,

//    private val questionsPacksManager: QuestionsPacksManager,
//    private val questionsPacksResolver: QuestionsPacksResolver,
//
//    private val remoteStorageRepository: RemoteStorageRepository,
//    private val sharedPreferenceHelper: SharedPreferenceHelper,
//
//    billing: Billing
// ) : PaidContentPresenterBase<QuestionsPacksView>(billing) {
//    private val adapter = QuestionsPacksAdapter(this::onPackPressed, questionsPacksResolver)
//    private val skus = questionsPacksManager.ids
//    private var isPacksLoaded = false
//
//    private val compositeDisposable = CompositeDisposable()
//
//    init {
//        adapter.selection = questionsPacksManager.currentPackIndex
//    }
//
//    fun loadContent() {
//        view?.showContentProgress()
//        compositeDisposable += getInventoryRx(ProductTypes.IN_APP, skus)
//            .subscribeOn(mainScheduler)
//            .map {
//                it.map { sku ->
//                    sku to questionsPacksManager.getPackById(sku.id.code)!!
//                }
//            }
//            .flatMap { packs ->
//                val ids = packs.map { it.second.courseId }.toLongArray()
//                packs.forEach {
//                    questionsPacksManager.onQuestionsPackViewed(it.second)
//                }
//
//                api.getCourses(ids)
//                    .map { it.courses }
//                    .map { courses ->
//                        courses.mapNotNull { course ->
//                            val pack = packs.find { it.second.courseId == course.id }
//                            pack?.second?.size = course.totalUnits
//                            pack
//                        }
//                    }
//                    .subscribeOn(backgroundScheduler)
//            }
//            .observeOn(mainScheduler)
//            .subscribeBy(
//                onSuccess = {
//                    adapter.items = it
//                    view?.hideContentProgress()
//                    isPacksLoaded = true
//                    restorePurchases()
//                },
//                onError = {
//                    if (it is PurchasesNotSupportedException) {
//                        view?.onPurchasesNotSupported()
//                    } else {
//                        view?.onContentError()
//                    }
//                }
//            )
//    }
//
//    fun restorePurchases() {
//        view?.showProgress()
//        compositeDisposable += consume(getAllPurchases())
//    }
//
//    private fun consume(observable: Observable<Purchase>) =
//        consumeIfNotFake(
//            observable.subscribeOn(mainScheduler).filter {
//                skus.contains(it.sku)
//            }
//        )
//            .observeOn(mainScheduler)
//            .subscribeBy(
//                onSuccess = {
//                    adapter.addOwnedContent(it)
//                    view?.hideProgress()
//                },
//                onError = {
//                    if (it !is BillingException || it.response != ResponseCodes.USER_CANCELED && it.response != ResponseCodes.BILLING_UNAVAILABLE) {
//                        view?.onPurchaseError()
//                    }
//                    view?.hideProgress()
//                }
//            )
//
//    private fun consumeIfNotFake(observable: Observable<Purchase>) =
//        sharedPreferenceHelper.isFakeUser().flatMap { isFake ->
//            if (isFake) {
//                observable.map { it.sku }.toList()
//            } else {
//                observable.flatMapCompletable { saveToRemoteStorageAndThenConsume(it) } then
//                    remoteStorageRepository.getQuestionsPacks().subscribeOn(backgroundScheduler)
//            }
//        }
//
//    private fun saveToRemoteStorageAndThenConsume(purchase: Purchase): Completable =
//        remoteStorageRepository
//            .storeQuestionsPack(purchase.sku)
//            .subscribeOn(backgroundScheduler) then
//            checkout?.onReady()
//                ?.flatMapCompletable { it.consumeRx(purchase.token) }
//                ?.subscribeOn(mainScheduler)
//
//    private fun onPackPressed(sku: Sku, pack: QuestionsPack, isOwned: Boolean) {
//        if (isOwned || questionsPacksResolver.isAvailableForFree(pack)) {
//            changeCourse(pack)
//        } else {
//            purchase(sku, pack)
//        }
//    }
//
//    private fun purchase(sku: Sku, pack: QuestionsPack) {
//        analytics.logEvent(Analytics.EVENT_ON_QUESTIONS_PACK_PURCHASE_BUTTON_CLICKED)
//        val purchaseObservable = checkout?.startPurchaseFlowRx(sku)?.doOnNext {
//            analytics.logAmplitudePurchase(
//                AmplitudeAnalytics.QuestionPacks.PACK_PURCHASED, sku,
//                mapOf(
//                    AmplitudeAnalytics.QuestionPacks.PARAM_PACK_ID to pack.courseId,
//                    AmplitudeAnalytics.QuestionPacks.PARAM_PACK_NAME to pack.id
//                )
//            )
//        } ?: Observable.empty<Purchase>()
//        compositeDisposable addDisposable consume(purchaseObservable.flatMap { getAllPurchases() })
//    }
//
//    private fun changeCourse(pack: QuestionsPack) {
//        view?.showProgress()
//        compositeDisposable addDisposable api.joinCourse(pack.courseId).doOnComplete {
//            questionsPacksManager.switchPack(pack)
//        }
//            .observeOn(mainScheduler)
//            .subscribeOn(backgroundScheduler)
//            .subscribe(
//                {
//                    adapter.selection = pack.ordinal
//                    view?.hideProgress()
//                },
//                {
//                    view?.hideProgress()
//                }
//            )
//    }
//
//    override fun attachView(view: QuestionsPacksView) {
//        super.attachView(view)
//
//        view.onAdapter(adapter)
//
//        if (isPacksLoaded) {
//            view.hideContentProgress()
//        } else {
//            loadContent()
//        }
//    }
//
//    override fun destroy() {
//        compositeDisposable.dispose()
//    }
// }
