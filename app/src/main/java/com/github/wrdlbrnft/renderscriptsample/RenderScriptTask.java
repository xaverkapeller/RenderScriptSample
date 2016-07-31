package com.github.wrdlbrnft.renderscriptsample;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 31/07/16
 */
class RenderScriptTask extends AsyncTask<Void, Void, Bitmap> {

    private final WeakReference<ImageView> mOutputViewReference;

    private final Allocation mInputAllocation;
    private final Allocation mOutputAllocation;
    private final Bitmap mOutputBitmap;
    private final ScriptC_saturation mScript;
    private final float mSaturationValue;

    RenderScriptTask(ImageView outputView, Allocation inputAllocation, Allocation outputAllocation, Bitmap outputBitmap, ScriptC_saturation script, float saturationValue) {
        mOutputViewReference = new WeakReference<>(outputView);
        mInputAllocation = inputAllocation;
        mOutputAllocation = outputAllocation;
        mOutputBitmap = outputBitmap;
        mScript = script;
        mSaturationValue = saturationValue;
    }

    protected Bitmap doInBackground(Void... values) {
        if (isCancelled()) {
            return null;
        }

        // First we set the saturation level on the Script
        mScript.set_saturationValue(mSaturationValue);

        // Now we invoke our defined Kernel
        mScript.forEach_saturation(mInputAllocation, mOutputAllocation);

        // After the Kernel has finished we copy the results to the output Bitmap and return it
        mOutputAllocation.copyTo(mOutputBitmap);

        return mOutputBitmap;
    }

    protected void onPostExecute(Bitmap result) {
        if (result == null) {
            // If result is null then Task was canceled.
            // Therefore don't do anything.
            return;
        }

        final ImageView outputView = mOutputViewReference.get();
        if (outputView == null) {
            // If the output View is null then the Activity has already been closed.
            // Therefore don't do anything.
            return;
        }

        // Otherwise set the result image to the output View
        outputView.setImageBitmap(result);
    }
}
