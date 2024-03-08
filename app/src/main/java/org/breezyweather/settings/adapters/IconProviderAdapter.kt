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

package org.breezyweather.settings.adapters

import android.app.Activity
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import james.adaptiveicon.AdaptiveIcon
import james.adaptiveicon.AdaptiveIconView
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.theme.resource.providers.ResourceProvider

class IconProviderAdapter // adapter.
    (
    private val mActivity: Activity,
    private val mProviderList: List<ResourceProvider>,
    private val mListener: OnItemClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // holder.
    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mContainer: RelativeLayout = itemView.findViewById(R.id.item_icon_provider_container)
        private val mIcon: AdaptiveIconView = itemView.findViewById(R.id.item_icon_provider_clearIcon)
        private val mTitle: TextView = itemView.findViewById(R.id.item_icon_provider_title)
        private val mPreviewButton: AppCompatImageButton = itemView.findViewById(R.id.item_icon_provider_previewButton)

        fun onBindView() {
            val provider = mProviderList[bindingAdapterPosition]
            mContainer.setOnClickListener { mListener.onItemClicked(provider, bindingAdapterPosition) }
            val drawable = provider.providerIcon
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && drawable is AdaptiveIconDrawable
            ) {
                mIcon.icon = AdaptiveIcon(
                    drawable.foreground,
                    drawable.background,
                    0.5
                )
                mIcon.setPath(AdaptiveIconView.PATH_CIRCLE)
            } else {
                mIcon.icon = AdaptiveIcon(drawable, null, 1.0)
            }
            mTitle.text = provider.providerName
            mPreviewButton.setOnClickListener {
                IntentHelper.startPreviewIconActivity(
                    mActivity,
                    provider.packageName
                )
            }
        }
    }

    private inner class GetMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mGetMoreContainer = itemView.findViewById<RelativeLayout>(R.id.item_icon_provider_get_more_container)

        fun onBindView() {
            mGetMoreContainer.setOnClickListener {
                mListener.onWebItemClicked("https://github.com/breezy-weather/breezy-weather-icon-packs/blob/main/README.md")
            }
        }
    }

    // interface.
    interface OnItemClickedListener {
        fun onItemClicked(provider: ResourceProvider, adapterPosition: Int)
        fun onWebItemClicked(query: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_icon_provider, parent, false)
            )
        } else {
            GetMoreViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_icon_provider_get_more, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GetMoreViewHolder) {
            holder.onBindView()
            return
        }
        (holder as ViewHolder).onBindView()
    }

    override fun getItemCount() = mProviderList.size + 1

    override fun getItemViewType(position: Int) = if (position < mProviderList.size) 1 else -1
}
