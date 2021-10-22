package com.userfaltakas.sceneformdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.userfaltakas.sceneformdemo.Constants.BIG_VALUE
import com.userfaltakas.sceneformdemo.Constants.MAX_DISTANCE
import com.userfaltakas.sceneformdemo.Constants.MAX_ERROR_TOLERANCE
import com.userfaltakas.sceneformdemo.Constants.SMALL_VALUE
import com.userfaltakas.sceneformdemo.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var arFragment: ArFragment
    private val augmentedImageMap = HashMap<AugmentedImage, AugmentedImageNode>()
    private var imageExist = true
    private val sound = Sound(this@MainActivity)
    private var errorTolerance = 0
    private var startVector = Vector3(0f, 0f, 0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        arFragment = fragment as ArFragment

        arFragment.arSceneView.scene.addOnUpdateListener {
            val curFrame = arFragment.arSceneView.arFrame
            val curScene = arFragment.arSceneView.scene
            if (curFrame != null && curFrame.camera.trackingState == TrackingState.TRACKING) {
                updateTrackerImage(curFrame, curScene)
            }
        }
    }

    private fun updateTrackerImage(frame: Frame, scene: com.google.ar.sceneform.Scene) {
        val imageList = frame.getUpdatedTrackables(AugmentedImage::class.java)
        val worldPosition = frame.camera.pose

        imageExist = false

        for (image in imageList) {
            if (image.trackingState == TrackingState.TRACKING) {
                if (image.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING || image.trackingMethod == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE) {
                    errorTolerance = 0
                    imageExist = true

                    if (!augmentedImageMap.containsKey(image)) {
                        AugmentedImageNode(this).apply {
                            //sound.startMediaPlayer()
                            setAugmentedImage(image)
                            augmentedImageMap[image] = this
                            arFragment.arSceneView.scene.addChild(this)
                            startVector = scene.camera.worldPosition
                            startVector.z = 0f
                            startVector.x.times(1000000)
                            startVector.y.times(1000000)
                        }
                    } else {
                        AugmentedImageNode(this).apply {
                            if (!image.anchors.isEmpty()) {
//                              val objectPose = image.anchors.first().pose
                                val objectPose = this.localPosition

//                                val cameraPosition: Vector3 = scene.camera.worldPosition
//                                val catPosition: Vector3 = this.worldPosition
//                                val direction = Vector3.subtract(cameraPosition, catPosition)
//
//                                Log.d("direction", direction.toString())

                                val cameraPosition: Vector3 = scene.camera.worldPosition
                                cameraPosition.z = 0f
                                cameraPosition.x.times(1000000)
                                cameraPosition.y.times(1000000)

                                val a =
                                    Vector3.angleBetweenVectors(startVector, cameraPosition)

                                val dx: Float = objectPose.x - worldPosition.tx()
                                val dy: Float = objectPose.y - worldPosition.ty()
                                val dz: Float = objectPose.z - worldPosition.tz()


                                val distance = sqrt(dx * dx + dy * dy + dz * dz)
                                setSound(distance, dx)

                                setTextViews(
                                    image.name,
                                    "%.2f".format(distance) + "m",
                                    "%.2f".format(a)
                                )
                            }
                        }
                    }
                } else if (image.trackingMethod == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE) {
                    if (errorTolerance > MAX_ERROR_TOLERANCE) {
                        sound.pause()
                    } else {
                        errorTolerance++
                    }
                }
            } else if (image.trackingState == TrackingState.STOPPED) {
                augmentedImageMap.remove(image)
            }
        }

        if (!imageExist) {
            setTextViews("0.00", "0.00", "0.00")
        }
    }

    private fun setTextViews(imageName: String, distance: String, position: String) {
        binding.imageName.text = imageName
        binding.distanceTextView.text = distance
        binding.positionTextView.text = position
    }

    private fun setSound(distance: Float, dx: Float) {
        if (distance < MAX_DISTANCE) {
            sound.start()
            when {
                dx > 0 -> {
                    sound.changeVolume(
                        1 - distance - (dx / SMALL_VALUE),
                        1 - distance - (dx / BIG_VALUE)
                    )
                }
                dx < 0 -> {
                    sound.changeVolume(
                        (1 - distance) + (dx / BIG_VALUE),
                        1 - distance + (dx / SMALL_VALUE)
                    )
                }
                else -> {
                    sound.changeVolume(1 - distance, 1 - distance)
                }
            }

        } else if (distance > MAX_DISTANCE) {
            sound.pause()
        }
    }
}