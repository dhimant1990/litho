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

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.facebook.litho.testing.TestDrawableComponent.create;
import static com.facebook.litho.testing.helper.ComponentTestHelper.mountComponent;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.powermock.reflect.Whitebox.getInternalState;

import android.graphics.Color;
import android.support.v4.util.LongSparseArray;
import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.TestViewComponent;
import com.facebook.litho.testing.helper.ComponentTestHelper;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;

@RunWith(ComponentsTestRunner.class)
public class MountStateRemountTest {
  private ComponentContext mContext;

  @Before
  public void setup() {
    mContext = new ComponentContext(RuntimeEnvironment.application);
  }

  @Test
  public void testRemountSameLayoutState() {
    final TestComponent component1 = create(mContext)
        .build();
    final TestComponent component2 = create(mContext)
        .build();
    final TestComponent component3 = create(mContext)
        .build();
    final TestComponent component4 = create(mContext)
        .build();

    final LithoView lithoView =
        mountComponent(
            mContext, Column.create(mContext).child(component1).child(component2).build());

    assertThat(component1.isMounted()).isTrue();
    assertThat(component2.isMounted()).isTrue();

    mountComponent(
        mContext, lithoView, Column.create(mContext).child(component3).child(component4).build());

    assertThat(component1.isMounted()).isTrue();
    assertThat(component2.isMounted()).isTrue();
    assertThat(component3.isMounted()).isFalse();
    assertThat(component4.isMounted()).isFalse();

    final MountState mountState = getInternalState(lithoView, "mMountState");
    final LongSparseArray<MountItem> indexToItemMap =
        getInternalState(mountState, "mIndexToItemMap");

    final List<Component> components = new ArrayList<>();
    for (int i = 0; i < indexToItemMap.size(); i++) {
      components.add(indexToItemMap.valueAt(i).getComponent());
    }

    assertThat(containsRef(components, component1)).isFalse();
    assertThat(containsRef(components, component2)).isFalse();
    assertThat(containsRef(components, component3)).isTrue();
    assertThat(containsRef(components, component4)).isTrue();
  }

  /**
   * There was a crash when mounting a drawing in place of a view. This test is here to make sure
   * this does not regress. To reproduce this crash the pools needed to be in a specific state
   * as view layout outputs and mount items were being re-used for drawables.
   */
  @Test
  public void testRemountDifferentMountType() throws IllegalAccessException, NoSuchFieldException {
    clearPool("sLayoutOutputPool");
    clearPool("sViewNodeInfoPool");

    final LithoView lithoView =
        ComponentTestHelper.mountComponent(mContext, TestViewComponent.create(mContext).build());

    ComponentTestHelper.mountComponent(
        mContext, lithoView, TestDrawableComponent.create(mContext).build());
  }

  @Test
  public void testRemountNewLayoutState() {
    final TestComponent component1 = create(mContext).color(Color.RED).build();
    final TestComponent component2 = create(mContext).color(Color.BLUE).build();
    final TestComponent component3 = create(mContext)
        .unique()
        .build();
    final TestComponent component4 = create(mContext)
        .unique()
        .build();

    final LithoView lithoView =
        mountComponent(
            mContext, Column.create(mContext).child(component1).child(component2).build());

    assertThat(component1.isMounted()).isTrue();
    assertThat(component2.isMounted()).isTrue();

    mountComponent(
        mContext, lithoView, Column.create(mContext).child(component3).child(component4).build());

    assertThat(component1.isMounted()).isFalse();
    assertThat(component2.isMounted()).isFalse();
    assertThat(component3.isMounted()).isTrue();
    assertThat(component4.isMounted()).isTrue();
  }

  @Test
  public void testRemountAfterSettingNewRootTwice() {
    final TestComponent component1 =
        create(mContext).color(Color.RED).returnSelfInMakeShallowCopy().build();
    final TestComponent component2 =
        create(mContext).returnSelfInMakeShallowCopy().color(Color.BLUE).build();

    final LithoView lithoView = new LithoView(mContext);
    final ComponentTree componentTree =
        ComponentTree.create(mContext, Column.create(mContext).child(component1).build()).build();
    mountComponent(
        lithoView, componentTree, makeMeasureSpec(100, EXACTLY), makeMeasureSpec(100, EXACTLY));

    assertThat(component1.isMounted()).isTrue();

    componentTree.setRootAndSizeSpec(
        Column.create(mContext).child(component2).build(),
        makeMeasureSpec(50, EXACTLY),
        makeMeasureSpec(50, EXACTLY));

    componentTree.setSizeSpec(makeMeasureSpec(100, EXACTLY), makeMeasureSpec(100, EXACTLY));

    assertThat(component2.isMounted()).isTrue();
  }

  @Test
  public void testRemountPartiallyDifferentLayoutState() {
    final TestComponent component1 = create(mContext)
        .build();
    final TestComponent component2 = create(mContext)
        .build();
    final TestComponent component3 = create(mContext)
        .build();
    final TestComponent component4 = create(mContext)
        .build();

    final LithoView lithoView =
        mountComponent(
            mContext, Column.create(mContext).child(component1).child(component2).build());

    assertThat(component1.isMounted()).isTrue();
    assertThat(component2.isMounted()).isTrue();

    mountComponent(
        mContext,
        lithoView,
        Column.create(mContext)
            .child(component3)
            .child(Column.create(mContext).wrapInView().child(component4))
            .build());

    assertThat(component1.isMounted()).isTrue();
    assertThat(component2.isMounted()).isFalse();
    assertThat(component3.isMounted()).isFalse();
    assertThat(component4.isMounted()).isTrue();
  }

  private boolean containsRef(List<?> list, Object object) {
    for (Object o : list) {
      if (o == object) {
        return true;
      }
    }
    return false;
  }

  private static void clearPool(String name) {
    final RecyclePool<?> pool =
        Whitebox.getInternalState(ComponentsPools.class, name);

    while (pool.acquire() != null) {
      // Run.
    }
  }
}
