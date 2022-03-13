package com.userfaltakas.sceneformdemo

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.userfaltakas.sceneformdemo.Constants.DIV_VALUE
import com.userfaltakas.sceneformdemo.Constants.MAX_DISTANCE
import com.userfaltakas.sceneformdemo.Constants.MAX_POSITION
import com.userfaltakas.sceneformdemo.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var arFragment: ArFragment
    private val augmentedImageMap = HashMap<AugmentedImage, AugmentedImageNode>()
    private var imageExist = true
    private val sound = Sound(this@MainActivity)
    private var firstVector = Vector3(0f, 0f, 0f)
    private var secondVector = Vector3(0f, 0f, 0f)
    private var functionality = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        arFragment = fragment as ArFragment
        binding.settings.setOnClickListener {
            setDialog().show()
        }

        arFragment.arSceneView.scene.addOnUpdateListener {
            val curFrame = arFragment.arSceneView.arFrame
            if (curFrame != null && curFrame.camera.trackingState == TrackingState.TRACKING) {
                updateTrackerImage(curFrame)
            }
        }
    }

    private fun updateTrackerImage(frame: Frame) {
        val imageList = frame.getUpdatedTrackables(AugmentedImage::class.java)
        val worldPosition = frame.camera.pose

        imageExist = false

        for (image in imageList) {
            if (image.trackingState == TrackingState.TRACKING) {
                if (image.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING || image.trackingMethod == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE) {
                    imageExist = true

                    if (!augmentedImageMap.containsKey(image)) {
                        AugmentedImageNode(this).apply {
                            sound.startMediaPlayer()
                            setAugmentedImage(image)
                            augmentedImageMap[image] = this
                            arFragment.arSceneView.scene.addChild(this)
                            val objectPose = this.localPosition
                            firstVector.x = objectPose.x - worldPosition.tx()
                            firstVector.y = objectPose.y - worldPosition.ty()
                            firstVector.z = 0f
                        }
                    } else {
                        AugmentedImageNode(this).apply {
                            if (!image.anchors.isEmpty()) {
                                val objectPose = this.localPosition
                                secondVector.x = objectPose.x - worldPosition.tx()
                                secondVector.y = objectPose.y - worldPosition.ty()
                                secondVector.z = 0f

                                val position =
                                    Vector3.angleBetweenVectors(firstVector, secondVector)

                                val isLeft = firstVector.x < secondVector.x

                                secondVector.z = objectPose.z - worldPosition.tz()

                                setSound(getDistance(secondVector), position, isLeft)
                                setTextViews(
                                    "%.2f".format(getDistance(secondVector)) + "m",
                                    "%.2f".format(position),
                                    isLeft
                                )
                            }
                        }
                    }
                }
            } else if (image.trackingState == TrackingState.STOPPED) {
                augmentedImageMap.remove(image)
            }
        }

        if (!imageExist) {
            setTextViews("0.00", "0.00", null)
        }
    }

    private fun setTextViews(distance: String, position: String, isLeft: Boolean?) {
        binding.distanceTextView.text = distance
        binding.positionTextView.text = position
        when (isLeft) {
            true -> {
                binding.imageName.text = "<-"
            }
            false -> {
                binding.imageName.text = "->"
            }
            else -> {
                binding.imageName.text = "none"
            }
        }

    }

    private fun getDistance(vector: Vector3): Float {
        return sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z)
    }

    private fun setSound(distance: Float, position: Float, isLeft: Boolean) {
        if (functionality == 1) {
            if (distance <= MAX_DISTANCE && position < MAX_POSITION) {
                sound.start()
                when (isLeft) {
                    true -> {
                        sound.changeVolume(
                            1 - distance - position.div(DIV_VALUE),
                            1 - distance + position.div(DIV_VALUE)
                        )
                    }
                    false -> {
                        sound.changeVolume(
                            1 - distance + position.div(DIV_VALUE),
                            1 - distance - position.div(DIV_VALUE)
                        )
                    }
                }
            } else {
                sound.pause()
            }
        } else if (functionality == 0) {
            if (distance <= MAX_DISTANCE) {
                sound.start()
                sound.changeVolume(1 - distance - 0.2f, 1 - distance - 0.2f)
            } else {
                sound.pause()
            }
        }
    }

    private fun setDialog(): AlertDialog {
        val options = arrayOf("Distance only", "Distance + rotation")
        return AlertDialog.Builder(this)
            .setTitle("Functionality")
            .setSingleChoiceItems(options, functionality) { _, i ->
                functionality = i
            }.setPositiveButton("OK") { _, _ ->
                if (functionality == 0) {
                    binding.imageName.visibility = android.view.View.GONE
                    binding.rotateImage.visibility = android.view.View.GONE
                    binding.positionTextView.visibility = android.view.View.GONE
                } else {
                    binding.imageName.visibility = android.view.View.VISIBLE
                    binding.rotateImage.visibility = android.view.View.VISIBLE
                    binding.positionTextView.visibility = android.view.View.VISIBLE
                }
            }.create()
    }

}