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

package android.print.cts;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentAdapter.LayoutResultCallback;
import android.print.PrintDocumentAdapter.WriteResultCallback;
import android.print.PrintManager;
import android.print.PrinterId;
import android.print.cts.services.PrintServiceCallbacks;
import android.print.cts.services.PrinterDiscoverySessionCallbacks;
import android.print.cts.services.StubbablePrinterDiscoverySession;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.support.test.uiautomator.UiAutomatorTestCase;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.DisplayMetrics;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * This is the base class for print tests.
 */
public abstract class BasePrintTest extends UiAutomatorTestCase {

    private static final long OPERATION_TIMEOUT = 10000;

    private static final String ARG_PRIVILEGED_OPS = "ARG_PRIVILEGED_OPS";

    private static final String PRINT_SPOOLER_PACKAGE_NAME = "com.android.printspooler";

    protected static final String PRINT_JOB_NAME = "Test";

    private PrintDocumentAdapterContractActivity mActivity;

    private Locale mOldLocale;

    private CallCounter mLayoutCallCounter;
    private CallCounter mWriteCallCounter;
    private CallCounter mFinishCallCounter;
    private CallCounter mPrintJobQueuedCallCounter;

    @Override
    public void setUp() throws Exception {
        // Make sure we start with a clean slate.
        clearPrintSpoolerData();

        // Workaround for dexmaker bug: https://code.google.com/p/dexmaker/issues/detail?id=2
        // Dexmaker is used by mockito.
        System.setProperty("dexmaker.dexcache", getInstrumentation()
                .getTargetContext().getCacheDir().getPath());

        // Set to US locale.
        Resources resources = getInstrumentation().getTargetContext().getResources();
        Configuration oldConfiguration = resources.getConfiguration();
        if (!oldConfiguration.locale.equals(Locale.US)) {
            mOldLocale = oldConfiguration.locale;
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            Configuration newConfiguration = new Configuration(oldConfiguration);
            newConfiguration.locale = Locale.US;
            resources.updateConfiguration(newConfiguration, displayMetrics);
        }

        // Initialize the latches.
        mLayoutCallCounter = new CallCounter();
        mFinishCallCounter = new CallCounter();
        mWriteCallCounter = new CallCounter();
        mFinishCallCounter = new CallCounter();
        mPrintJobQueuedCallCounter = new CallCounter();

        // Create the activity for the right locale.
        createActivity();
    }

    @Override
    public void tearDown() throws Exception {
        // Done with the activity.
        getActivity().finish();

        // Restore the locale if needed.
        if (mOldLocale != null) {
            Resources resources = getInstrumentation().getTargetContext().getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            Configuration newConfiguration = new Configuration(resources.getConfiguration());
            newConfiguration.locale = mOldLocale;
            mOldLocale = null;
            resources.updateConfiguration(newConfiguration, displayMetrics);
        }

        // Make sure the spooler is cleaned.
        clearPrintSpoolerData();
    }

