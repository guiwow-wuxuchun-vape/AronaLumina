package com.project.lumina.client.overlay.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.overlay.mods.DummyOverlay
import com.project.lumina.client.service.Services
import com.project.lumina.client.ui.theme.LuminaClientTheme
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
@Suppress("MemberVisibilityCanBePrivate")
object OverlayManager {

    private val overlayWindows = ArrayList<OverlayWindow>()

    var currentContext: Context? = null
        private set

    var isShowing = false
        private set

    init {


        with(overlayWindows) {

            if (!Services.RemisOnline) {
                add(OverlayButton())
                addAll(
                    GameManager
                        .elements
                        .filter { it.isShortcutDisplayed }
                        .map { it.overlayShortcutButton })
            }
            else add(DummyOverlay())
        }

    }

    fun showOverlayWindow(overlayWindow: OverlayWindow) {
        overlayWindows.add(overlayWindow)

        val context = currentContext
        if (isShowing && context != null) {
            showOverlayWindow(context, overlayWindow)
        }
    }

    fun dismissOverlayWindow(overlayWindow: OverlayWindow) {
        overlayWindows.remove(overlayWindow)

        val context = currentContext
        if (isShowing && context != null) {
            dismissOverlayWindow(context, overlayWindow)
        }
    }

    fun show(context: Context) {
        currentContext = context

        overlayWindows.forEach {
            showOverlayWindow(context, it)
        }

        isShowing = true
    }

    fun dismiss() {
        val context = currentContext
        if (context != null) {
            overlayWindows.forEach {
                dismissOverlayWindow(context, it)
            }
            isShowing = false
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun showOverlayWindow(context: Context, overlayWindow: OverlayWindow) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = overlayWindow.layoutParams
        val composeView = overlayWindow.composeView
    
        // --------- 1. Android 14 强制校验 token ---------
        if (Build.VERSION.SDK_INT >= 34) {
            // 如果 context 不是 Activity，直接抛，避免 BadTokenException
            if (context !is android.app.Activity) {
                android.util.Log.e("OverlayManager", "API34+ requires Activity context")
                return
            }
            // 利用 Activity 的 WindowManager（自带 token）
            val activityWM = context.windowManager
            try {
                activityWM.addView(composeView, layoutParams)
            } catch (e: WindowManager.BadTokenException) {
                android.util.Log.e("OverlayManager", "BadToken in API34", e)
            }
            return
        }
    
        // --------- 2. 旧系统走老逻辑 ---------
        try {
            windowManager.addView(composeView, layoutParams)
        } catch (e: WindowManager.BadTokenException) {
            // 低版本也遇到 token 失效，直接 toast 后忽略
            android.widget.Toast.makeText(
                context,
                "Overlay failed: app in background",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun dismissOverlayWindow(context: Context, overlayWindow: OverlayWindow) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val composeView = overlayWindow.composeView

        try {
            windowManager.removeView(composeView)
        } catch (_: Exception) {

        }
    }

    fun showCustomOverlay(view: View) {
        val wm = AppContext.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(view, params)
    }

    fun dismissCustomOverlay(view: View) {
        val wm = AppContext.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.removeView(view)
    }


}