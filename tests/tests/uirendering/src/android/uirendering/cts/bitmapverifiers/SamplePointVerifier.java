/*
 * Copyright (C) 2014 The Android Open Source Project
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
package android.uirendering.cts.bitmapverifiers;

import android.graphics.Point;
import android.util.Log;

/**
 * This class will test specific points, and ensure that they match up perfectly with the input colors
 */
public class SamplePointVerifier extends BitmapVerifier {
    private static final String TAG = "SamplePoint";
    private Point[] mTestPoints;
    private int[] mExpectedColors;

    public SamplePointVerifier(Point[] testPoints, int[] expectedColors) {
        mTestPoints = testPoints;
        mExpectedColors = expectedColors;
    }

    @Override
    public boolean verify(int[] bitmap, int offset, int stride, int width, int height) {
        for (int i = 0 ; i < mTestPoints.length ; i++) {
            int x = mTestPoints[i].x;
            int y = mTestPoints[i].y;
            int index = indexFromXAndY(x, y, stride, offset);
            if (mExpectedColors[i] != bitmap[index]) {
                Log.d(TAG, "Expected : " + Integer.toHexString(mExpectedColors[i]) +
                        " at position x = " + x + " y = " + y + " , tested color : " +
                        Integer.toHexString(bitmap[index]));
                return false;
            }
        }
        return true;
    }
}
