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

// Don't edit this file!  It is auto-generated by frameworks/rs/api/gen_runtime.

package android.renderscript.cts;

import android.renderscript.Allocation;
import android.renderscript.RSRuntimeException;
import android.renderscript.Element;

public class TestNativeHypot extends RSBaseCompute {

    private ScriptC_TestNativeHypot script;
    private ScriptC_TestNativeHypotRelaxed scriptRelaxed;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        script = new ScriptC_TestNativeHypot(mRS);
        scriptRelaxed = new ScriptC_TestNativeHypotRelaxed(mRS);
    }

    private void checkNativeHypotFloatFloatFloat() {
        Allocation inX = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 1, 0x3d61f129bdf66018l, false);
        Allocation inY = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 1, 0x3d61f129bdf66019l, false);
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 1), INPUTSIZE);
            script.set_gAllocInY(inY);
            script.forEach_testNativeHypotFloatFloatFloat(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloatFloatFloat: " + e.toString());
        }
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 1), INPUTSIZE);
            scriptRelaxed.set_gAllocInY(inY);
            scriptRelaxed.forEach_testNativeHypotFloatFloatFloat(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloatFloatFloat: " + e.toString());
        }
    }

    private void checkNativeHypotFloat2Float2Float2() {
        Allocation inX = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 2, 0x92a8064760a50e64l, false);
        Allocation inY = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 2, 0x92a8064760a50e65l, false);
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 2), INPUTSIZE);
            script.set_gAllocInY(inY);
            script.forEach_testNativeHypotFloat2Float2Float2(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloat2Float2Float2: " + e.toString());
        }
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 2), INPUTSIZE);
            scriptRelaxed.set_gAllocInY(inY);
            scriptRelaxed.forEach_testNativeHypotFloat2Float2Float2(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloat2Float2Float2: " + e.toString());
        }
    }

    private void checkNativeHypotFloat3Float3Float3() {
        Allocation inX = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 3, 0xe70ce46762831005l, false);
        Allocation inY = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 3, 0xe70ce46762831006l, false);
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 3), INPUTSIZE);
            script.set_gAllocInY(inY);
            script.forEach_testNativeHypotFloat3Float3Float3(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloat3Float3Float3: " + e.toString());
        }
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 3), INPUTSIZE);
            scriptRelaxed.set_gAllocInY(inY);
            scriptRelaxed.forEach_testNativeHypotFloat3Float3Float3(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloat3Float3Float3: " + e.toString());
        }
    }

    private void checkNativeHypotFloat4Float4Float4() {
        Allocation inX = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 4, 0x3b71c287646111a6l, false);
        Allocation inY = createRandomAllocation(mRS, Element.DataType.FLOAT_32, 4, 0x3b71c287646111a7l, false);
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 4), INPUTSIZE);
            script.set_gAllocInY(inY);
            script.forEach_testNativeHypotFloat4Float4Float4(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloat4Float4Float4: " + e.toString());
        }
        try {
            Allocation out = Allocation.createSized(mRS, getElement(mRS, Element.DataType.FLOAT_32, 4), INPUTSIZE);
            scriptRelaxed.set_gAllocInY(inY);
            scriptRelaxed.forEach_testNativeHypotFloat4Float4Float4(inX, out);
        } catch (Exception e) {
            throw new RSRuntimeException("RenderScript. Can't invoke forEach_testNativeHypotFloat4Float4Float4: " + e.toString());
        }
    }

    public void testNativeHypot() {
        checkNativeHypotFloatFloatFloat();
        checkNativeHypotFloat2Float2Float2();
        checkNativeHypotFloat3Float3Float3();
        checkNativeHypotFloat4Float4Float4();
    }
}
