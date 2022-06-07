package com.asyncdevs.mercury

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.asyncdevs.mercury.ml.Model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.w3c.dom.Text

import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    var selectedPhotoUri : Uri? = null
    var bitmap : Bitmap?= null
    var byteBuffer : ByteBuffer?= null
    var tensorImage : TensorImage?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<Button>(R.id.btn_upload).setOnClickListener {
            findViewById<TextView>(R.id.tv_result).text = "The result of your report will appear here"
            Toast.makeText(this, "Select photo", Toast.LENGTH_SHORT).show()
            val i = Intent(Intent.ACTION_PICK)
            i.type= "image/*"
            startActivityForResult(i, 0)
        }

        findViewById<Button>(R.id.btn_predict).setOnClickListener {
            var arrayList = ArrayList<Float>()
            val model = Model.newInstance(this)
            val outputs = model.process(tensorImage!!.tensorBuffer)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            var values = outputFeature0.floatArray
            for(va in values){
                Log.i("SRI1711",va.toString() )
                arrayList.add(va)
            }
            val max = outputFeature0.floatArray.maxOrNull() ?:0
            val index = values.indexOf(max as Float)
            Log.i("SRI1711",index.toString())

            if(index == 0){
                findViewById<TextView>(R.id.tv_result).text = "Meningioma \n" +
                        "A usually non-cancerous tumour that arises from the membranes surrounding the brain and spinal cord. In most cases, the condition is non-cancerous."
            }
            else if(index == 1){
                findViewById<TextView>(R.id.tv_result).text = "Glioma \n" +
                        " SYMPTOMS : headaches, seizures, irritability, vomiting, visual difficulties and weakness or numbness of the extremities."
            }
            else if(index == 2){
                findViewById<TextView>(R.id.tv_result).text = "Pitutary\n" +
                        "Non-cancerous tumours in the pituitary gland that don't spread beyond the skull.\n" +
                        "The pituitary gland is in the skull, below the brain and above the nasal passages. A large tumour can press upon and damage the brain and nerves."
            }
            model.close()
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode== Activity.RESULT_OK && data!=null){
            Log.i("info", "Photo Selected")
            selectedPhotoUri = data.data
            Log.e("SRI1711", "$selectedPhotoUri")
            //val ibtn_profile = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.ibtn_profile)
            bitmap = MediaStore.Images.Media.getBitmap(
                contentResolver,
                selectedPhotoUri
            )
            converttoByteBuffer(bitmap)
        }
    }

    private fun converttoByteBuffer(bitmap: Bitmap?) {
        val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(128, 128, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()
        tensorImage = TensorImage(DataType.UINT8)
        tensorImage!!.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

    }
}