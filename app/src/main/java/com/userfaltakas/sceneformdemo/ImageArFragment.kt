package com.userfaltakas.sceneformdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException

class ImageArFragment : ArFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
    }


    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        config.focusMode = Config.FocusMode.AUTO
        config.augmentedImageDatabase = createAugmentedImageDatabase(session ?: return config)
        return config
    }


    private fun createAugmentedImageDatabase(session: Session): AugmentedImageDatabase? {
        return try {
            val inputStream = resources.openRawResource(R.raw.my_image_database)
            AugmentedImageDatabase.deserialize(session, inputStream)
        } catch (e: IOException) {
            Log.e("Exception", "error", e)
            null
        }
    }
}



