/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.settings.compose

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.DarkMode
import org.breezyweather.common.basic.models.options.appearance.*
import org.breezyweather.common.extensions.toBitmap
import org.breezyweather.common.ui.composables.AlertDialogLink
import org.breezyweather.common.ui.composables.AlertDialogNoPadding
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SwitchPreferenceView
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.settings.preference.switchPreferenceItem
import org.breezyweather.theme.ThemeManager
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.resource.ResourcesProviderFactory
import org.breezyweather.theme.resource.providers.ResourceProvider
import java.util.Date

@Composable
fun AppearanceSettingsScreen(
    context: Context,
    navController: NavHostController,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = paddingValues,
    ) {
        listPreferenceItem(R.string.settings_appearance_language_title) { id ->
            ListPreferenceView(
                titleId = id,
                valueArrayId = R.array.language_values,
                nameArrayId = R.array.languages,
                selectedKey = SettingsManager.getInstance(context).language.id,
                onValueChanged = {
                    SettingsManager.getInstance(context).language = Language.getInstance(it)
                    SettingsManager.getInstance(context).languageUpdateLastTimestamp = Date().time

                    SnackbarHelper.showSnackbar(
                        content = context.getString(R.string.settings_changes_apply_after_restart),
                        action = context.getString(R.string.action_restart)
                    ) {
                        BreezyWeather.instance.recreateAllActivities()
                    }
                },
            )
        }
        listPreferenceItem(R.string.settings_appearance_dark_mode_title) { id ->
            ListPreferenceView(
                titleId = id,
                selectedKey = SettingsManager.getInstance(context).darkMode.id,
                valueArrayId = R.array.dark_mode_values,
                nameArrayId = R.array.dark_modes,
                onValueChanged = {
                    SettingsManager
                        .getInstance(context)
                        .darkMode = DarkMode.getInstance(it)

                    AsyncHelper.delayRunOnUI({
                        ThemeManager
                            .getInstance(context)
                            .update(darkMode = SettingsManager.getInstance(context).darkMode)
                    },300)
                },
            )
        }
        switchPreferenceItem(R.string.settings_appearance_dark_mode_locations_title) { id ->
            SwitchPreferenceView(
                titleId = id,
                summaryOnId = R.string.settings_enabled,
                summaryOffId = R.string.settings_disabled,
                checked = SettingsManager.getInstance(context).dayNightModeForLocations,
                onValueChanged = {
                    SettingsManager.getInstance(context).dayNightModeForLocations = it
                },
            )
        }
        clickablePreferenceItem(
            R.string.settings_appearance_icon_pack_title
        ) {
            val dialogIconPackOpenState = remember { mutableStateOf(false) }
            val dialogLinkOpenState = remember { mutableStateOf(false) }
            val iconProviderState = remember {
                mutableStateOf(
                    SettingsManager.getInstance(context).iconProvider
                )
            }
            val listProviderState = remember {
                mutableStateOf(listOf<ResourceProvider>())
            }

            PreferenceView(
                title = stringResource(it),
                summary = ResourcesProviderFactory
                    .getNewInstance(iconProviderState.value)
                    .providerName
            ) {
                dialogIconPackOpenState.value = true
                /*(context as? Activity)?.let { activity ->
                    ProvidersPreviewerDialog.show(activity) { packageName ->
                        SettingsManager.getInstance(context).iconProvider = packageName
                        iconProviderState.value = packageName
                    }
                }*/
            }
            if (dialogIconPackOpenState.value) {
                // TODO: async
                listProviderState.value = ResourcesProviderFactory.getProviderList(BreezyWeather.instance)

                AlertDialogNoPadding(
                    onDismissRequest = {
                        dialogIconPackOpenState.value = false
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                dialogLinkOpenState.value = true
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.settings_icon_packs_get_more),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                dialogIconPackOpenState.value = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.action_close),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.settings_icon_packs_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    },
                    text = {
                        // 3 columns: icon / name / search icon
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                /*.fillMaxHeight()*/,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(listProviderState.value) {
                                ListItem(
                                    headlineContent = {
                                        Text(it.providerName ?: "")
                                    },
                                    modifier = Modifier.clickable {
                                        SettingsManager.getInstance(context).iconProvider = it.packageName
                                        iconProviderState.value = it.packageName
                                        dialogIconPackOpenState.value = false
                                    },
                                    leadingContent = {
                                        it.providerIcon?.toBitmap()?.asImageBitmap()?.let { bitmap ->
                                            Image(
                                                bitmap,
                                                contentDescription = it.providerName,
                                                modifier = Modifier
                                                    .height(42.dp)
                                                    .width(42.dp)
                                            )
                                        }
                                    },
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                IntentHelper.startPreviewIconActivity(
                                                    context as Activity,
                                                    it.packageName
                                                )
                                            },
                                            modifier = Modifier.clip(CircleShape)
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.ic_search),
                                                contentDescription = stringResource(R.string.settings_icon_packs_check_details),
                                                tint = DayNightTheme.colors.titleColor
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
            if (dialogLinkOpenState.value) {
                AlertDialogLink(
                    onClose = { dialogLinkOpenState.value = false },
                    linkToOpen = "https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/README.md"
                )
            }
        }
        clickablePreferenceItem(R.string.settings_units) { id ->
            PreferenceView(
                titleId = id,
                summaryId = R.string.settings_units_summary
            ) {
                navController.navigate(SettingsScreenRouter.Unit.route)
            }
        }

        bottomInsetItem()
    }
}