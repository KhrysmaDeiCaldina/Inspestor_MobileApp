package com.thesis.inspestor_mobileapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.thesis.inspestor_mobileapp.databinding.ActivityMainBinding
import com.thesis.inspestor_mobileapp.ml.TestTrainMetadata
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;
    private lateinit var imageView: ImageView;
    private lateinit var button: Button;
    private lateinit var tvOutput: TextView;
    private lateinit var textView: TextView;
    private val GALLERY_REQUEST_CODE = 123;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN )

        imageView = binding.previewImage
        button = binding.cameraBtn
        textView = binding.activeResult
        tvOutput = binding.pestResult
        val buttonLoad = binding.galleryBtn

        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePicturePreview.launch(null)
            }
            else {
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        buttonLoad.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            }
            else {
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            takePicturePreview.launch(null)
        }
        else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            outputGenerator(bitmap)
        }
    }

    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result->
        Log.i("TAG", "this is the result: ${result.data} ${result.resultCode}")
        onResultRecieved(GALLERY_REQUEST_CODE, result)
    }

    private fun onResultRecieved(requestCode: Int, result: ActivityResult?){
        when(requestCode){
            GALLERY_REQUEST_CODE ->{
                if (result?.resultCode == Activity.RESULT_OK){
                    result.data?.data?.let{uri ->
                        Log.i("Tag", "onResultRecieved: $uri")
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                    }
                }
                else {
                    Log.e("Tag", "onActivityResult: error in selecting image")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun outputGenerator(bitmap: Bitmap){
        val TestTrainModel = TestTrainMetadata.newInstance(this)

        // Creates inputs for reference.
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfimage = TensorImage.fromBitmap(newBitmap)

        // Runs model inference and gets result.
        val outputs = TestTrainModel.process(tfimage).detectionResultList.apply {
            sortByDescending {
                it.scoreAsFloat
            }
        }

        val detectionResult = outputs[0]

        if (detectionResult.scoreAsFloat <= .50){
            tvOutput.text = "No pest detected"
            textView.text = " "
        } else {
            tvOutput.text = detectionResult.categoryAsString + " " + detectionResult.scoreAsFloat
            Log.i("Tag", "outputGenerator: $detectionResult")
            if (detectionResult.categoryAsString == "Rice Grain Bug"){
                textView.text = "LAMBDA-CYHALOTHRIN 25 g/L" + "\n" + "N CYPERMETHRIN 50g/L" + "\n" + "DIAZINON 600 g/L"
            }
            else if (detectionResult.categoryAsString == "Rice Bug"){
                textView.text = "LAMBDA-CYHALOTHRIN 25 g/L" + "\n" + "DIAZINON 600 g/L" + "\n" + "N CYPERMETHRIN 50g/L"
            }
            else if (detectionResult.categoryAsString == "Brown Planthopper"){
                textView.text = "PHENTHOATE 500 g/L" + "\n" + "PHENTHOATE+BPMC 250 g/L" + "\n" + "CYPERMETHRIN 50g/L"
            }
            else {
                textView.text = " "
            }
        }


        /*else if (detectionResult.scoreAsFloat <= .25){
            textView.text = " "
        }*/

        //textView.text = detectionResult.categoryAsString

        // Releases model resources if no longer used.
        TestTrainModel.close()

        /*
        val cardsModel = Detect.newInstance(this)

        // Creates inputs for reference.
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfimage = TensorImage.fromBitmap(newBitmap)

        // Runs model inference and gets result.
        val outputs = cardsModel.process(tfimage)
            .detectionResultList.apply {
                sortByDescending { it.scoreAsFloat }
            }
        val detectionResult = outputs[0]

        tvOutput.text = detectionResult.categoryAsString
        Log.i("Tag", "outputGenerator: $detectionResult")

        */
    }
}