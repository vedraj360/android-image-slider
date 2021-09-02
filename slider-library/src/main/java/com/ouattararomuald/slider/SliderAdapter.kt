package com.ouattararomuald.slider

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import com.ouattararomuald.CustomViewPager
import java.util.*

/**
 * Adapter to create a slider.
 *
 * The slider will have the given number of slides.
 *
 * @property context Context.
 * @property imageLoaderFactory image loader factory.
 * @property imageUrls urls of images to display.
 * @property descriptions images descriptions.
 * @property sliderId ID of the slider.
 * @param sliderId ID of the slider.
 */
class SliderAdapter(
    private val context: Context,
    private val imageLoaderFactory: ImageLoader.Factory<*>,
    val imageUrls: List<String>,
    val descriptions: List<String> = emptyList(),
    sliderId: String? = null,
    val enableZoom: Boolean = false,
    val enableRotationView: Boolean = false
) : PagerAdapter() {

    val sliderId: String = sliderId ?: UUID.randomUUID().toString()
    private val imageLoader: ImageLoader

    private lateinit var slideImageView: PhotoView
    private lateinit var slideImageViewNoZoom: ImageView
    private lateinit var descriptionLayout: LinearLayout
    private lateinit var descriptionTextView: AppCompatTextView
    private lateinit var imageRotateLeft: ImageButton
    private lateinit var imageRotateRight: ImageButton
    private lateinit var rotationLayout: LinearLayout

    private var imageClickListener: ImageViewClickListener? = null

    private var rotationAngles = mutableListOf<Float>()

    init {
        if (imageUrls.isEmpty()) {
            throw IllegalArgumentException("imagesUrls.size < 0")
        }
        if (descriptions.isNotEmpty() && descriptions.size != imageUrls.size) {
            throw IllegalArgumentException("Descriptions.size != imagesUrls.size")
        }
        imageLoader = imageLoaderFactory.create()

        rotationAngles = Array(imageUrls.size) { 0F }.toMutableList()
    }

    /**
     * Determines whether this adapter has attached description or not.
     *
     * @return true if it has descriptions. Otherwise returns false.
     */
    internal val hasDescriptions: Boolean
        get() = descriptions.isNotEmpty()

    override fun getCount(): Int = imageUrls.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.slide_item_view, container, false)
        view.tag = imageUrls[position]
        view.apply {
            slideImageView = findViewById(R.id.image)
            rotationLayout = findViewById(R.id.rotation_layout)
            slideImageViewNoZoom = findViewById(R.id.image_nozoom)
            imageRotateLeft = findViewById(R.id.image_rotate_left)
            imageRotateRight = findViewById(R.id.image_rotate_right)
            rotationLayout.isVisible = enableRotationView
            if (enableRotationView) {
                var rotationAngle = rotationAngles[position]
                imageRotateLeft.setOnClickListener {
                    rotationAngle -= 90.0.toFloat()
                    if (rotationAngle == - 90f) {
                        rotationAngle = 270f
                    }
                    rotationAngles[position] = rotationAngle
                    imageClickListener?.onRotateLeft(
                        imageUrls[position],
                        position,
                        rotationAngles[position],
                        imageUrls[position]
                    )
                }
                imageRotateRight.setOnClickListener {
                    rotationAngle += 90.0.toFloat()
                    if (rotationAngle == 360f) {
                        rotationAngle = 0f
                    }
                    rotationAngles[position] = rotationAngle
                    imageClickListener?.onRotateRightClicked(
                        imageUrls[position],
                        position,
                        rotationAngles[position],
                        imageUrls[position]
                    )
                }
            }
            if (enableZoom) {
                slideImageView.isVisible = true
                slideImageViewNoZoom.isVisible = false
                slideImageView.setOnClickListener {
                    imageClickListener?.onItemClicked(
                        imageUrls[position],
                        position,
                        imageUrls[position]
                    )
                }
                imageLoader.configureImageView(slideImageView)
                imageLoader.load(imageUrls[position], slideImageView)
            } else {
                slideImageViewNoZoom.isVisible = true
                slideImageView.isVisible = false
                slideImageViewNoZoom.setOnClickListener {
                    imageClickListener?.onItemClicked(
                        sliderId,
                        position,
                        imageUrls[position]
                    )
                }
                imageLoader.configureImageView(slideImageViewNoZoom)
                imageLoader.load(imageUrls[position], slideImageViewNoZoom)

            }
            if (enableRotationView) {
                slideImageView.rotation = rotationAngles[position]
            }
            descriptionLayout = findViewById(R.id.description_layout)
            descriptionTextView = findViewById(R.id.description_textview)

        }

        if (descriptions.isNotEmpty()) {
            descriptionTextView.text = descriptions[position]
        }

        descriptionLayout.isVisible = descriptions.isNotEmpty()

        val viewPager = container as CustomViewPager
        viewPager.addView(view, 0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val viewPager = container as ViewPager
        val view = obj as View
        viewPager.removeView(view)
    }

    fun setImageClickListener(listener: ImageViewClickListener?) {
        imageClickListener = listener
    }

    interface ImageViewClickListener {
        /**
         * Invoked after a click on an item in the slider
         *
         * @param sliderId ID of the slider.
         * @param position position of item that was clicked.
         * @param imageUrl url of the image<x.
         */
        fun onItemClicked(sliderId: String, position: Int, imageUrl: String)
        fun onRotateRightClicked(
            sliderId: String,
            position: Int,
            rotationAngle: Float,
            imageUrl: String
        )

        fun onRotateLeft(sliderId: String, position: Int, rotationAngle: Float, imageUrl: String)

    }
}
