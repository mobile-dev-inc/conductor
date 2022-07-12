/*
 *
 *  Copyright (c) 2022 mobile.dev inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package conductor.orchestra.yaml

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(`as` = YamlElementSelector::class)
data class YamlElementSelector(
    val text: String? = null,
    val id: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val tolerance: Int? = null,
    val optional: Boolean? = null,
    val retryTapIfNoChange: Boolean? = null,
    val point: String? = null,
    val below: YamlElementSelector? = null,
    val above: YamlElementSelector? = null,
    val leftOf: YamlElementSelector? = null,
    val rightOf: YamlElementSelector? = null,
) : YamlElementSelectorUnion
