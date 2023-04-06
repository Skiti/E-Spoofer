package u.scooters.attack.main

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class SpecialTextView : AppCompatTextView {
    var type: RequestType? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, type: RequestType?) : super(
        context!!, attrs
    ) {
        this.type = type
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        type: RequestType?
    ) : super(
        context!!, attrs, defStyleAttr
    ) {
        this.type = type
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}