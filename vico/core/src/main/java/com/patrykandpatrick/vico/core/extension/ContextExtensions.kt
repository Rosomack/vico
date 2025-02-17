/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.extension

import android.content.Context
import android.os.Build
import android.util.TypedValue

/**
 * Converts the provided dimension from sp to px.
 */
public fun Context.spToPx(sp: Float): Float = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    TypedValue.convertDimensionToPixels(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
} else {
    sp * resources.configuration.fontScale
}
