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

package com.facebook.litho;

import static androidx.lifecycle.Lifecycle.Event.ON_DESTROY;
import static androidx.lifecycle.Lifecycle.Event.ON_PAUSE;
import static androidx.lifecycle.Lifecycle.Event.ON_RESUME;
import static com.facebook.litho.LithoLifecycleProvider.LithoLifecycle.DESTROYED;
import static com.facebook.litho.LithoLifecycleProvider.LithoLifecycle.HINT_INVISIBLE;
import static com.facebook.litho.LithoLifecycleProvider.LithoLifecycle.HINT_VISIBLE;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import com.facebook.infer.annotation.Nullsafe;
import javax.annotation.Nullable;

/**
 * This LithoLifecycleProvider implementation dispatches to the registered observers the lifecycle
 * state changes triggered by the provided LifecycleOwner. For example, if a Fragment is passed as
 * param, the observers will be registered to listen to all of the fragment's lifecycle state
 * changes.
 */
@Nullsafe(Nullsafe.Mode.LOCAL)
public class AOSPLithoLifecycleProvider
    implements LithoLifecycleProvider, LifecycleEventObserver, AOSPLifecycleOwnerProvider {
  private LithoLifecycleProviderDelegate mLithoLifecycleProviderDelegate;
  private LifecycleOwner mLifecycleOwner;

  public AOSPLithoLifecycleProvider(LifecycleOwner lifecycleOwner) {
    mLithoLifecycleProviderDelegate = new LithoLifecycleProviderDelegate();
    mLifecycleOwner = lifecycleOwner;
    lifecycleOwner.getLifecycle().addObserver(this);
  }

  @Override
  public LithoLifecycle getLifecycleStatus() {
    return mLithoLifecycleProviderDelegate.getLifecycleStatus();
  }

  @Override
  public void moveToLifecycle(LithoLifecycle lithoLifecycle) {
    mLithoLifecycleProviderDelegate.moveToLifecycle(lithoLifecycle);
  }

  @Override
  public void addListener(LithoLifecycleListener listener) {
    mLithoLifecycleProviderDelegate.addListener(listener);
  }

  @Override
  public void removeListener(LithoLifecycleListener listener) {
    mLithoLifecycleProviderDelegate.removeListener(listener);
  }

  public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
    switch (event) {
      case ON_RESUME:
        moveToLifecycle(HINT_VISIBLE);
        break;
      case ON_PAUSE:
        moveToLifecycle(HINT_INVISIBLE);
        break;
      case ON_DESTROY:
        moveToLifecycle(DESTROYED);
        break;
    }
  }

  @Override
  @Nullable
  public LifecycleOwner getLifecycleOwner() {
    return mLifecycleOwner;
  }
}
