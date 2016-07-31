package com.github.wrdlbrnft.renderscriptsample;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
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
        mBinding.imageView.setImageBitmap(image);

        mBinding.seekBar.setProgress(50);
        mBinding.seekBar.setOnSeekBarChangeListener(this);

        // Create the RenderScript instance
        final RenderScript renderScript = RenderScript.create(this);

        // Setup the Allocations. For now we use the input image to create
        // both the input and output Allocations
        // Since our input image never changes in this example we never need to modify the input
        // Allocation again.
        mInputAllocation = Allocation.createFromBitmap(renderScript, image);
        mOutputAllocation = Allocation.createFromBitmap(renderScript, image);

        // We also need an output Bitmap. This Bitmap is where we copy the result from the
        // output allocation to. So we create an empty Bitmap with the same height as the input image.
        mOutputBitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());

        // Finally we create an instance of the saturation script wrapper class.
        mScript = new ScriptC_saturation(renderScript);

        updateImage(1.0f);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Here we calculate the saturation level from the SeekBar progress.
        float saturation = (float) ((MAX_SATURATION - MIN_SATURATION) * (progress / 100.0) + MIN_SATURATION);

        // And call updateImage to invoke the RenderScript task.
        updateImage(saturation);
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

    private void updateImage(float saturation) {
        if (mCurrentTask != null) {
            // If a task is already running we need to cancel it.
            mCurrentTask.cancel(true);
        }

        // Execute the RenderScript task
        mCurrentTask = new RenderScriptTask(mBinding.imageView, mInputAllocation, mOutputAllocation, mOutputBitmap, mScript, saturation);
        mCurrentTask.execute();
    }
}
