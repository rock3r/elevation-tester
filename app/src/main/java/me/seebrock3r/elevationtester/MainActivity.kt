package me.seebrock3r.elevationtester

import android.graphics.Rect
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionManager
import android.view.MotionEvent
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main_collapsed.*
import kotlinx.android.synthetic.main.include_controls_collapsed.*
import kotlinx.android.synthetic.main.include_header_collapsed.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var outlineProvider: TweakableOutlineProvider
    private var buttonVerticalMarginPixel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_collapsed)

        outlineProvider = TweakableOutlineProvider(resources = resources, scaleX = 1f, scaleY = 1f, yShift = 0)
        button.outlineProvider = outlineProvider

        setupPanelHeaderControls()
        setupElevationControls()
        setupScaleXYControls()
        setupYShiftControls()

        setupDragYToMove()
    }

    private var panelExpanded = false

    private fun setupPanelHeaderControls() {
        panelHeader.setOnClickListener {
            if (panelExpanded) collapsePanel() else expandPanel()
            panelExpanded = !panelExpanded
        }
    }

    private fun collapsePanel(animate: Boolean = true) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootContainer)
        }
        
        ConstraintSet().apply {
            clone(this@MainActivity, R.layout.activity_main_collapsed)
        }.applyTo(rootContainer)
        expandCollapseImage.isChecked = false
        button.text = getString(R.string.drag_up_and_down)
    }

    private fun expandPanel() {
        TransitionManager.beginDelayedTransition(rootContainer)
        ConstraintSet().apply {
            clone(this@MainActivity, R.layout.activity_main_expanded)
        }.applyTo(rootContainer)
        expandCollapseImage.isChecked = true
        button.text = getString(R.string.use_controls_below)
    }

    private fun setupElevationControls() {
        elevationBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        setElevation(progress)
                    }
                }
        )
        elevationValue.text = getString(R.string.elevation_value, 0)
    }

    private fun setElevation(progress: Int) {
        button.elevation = progress * resources.displayMetrics.density
        elevationValue.text = getString(R.string.elevation_value, progress)
    }

    private fun setupScaleXYControls() {
        xScaleBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        setScaleX(progress)
                    }
                }
        )

        yScaleBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        setScaleY(progress)
                    }
                }
        )

        setScaleX(0)
        xScaleValue.setOnClickListener { xScaleBar.progress = xScaleBar.max / 2 }
        yScaleValue.text = getString(R.string.y_scale_value, 0)
        yScaleBar.progress = yScaleBar.max / 2
        xScaleBar.progress = xScaleBar.max / 2
    }

    private fun setScaleX(progress: Int) {
        val scale = progress - xScaleBar.max / 2
        outlineProvider.scaleX = 1 + scale / 100f
        button.invalidateOutline()
        xScaleValue.text = getString(R.string.x_scale_value, scale + 100)
    }

    private fun setScaleY(progress: Int) {
        val scale = progress - yScaleBar.max / 2
        outlineProvider.scaleY = 1 + scale / 100f
        button.invalidateOutline()
        yScaleValue.text = getString(R.string.y_scale_value, scale + 100)
    }

    private fun setupYShiftControls() {
        yShiftBar.setOnSeekBarChangeListener(
                object : BetterSeekListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        setShiftY(progress)
                    }
                }
        )
        yShiftValue.text = getString(R.string.y_shift_value, 0)
        yShiftBar.progress = yShiftBar.max / 2
    }

    private fun setShiftY(progress: Int) {
        val shift = progress - yShiftBar.max / 2
        outlineProvider.yShift = shift
        button.invalidateOutline()
        yShiftValue.text = getString(R.string.y_shift_value, shift)
    }

    private fun setupDragYToMove() {
        buttonVerticalMarginPixel = resources.getDimensionPixelSize(R.dimen.button_vertical_margin)

        rootContainer.setOnTouchListener { _, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> handleActionDown(motionEvent)
                MotionEvent.ACTION_MOVE -> handleDrag(motionEvent)
                else -> false
            }
        }
    }

    private fun handleActionDown(motionEvent: MotionEvent): Boolean {
        if (panelExpanded) {
            return false    // Only draggable when the panel is collapsed
        }

        val hitRect = Rect()
        button.getHitRect(hitRect)
        return hitRect.contains(motionEvent.getX(0).roundToInt(), motionEvent.getY(0).roundToInt())
    }

    private fun handleDrag(motionEvent: MotionEvent): Boolean {
        val availableHeight = panelHeader.y
        val clampedEventY = motionEvent.getY(0)
            .roundToInt()
            .coerceIn(buttonVerticalMarginPixel, availableHeight.toInt() - buttonVerticalMarginPixel)

        val layoutParams = button.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.verticalBias = (clampedEventY - button.height / 2) / availableHeight
        button.layoutParams = layoutParams
        return true
    }

}