    protected void print(final PrintDocumentAdapter adapter) {
        // Initiate printing as if coming from the app.
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                PrintManager printManager = (PrintManager) getActivity()
                        .getSystemService(Context.PRINT_SERVICE);
                printManager.print("Print job", adapter, null);
            }
        });
    }

    protected void onLayoutCalled() {
        mLayoutCallCounter.call();
    }

    protected void onWriteCalled() {
        mWriteCallCounter.call();
    }

    protected void onFinishCalled() {
        mFinishCallCounter.call();
    }

    protected void onPrintJobQueuedCalled() {
        mPrintJobQueuedCallCounter.call();
    }

    protected void waitForServiceOnPrintJobQueuedCallbackCalled() {
        waitForCallbackCallCount(mPrintJobQueuedCallCounter, 1,
                "Did not get expected call to onPrintJobQueued.");
    }

    protected void waitForAdapterCallbackFinish() {
        waitForCallbackCallCount(mFinishCallCounter, 1,
                "Did not get expected call to finish.");
    }

    protected void waitForLayoutAdapterCallbackCount(int count) {
        waitForCallbackCallCount(mLayoutCallCounter, count,
                "Did not get expected call to layout.");
    }

    protected void waitForWriteForAdapterCallback() {
        waitForCallbackCallCount(mWriteCallCounter, 1, "Did not get expected call to write.");
    }

    private void waitForCallbackCallCount(CallCounter counter, int count, String message) {
        try {
            counter.waitForCount(count, OPERATION_TIMEOUT);
        } catch (TimeoutException te) {
            fail(message);
        }
    }

    protected void selectPrinter(String printerName) throws UiObjectNotFoundException {
        UiObject destinationSpinner = new UiObject(new UiSelector().resourceId(
                "com.android.printspooler:id/destination_spinner"));
        destinationSpinner.click();
        UiObject printerOption = new UiObject(new UiSelector().text(printerName));
        printerOption.click();
    }

    protected void changeOrientation(String orientation) throws UiObjectNotFoundException {
        UiObject orientationSpinner = new UiObject(new UiSelector().resourceId(
                "com.android.printspooler:id/orientation_spinner"));
        orientationSpinner.click();
        UiObject orientationOption = new UiObject(new UiSelector().text(orientation));
        orientationOption.click();
    }

    protected void changeMediaSize(String mediaSize) throws UiObjectNotFoundException {
        UiObject mediaSizeSpinner = new UiObject(new UiSelector().resourceId(
                "com.android.printspooler:id/paper_size_spinner"));
        mediaSizeSpinner.click();
        UiObject mediaSizeOption = new UiObject(new UiSelector().text(mediaSize));
        mediaSizeOption.click();
    }

    protected void changeColor(String color) throws UiObjectNotFoundException {
        UiObject colorSpinner = new UiObject(new UiSelector().resourceId(
                "com.android.printspooler:id/color_spinner"));
        colorSpinner.click();
        UiObject colorOption = new UiObject(new UiSelector().text(color));
        colorOption.click();
    }

    protected void clickPrintButton() throws UiObjectNotFoundException {
        UiObject printButton = new UiObject(new UiSelector().resourceId(
                "com.android.printspooler:id/print_button"));
        printButton.click();
    }

    protected PrintDocumentAdapterContractActivity getActivity() {
        return mActivity;
    }

    private void createActivity() {
        mActivity = launchActivity(
                getInstrumentation().getTargetContext().getPackageName(),
                PrintDocumentAdapterContractActivity.class, null);
    }

    protected void clearPrintSpoolerData() throws Exception {
        IPrivilegedOperations privilegedOps = IPrivilegedOperations.Stub.asInterface(
                getParams().getBinder(ARG_PRIVILEGED_OPS));
        privilegedOps.clearApplicationUserData(PRINT_SPOOLER_PACKAGE_NAME);
    }

    protected void verifyLayoutCall(InOrder inOrder, PrintDocumentAdapter mock,
            PrintAttributes oldAttributes, PrintAttributes newAttributes,
            final boolean forPreview) {
        inOrder.verify(mock).onLayout(eq(oldAttributes), eq(newAttributes),
                any(CancellationSignal.class), any(LayoutResultCallback.class), argThat(
                        new BaseMatcher<Bundle>() {
                            @Override
                            public boolean matches(Object item) {
                                Bundle bundle = (Bundle) item;
                                return forPreview == bundle.getBoolean(
                                        PrintDocumentAdapter.EXTRA_PRINT_PREVIEW);
                            }

                            @Override
                            public void describeTo(Description description) {
                                /* do nothing */
                            }
                        }));
    }

    protected PrintDocumentAdapter createMockPrintDocumentAdapter(Answer<Void> layoutAnswer,
            Answer<Void> writeAnswer, Answer<Void> finishAnswer) {
        // Create a mock print adapter.
        PrintDocumentAdapter adapter = mock(PrintDocumentAdapter.class);
        if (layoutAnswer != null) {
            doAnswer(layoutAnswer).when(adapter).onLayout(any(PrintAttributes.class),
                    any(PrintAttributes.class), any(CancellationSignal.class),
                    any(LayoutResultCallback.class), any(Bundle.class));
        }
        if (writeAnswer != null) {
            doAnswer(writeAnswer).when(adapter).onWrite(any(PageRange[].class),
                    any(ParcelFileDescriptor.class), any(CancellationSignal.class),
                    any(WriteResultCallback.class));
        }
        if (finishAnswer != null) {
            doAnswer(finishAnswer).when(adapter).onFinish();
        }
        return adapter;
    }

    @SuppressWarnings("unchecked")
    protected PrinterDiscoverySessionCallbacks createMockPrinterDiscoverySessionCallbacks(
            Answer<Void> onStartPrinterDiscovery, Answer<Void> onStopPrinterDiscovery,
            Answer<Void> onValidatePrinters, Answer<Void> onStartPrinterStateTracking,
            Answer<Void> onStopPrinterStateTracking, Answer<Void> onDestroy) {
        PrinterDiscoverySessionCallbacks callbacks = mock(PrinterDiscoverySessionCallbacks.class);

        doCallRealMethod().when(callbacks).setSession(any(StubbablePrinterDiscoverySession.class));
        when(callbacks.getSession()).thenCallRealMethod();

        if (onStartPrinterDiscovery != null) {
            doAnswer(onStartPrinterDiscovery).when(callbacks).onStartPrinterDiscovery(
                    any(List.class));
        }
        if (onStopPrinterDiscovery != null) {
            doAnswer(onStopPrinterDiscovery).when(callbacks).onStopPrinterDiscovery();
        }
        if (onValidatePrinters != null) {
            doAnswer(onValidatePrinters).when(callbacks).onValidatePrinters(
                    any(List.class));
        }
        if (onStartPrinterStateTracking != null) {
            doAnswer(onStartPrinterStateTracking).when(callbacks).onStartPrinterStateTracking(
                    any(PrinterId.class));
        }
        if (onStopPrinterStateTracking != null) {
            doAnswer(onStopPrinterStateTracking).when(callbacks).onStopPrinterStateTracking(
                    any(PrinterId.class));
        }
        if (onDestroy != null) {
            doAnswer(onDestroy).when(callbacks).onDestroy();
        }

        return callbacks;
    }

    protected PrintServiceCallbacks createMockPrintServiceCallbacks(
            Answer<PrinterDiscoverySessionCallbacks> onCreatePrinterDiscoverySessionCallbacks,
            Answer<Void> onPrintJobQueued, Answer<Void> onRequestCancelPrintJob) {
        final PrintServiceCallbacks service = mock(PrintServiceCallbacks.class);

        doCallRealMethod().when(service).setService(any(PrintService.class));
        when(service.getService()).thenCallRealMethod();

        if (onCreatePrinterDiscoverySessionCallbacks != null) {
            doAnswer(onCreatePrinterDiscoverySessionCallbacks).when(service)
                    .onCreatePrinterDiscoverySessionCallbacks();
        }
        if (onPrintJobQueued != null) {
            doAnswer(onPrintJobQueued).when(service).onPrintJobQueued(any(PrintJob.class));
        }
        if (onRequestCancelPrintJob != null) {
            doAnswer(onRequestCancelPrintJob).when(service).onRequestCancelPrintJob(
                    any(PrintJob.class));
        }

        return service;
    }

    protected final class CallCounter {
        private final Object mLock = new Object();

        private int mCallCount;

        public void call() {
            synchronized (mLock) {
                mCallCount++;
            }
        }

        public void waitForCount(int count, long timeoutMIllis) throws TimeoutException {
            synchronized (mLock) {
                final long startTimeMillis = SystemClock.uptimeMillis();
                while (mCallCount < count) {
                    try {
                        final long elapsedTimeMillis = SystemClock.uptimeMillis() - startTimeMillis;
                        final long remainingTimeMillis = timeoutMIllis - elapsedTimeMillis;
                        if (remainingTimeMillis <= 0) {
                            throw new TimeoutException();
                        }
                        mLock.wait(timeoutMIllis);
                    } catch (InterruptedException ie) {
                        /* ignore */
                    }
                }
            }
        }
    }
}
