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

package maestro.orchestra.yaml

import maestro.Point
import maestro.orchestra.ApplyConfigurationCommand
import maestro.orchestra.AssertCommand
import maestro.orchestra.BackPressCommand
import maestro.orchestra.ElementSelector
import maestro.orchestra.InputTextCommand
import maestro.orchestra.LaunchAppCommand
import maestro.orchestra.MaestroCommand
import maestro.orchestra.ScrollCommand
import maestro.orchestra.SwipeCommand
import maestro.orchestra.TapOnElementCommand
import maestro.orchestra.TapOnPointCommand

data class YamlFluentCommand(
    val tapOn: YamlElementSelectorUnion? = null,
    val assertVisible: YamlElementSelector? = null,
    val action: String? = null,
    val inputText: String? = null,
    val launchApp: YamlLaunchAppUnion? = null,
    val swipe: YamlElementSelectorUnion? = null,
    val config: Map<String, *>? = null,
) {

    @SuppressWarnings("ComplexMethod")
    fun toCommand(): MaestroCommand {
        return when {
            launchApp != null -> launchApp(launchApp)
            tapOn != null -> tapCommand(tapOn)
            assertVisible != null -> MaestroCommand(
                assertCommand = AssertCommand(
                    visible = toElementSelector(assertVisible),
                )
            )
            inputText != null -> MaestroCommand(
                inputTextCommand = InputTextCommand(inputText)
            )
            swipe != null -> swipeCommand(swipe)
            action != null -> when (action) {
                "back" -> MaestroCommand(backPressCommand = BackPressCommand())
                "scroll" -> MaestroCommand(scrollCommand = ScrollCommand())
                else -> throw IllegalStateException("Unknown navigation target: $action")
            }
            config != null -> MaestroCommand(
                applyConfigurationCommand = ApplyConfigurationCommand(config),
            )
            else -> throw IllegalStateException("No mapping provided for $this")
        }
    }

    private fun launchApp(command: YamlLaunchAppUnion): MaestroCommand {
        return when (command) {
            is StringLaunchApp -> MaestroCommand(
                launchAppCommand = LaunchAppCommand(
                    appId = command.appId,
                )
            )
            is YamlLaunchApp -> MaestroCommand(
                launchAppCommand = LaunchAppCommand(
                    appId = command.appId,
                    clearState = command.clearState,
                )
            )
        }
    }

    private fun tapCommand(tapOn: YamlElementSelectorUnion): MaestroCommand {
        val retryIfNoChange = (tapOn as? YamlElementSelector)?.retryTapIfNoChange ?: true
        val waitUntilVisible = (tapOn as? YamlElementSelector)?.waitUntilVisible ?: true
        val point = (tapOn as? YamlElementSelector)?.point

        return if (point != null) {
            val points = point.split(",")
                .map {
                    it.trim().toInt()
                }

            MaestroCommand(
                tapOnPoint = TapOnPointCommand(
                    x = points[0],
                    y = points[1],
                    retryIfNoChange = retryIfNoChange,
                    waitUntilVisible = waitUntilVisible,
                )
            )
        } else {
            MaestroCommand(
                tapOnElement = TapOnElementCommand(
                    selector = toElementSelector(tapOn),
                    retryIfNoChange = retryIfNoChange,
                    waitUntilVisible = waitUntilVisible,
                )
            )
        }
    }

    private fun swipeCommand(tapOn: YamlElementSelectorUnion): MaestroCommand {
        val start = (tapOn as? YamlElementSelector)?.start
        val end = (tapOn as? YamlElementSelector)?.end
        val startPoint: Point?
        val endPoint: Point?

        if (start != null) {
            val points = start.split(",")
                .map {
                    it.trim().toInt()
                }

            startPoint = Point(points[0], points[1])
        } else {
            throw IllegalStateException("No start point configured for swipe action")
        }

        if (end != null) {
            val points = end.split(",")
                .map {
                    it.trim().toInt()
                }

            endPoint = Point(points[0], points[1])
        } else {
            throw IllegalStateException("No end point configured for swipe action")
        }

        return MaestroCommand(
            swipeCommand = SwipeCommand(startPoint, endPoint)
        )
    }

    private fun toElementSelector(selectorUnion: YamlElementSelectorUnion): ElementSelector {
        return if (selectorUnion is StringElementSelector) {
            ElementSelector(
                textRegex = selectorUnion.value,
            )
        } else if (selectorUnion is YamlElementSelector) {
            toElementSelector(selectorUnion)
        } else {
            throw IllegalStateException("Unknown selector type: $selectorUnion")
        }
    }

    private fun toElementSelector(selector: YamlElementSelector): ElementSelector {
        val size = if (selector.width != null || selector.height != null) {
            ElementSelector.SizeSelector(
                width = selector.width,
                height = selector.height,
                tolerance = selector.tolerance,
            )
        } else {
            null
        }

        return ElementSelector(
            textRegex = selector.text,
            idRegex = selector.id,
            size = size,
            below = selector.below?.let { toElementSelector(it) },
            above = selector.above?.let { toElementSelector(it) },
            leftOf = selector.leftOf?.let { toElementSelector(it) },
            rightOf = selector.rightOf?.let { toElementSelector(it) },
            containsChild = selector.containsChild?.let { toElementSelector(it) },
            optional = selector.optional ?: false,
        )
    }
}
