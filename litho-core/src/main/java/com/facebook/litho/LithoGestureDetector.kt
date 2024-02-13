/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.litho

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import com.facebook.litho.annotations.OnCreateLayout

/**
 * Simple wrapper of [GestureDetector] to be used in Litho lifecycle methods. Using this class
 * ensures that UI Thread [Handler] is used for detecting gestures.
 *
 * Main purpose of creating this wrapper is to avoid usages where developers do not explicitly
 * provide UI Thread [Handler] inside @[OnCreateLayout] or other lifecycle methods that can be
 * called from BG threads which would potentially cause the app crash.
 */
open class LithoGestureDetector(context: Context, listener: OnGestureListener) :
    GestureDetector(context, listener, Handler(Looper.getMainLooper()))
