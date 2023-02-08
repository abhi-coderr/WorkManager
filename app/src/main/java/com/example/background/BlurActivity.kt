/**
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.example.background.databinding.ActivityBlurBinding

class BlurActivity : AppCompatActivity() {

    private val viewModel: BlurViewModel by viewModels {
        BlurViewModel.BlurViewModelFactory(
            application
        )
    }

    private var yesGoToBlur = false

    private lateinit var binding: ActivityBlurBinding

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.seeFileButton.setOnClickListener {
            viewModel.outputUri?.let { currentUri ->
//                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
//                actionView.resolveActivity(packageManager)?.run {
//                    startActivity(actionView)
//                }
                binding.imageView.setImageURI(currentUri)

            }
        }

//        binding.goButton.setOnClickListener {
//            Log.d("abhi->>>>>>","hey this is click event")
//            viewModel.applyBlur(blurLevel)
//        }

        binding.radioBlurGroup.setOnCheckedChangeListener { radioGroup, i ->

            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_original -> {
                    Toast.makeText(this, "remove blur", Toast.LENGTH_SHORT).show()
                    binding.imageView.setImageResource(R.drawable.android_cupcake)
                }
                R.id.radio_blur_lv_1 -> {
                    viewModel.applyBlur(1)
                    viewModel.outputWorkInfo.observe(this, workInfosObserver())
                    Toast.makeText(this, "1 level", Toast.LENGTH_SHORT).show()
                }
                R.id.radio_blur_lv_2 -> {
                    viewModel.applyBlur(2)
                    viewModel.outputWorkInfo.observe(this, workInfosObserver())
                    Toast.makeText(this, "2 level", Toast.LENGTH_SHORT).show()
                }
                R.id.radio_blur_lv_3 -> {
                    viewModel.applyBlur(3)
                    viewModel.outputWorkInfo.observe(this, workInfosObserver())
                    Toast.makeText(this, "3 level", Toast.LENGTH_SHORT).show()
                }
                else -> binding.imageView.setImageResource(R.drawable.android_cupcake)
            }

        }

    }

    // Define the observer function
    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showWorkFinished()

                // Normally this processing, which is not directly related to drawing views on
                // screen would be in the ViewModel. For simplicity we are keeping it here.
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                // If there is an output file show "See File" button
                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    viewModel.outputUri?.let { currentUri ->
                        binding.imageView.setImageURI(currentUri)
                    }
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.GONE
        }
    }

    private val blurLevel: Int
        get() =
            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }
}
