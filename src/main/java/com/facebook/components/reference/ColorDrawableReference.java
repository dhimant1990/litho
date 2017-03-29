/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.reference;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.v4.util.Pools;

import com.facebook.litho.ComponentContext;
import com.facebook.litho.config.ComponentsConfiguration;

/**
 * A Reference for {@link ColorDrawable}. This keeps a {@link Pools.Pool} of up to 10 ColorDrawable
 * and allows to specify color and alpha as create.
