package yhh.com.gol.activity.main

import android.graphics.Bitmap
import android.view.MotionEvent
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import yhh.com.gol.activity.main.controller.v2.GameController2
import yhh.com.gol.activity.main.domain.State
import yhh.com.gol.activity.main.module.SharedPreferenceHelper
import yhh.com.gol.util.TestUtilities

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21], manifest = Config.NONE)
class MainActivityPresenterTest {

    @MockK
    internal lateinit var view: MainActivity

    @MockK
    internal lateinit var gameController2: GameController2

    @MockK
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private lateinit var presenter: MainActivityPresenter

    @Before
    fun setUp() {
        TestUtilities.initRxSchedulers()
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)

        presenter = spyk(MainActivityPresenter(view, gameController2, sharedPreferenceHelper))
    }

    @Test
    fun initView() {
        val intent = PublishSubject.create<Pair<Int, Int>>()
        every { view.tempViewLayoutIntent } returns intent
        presenter.create()
        val slot = slot<State>()
        every { view.render(capture(slot)) } just Runs

        intent.onNext(Pair(100, 200))

        assertTrue(slot.captured is State.InitGameView)
        assertEquals(100, (slot.captured as State.InitGameView).width)
        assertEquals(200, (slot.captured as State.InitGameView).height)
    }

    @Test
    fun createBoard() {
        val initIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.tempViewLayoutIntent } returns initIntent
        val layoutIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.gameViewLayoutIntent } returns layoutIntent
        presenter.create()
        initIntent.onNext(Pair(100, 200))

        layoutIntent.onNext(Pair(100, 200))

        verify { gameController2.createBoard(100, 200) }
    }

    @Test
    fun addNewPoint() {
        val initIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.tempViewLayoutIntent } returns initIntent
        val layoutIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.gameViewLayoutIntent } returns layoutIntent
        val touchIntent = PublishSubject.create<MotionEvent>()
        every { view.gameViewTouchIntent } returns touchIntent
        presenter.create()
        initIntent.onNext(Pair(100, 200))
        layoutIntent.onNext(Pair(100, 200))

        touchIntent.onNext(MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_MOVE, 100f, 100f, 0))

        verify {
            gameController2.addGridPoint(100, 100)
        }
    }

    @Test
    fun mergePoint() {
        val initIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.tempViewLayoutIntent } returns initIntent
        val layoutIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.gameViewLayoutIntent } returns layoutIntent
        val touchIntent = PublishSubject.create<MotionEvent>()
        every { view.gameViewTouchIntent } returns touchIntent
        presenter.create()
        initIntent.onNext(Pair(100, 200))
        layoutIntent.onNext(Pair(100, 200))

        touchIntent.onNext(MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_UP, 100f, 100f, 0))

        verify {
            gameController2.mergeGridPoints()
        }
    }

    @Test
    fun initSeekBar() {
        val initIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.tempViewLayoutIntent } returns initIntent
        val layoutIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.gameViewLayoutIntent } returns layoutIntent
        val touchIntent = PublishSubject.create<MotionEvent>()
        every { view.gameViewTouchIntent } returns touchIntent
        val slots = ArrayList<State>()
        every { view.render(capture(slots)) } just Runs
        every { sharedPreferenceHelper.getFrameRate() } returns 30
        every { sharedPreferenceHelper.getScaleValue() } returns 6

        presenter.create()
        initIntent.onNext(Pair(100, 200))
        layoutIntent.onNext(Pair(100, 200))

        assertTrue(slots.contains(State.UpdateDefaultValue(30, 6)))
    }

    @Test
    fun frameRateChanged() {
        val initIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.tempViewLayoutIntent } returns initIntent
        val layoutIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.gameViewLayoutIntent } returns layoutIntent
        val touchIntent = PublishSubject.create<MotionEvent>()
        every { view.gameViewTouchIntent } returns touchIntent
        val frameRateChangeIntent = PublishSubject.create<Int>()
        every { view.frameRateChangeIntent } returns frameRateChangeIntent

        presenter.create()
        initIntent.onNext(Pair(100, 200))
        layoutIntent.onNext(Pair(100, 200))
        frameRateChangeIntent.onNext(80)

        verify {
            gameController2.setFrameRate(80)
            sharedPreferenceHelper.setFrameRate(80)
        }
    }

    @Test
    fun scaleChanged() {
        val initIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.tempViewLayoutIntent } returns initIntent
        val layoutIntent = PublishSubject.create<Pair<Int, Int>>()
        every { view.gameViewLayoutIntent } returns layoutIntent
        val touchIntent = PublishSubject.create<MotionEvent>()
        every { view.gameViewTouchIntent } returns touchIntent
        val scaleChangeIntent = PublishSubject.create<Int>()
        every { view.scaleChangeIntent } returns scaleChangeIntent

        presenter.create()
        initIntent.onNext(Pair(100, 200))
        layoutIntent.onNext(Pair(100, 200))
        scaleChangeIntent.onNext(40)

        verify {
            sharedPreferenceHelper.setScaleValue(40 - MainActivity.BASE_SCALE.toInt())
        }
    }

    @Test
    fun updateView() {
        val intent = PublishSubject.create<Bitmap>()
        every { gameController2.updateIntent } returns intent
        presenter.create()
        val slot = slot<State>()
        every { view.render(capture(slot)) } just Runs

        val bitmap = mockk<Bitmap>()
        intent.onNext(bitmap)

        assertTrue(slot.captured is State.UpdateGameView)
        assertEquals(bitmap, (slot.captured as State.UpdateGameView).bitmap)

    }

    @Test
    fun controlPause() {
        val intent = PublishSubject.create<Unit>()
        every { view.controlIntent } returns intent
        presenter.create()
        presenter.isPause = false
        val slot = slot<State>()
        every { view.render(capture(slot)) } just Runs

        intent.onNext(Unit)

        verify { gameController2.pause() }

        assertEquals(true, presenter.isPause)
        assertTrue(slot.captured is State.SwitchToStart)
    }

    @Test
    fun controlStart() {
        val intent = PublishSubject.create<Unit>()
        every { view.controlIntent } returns intent
        presenter.create()
        presenter.isPause = true
        val slot = slot<State>()
        every { view.render(capture(slot)) } just Runs

        intent.onNext(Unit)

        verify { gameController2.resume() }

        assertEquals(false, presenter.isPause)
        assertTrue(slot.captured is State.SwitchToPause)
    }

    @Test
    fun pause() {
        val intent = PublishSubject.create<Unit>()
        every { view.onPauseIntent } returns intent
        presenter.create()

        intent.onNext(Unit)

        verify { gameController2.sleep() }
    }

    @Test
    fun resume() {
        val intent = PublishSubject.create<Unit>()
        every { view.onResumeIntent } returns intent
        presenter.create()

        intent.onNext(Unit)

        verify { gameController2.awake() }
    }

    @Test
    fun randomAdd() {
        val intent = PublishSubject.create<Unit>()
        every { view.randomAddIntent } returns intent
        presenter.create()

        intent.onNext(Unit)

        verify { gameController2.randomAdd() }
    }

    @Test
    fun clear() {
        val intent = PublishSubject.create<Unit>()
        every { view.clearIntent } returns intent
        presenter.create()

        intent.onNext(Unit)

        verify { gameController2.clear() }
    }

    @Test
    fun destroy() {
        presenter.destroy()

        verify { gameController2.finish() }
    }
}