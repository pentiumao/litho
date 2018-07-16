/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.litho;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.ViewGroup;
import com.facebook.litho.config.ComponentsConfiguration;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;

@RunWith(ComponentsTestRunner.class)
public class ComponentTreeIncrementalMountTest {
  private LithoView mLithoView;
  private ComponentTree mComponentTree;
  private ComponentContext mComponentContext;

  private final Rect mMountedRect = new Rect();

  @Rule public ExpectedException mExpectedException = ExpectedException.none();

  @Before
  public void setup() {
    ComponentsConfiguration.incrementalMountUsesLocalVisibleBounds = false;
    mComponentContext = new ComponentContext(RuntimeEnvironment.application);
    mComponentTree =
        ComponentTree.create(
                mComponentContext,
                TestDrawableComponent.create(mComponentContext).color(Color.BLACK).build())
            .layoutDiffing(false)
            .build();

    mLithoView = mock(TestLithoView.class);
    when(mLithoView.getMountState()).thenReturn(mock(MountState.class));
    Whitebox.setInternalState(mComponentTree, "mLithoView", mLithoView);
    Whitebox.setInternalState(mComponentTree, "mMainThreadLayoutState", mock(LayoutState.class));

    // Can't use verify as the rect is reset when it is released back to the pool, which occurs
    // before we can check it.
    doAnswer(
            new Answer() {
              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                mMountedRect.set((Rect) invocation.getArguments()[1]);
                return null;
              }
            })
        .when(mLithoView)
        .mount(any(LayoutState.class), any(Rect.class), eq(true));
  }

  @After
  public void tearDown() {
    ComponentsConfiguration.incrementalMountUsesLocalVisibleBounds = true;
  }

  @Test
  public void testIncrementalMountBoundsSameAsParent() {
    setupIncrementalMountTest(new Rect(0, 0, 10, 10), new Rect(0, 0, 10, 10));

    mComponentTree.incrementalMountComponent();

    assertThat(mMountedRect).isEqualTo(new Rect(0, 0, 10, 10));
  }

  @Test
  public void testIncrementalMountBoundsWithNoParent() {
    setupIncrementalMountTest(new Rect(0, 0, 10, 10), null);

    mComponentTree.incrementalMountComponent();
    assertThat(mMountedRect).isEqualTo(new Rect(0, 0, 10, 10));
  }

  @Test
  public void testIncrementalMountBoundsInsideParent() {
    setupIncrementalMountTest(new Rect(10, 10, 20, 20), new Rect(0, 0, 30, 30));

    mComponentTree.incrementalMountComponent();
    assertThat(mMountedRect).isEqualTo(new Rect(0, 0, 10, 10));
  }

  @Test
  public void testIncrementalMountBoundsOutsideParent() {
    setupIncrementalMountTest(new Rect(10, 10, 20, 20), new Rect(20, 10, 30, 30));

    mComponentTree.incrementalMountComponent();
    verify(mLithoView, never()).mount(any(LayoutState.class), any(Rect.class), eq(true));
  }

  @Test
  public void testIncrementalMountBoundsToLeftOfParent() {
    setupIncrementalMountTest(new Rect(10, 10, 20, 20), new Rect(15, 10, 25, 20));

    mComponentTree.incrementalMountComponent();
    assertThat(mMountedRect).isEqualTo(new Rect(5, 0, 10, 10));
  }

  @Test
  public void testIncrementalMountBoundsToTopOfParent() {
    setupIncrementalMountTest(new Rect(10, 10, 20, 20), new Rect(10, 15, 20, 25));

    mComponentTree.incrementalMountComponent();
    assertThat(mMountedRect).isEqualTo(new Rect(0, 5, 10, 10));
  }

  @Test
  public void testIncrementalMountBoundsToRightOfParent() {
    setupIncrementalMountTest(new Rect(10, 10, 20, 20), new Rect(5, 10, 15, 20));

    mComponentTree.incrementalMountComponent();
    assertThat(mMountedRect).isEqualTo(new Rect(0, 0, 5, 10));
  }

  @Test
  public void testIncrementalMountBoundsToBottomOfParent() {
    setupIncrementalMountTest(new Rect(10, 10, 20, 20), new Rect(10, 5, 20, 15));

    mComponentTree.incrementalMountComponent();
    assertThat(mMountedRect).isEqualTo(new Rect(0, 0, 10, 5));
  }

  @Test
  public void testGetLocalVisibleBoundsWithExactRect() {
    mComponentTree =
        ComponentTree.create(
                mComponentContext,
                TestDrawableComponent.create(mComponentContext).color(Color.BLACK).build())
            .useExactRectForVisibilityEvents()
            .build();

    Whitebox.setInternalState(mComponentTree, "mLithoView", mLithoView);
    Whitebox.setInternalState(mComponentTree, "mMainThreadLayoutState", mock(LayoutState.class));

    doAnswer(
            new Answer<Boolean>() {
              @Override
              public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Rect rect = (Rect) invocation.getArguments()[0];
                rect.set(new Rect(10, 5, 20, 15));
                return true;
              }
            })
        .when(mLithoView)
        .getLocalVisibleRect(any(Rect.class));

    mComponentTree.incrementalMountComponent();
    assertThat(mMountedRect).isEqualTo(new Rect(10, 5, 20, 15));
  }

  private void setupIncrementalMountTest(
      final Rect lithoViewBoundsInScreen, final Rect parentBoundsInScreen) {

    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                int[] location = (int[]) invocation.getArguments()[0];
                location[0] = lithoViewBoundsInScreen.left;
                location[1] = lithoViewBoundsInScreen.top;
                return null;
              }
            })
        .when(mLithoView)
        .getLocationOnScreen(any(int[].class));
    when(mLithoView.getWidth())
        .thenReturn(lithoViewBoundsInScreen.right - lithoViewBoundsInScreen.left);
    when(mLithoView.getHeight())
        .thenReturn(lithoViewBoundsInScreen.bottom - lithoViewBoundsInScreen.top);

    if (parentBoundsInScreen == null) {
      return;
    }

    ViewGroup parentView = mock(ViewGroup.class);
    when(mLithoView.getParent()).thenReturn(parentView);

    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                int[] location = (int[]) invocation.getArguments()[0];
                location[0] = parentBoundsInScreen.left;
                location[1] = parentBoundsInScreen.top;
                return null;
              }
            })
        .when(parentView)
        .getLocationOnScreen(any(int[].class));
    when(parentView.getWidth()).thenReturn(parentBoundsInScreen.right - parentBoundsInScreen.left);
    when(parentView.getHeight()).thenReturn(parentBoundsInScreen.bottom - parentBoundsInScreen.top);
  }

  /**
   * Required in order to ensure that {@link LithoView#mount(LayoutState, Rect, boolean)} is mocked
   * correctly (it needs protected access to be mocked).
   */
  public static class TestLithoView extends LithoView {

    public TestLithoView(Context context) {
      super(context);
    }

    @Override
    protected void mount(
        LayoutState layoutState, Rect currentVisibleArea, boolean processVisibilityOutputs) {
      super.mount(layoutState, currentVisibleArea, processVisibilityOutputs);
    }
  }
}
