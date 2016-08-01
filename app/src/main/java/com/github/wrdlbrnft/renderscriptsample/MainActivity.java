package com.github.wrdlbrnft.renderscriptsample;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Type;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.example.android.basicrenderscript.R;
import com.example.android.basicrenderscript.databinding.ActivityMainBinding;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 31/07/16
 */
public class MainActivity extends AppCompatActivity implements OnSeekBarChangeListener {

    private static final float MAX_SATURATION = 2.0f;
    private static final float MIN_SATURATION = 0.0f;

    private ActivityMainBinding mBinding;
    private Allocation mInputAllocation;
    private Allocation mOutputAllocation;
    private Bitmap mOutputBitmap;
    private ScriptC_saturation mScript;

    private RenderScriptTask mCurrentTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Setting up the UI
        final Bitmap image = loadBitmap(R.drawable.test_image);

        mBinding.seekBar.setProgress(50);
        mBinding.seekBar.setOnSeekBarChangeListener(this);

        // Create the RenderScript instance
        final RenderScript renderScript = RenderScript.create(this);

        // Setup the input Allocation.
        // Since the input image never changes we can just create an Allocation now from the image
        // and never need to modify it again.
        mInputAllocation = Allocation.createFromBitmap(renderScript, image);

        // The output Allocation is created from a Type. The Type.Builder class can be used to
        // create a type based on an element. Since we are dealing with a 32 bit image we use the
        // RGBA_8888 element and then use setX() and setY() to set the width and height of the output
        // allocation to the same size as the input image.
        mOutputAllocation = Allocation.createTyped(renderScript, new Type.Builder(renderScript, Element.RGBA_8888(renderScript))
                .setX(image.getWidth())
                .setY(image.getHeight())
                .create());

        // We also need an output Bitmap. This Bitmap is where we copy the result from the
        // output allocation to. So we create an empty Bitmap with the same height as the input image.
        mOutputBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());

        // Then we give the Bitmap to the ImageView. Every time we copy new data to the Bitmap it
        // automatically invalidates the ImageView which draws the changes to the screen.
        mBinding.imageView.setImageBitmap(mOutputBitmap);

        // Finally we create an instance of the saturation script wrapper class.
        mScript = new ScriptC_saturation(renderScript);

        // Then we run the script once with our default saturation level of 1.0f.
        // updateImage() triggers the AsyncTask which executes the RenderScript Kernel
        updateImage(1.0f);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Here we calculate the saturation level from the SeekBar progress.
        float saturationLevel = (float) ((MAX_SATURATION - MIN_SATURATION) * (progress / 100.0) + MIN_SATURATION);

        // And call updateImage to invoke the RenderScript task.
        updateImage(saturationLevel);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private Bitmap loadBitmap(int resource) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }

    private void updateImage(float saturationLevel) {
        if (mCurrentTask != null) {
            // If a task is already running we need to cancel it.
            mCurrentTask.cancel(true);
        }

        // Execute the RenderScript task
        mCurrentTask = new RenderScriptTask(mBinding.imageView, mInputAllocation, mOutputAllocation, mOutputBitmap, mScript, saturationLevel);
        mCurrentTask.execute();
    }
}
