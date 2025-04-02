package com.yourfitness.pt.ui.features.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.formatEeeeMmmDd
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.ui.features.fitness_calendar.toPx
import com.yourfitness.common.ui.utils.toAge
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.utils.dialPhoneNumber
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.ItemClientPagerBinding
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.network.dto.ProfileInfoDto
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.network.dto.fullName

class ClientPagerAdapter<T : ClientInductionData>(
    private val isPager: Boolean = true,
    private val clients: ArrayList<T> = arrayListOf(),
    private val onClick: ((PtClientDto) -> Unit)? = null,
    private val onInductionClick: ((InductionInfo) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isSearchMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ItemClientPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (!isPager) {
            val layoutParams: RecyclerView.LayoutParams =
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            binding.root.layoutParams = layoutParams
        }
        return if (viewType == 0) {
            ClientViewHolder<PtClientDto>(binding, onClick)
        } else {
            InductionViewHolder<InductionInfo>(binding, onInductionClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> (holder as ClientViewHolder<T>).bind(clients[position], isSearchMode)
            1 -> (holder as InductionViewHolder<T>).bind(clients[position], isSearchMode)
        }
    }

    override fun getItemCount(): Int = clients.size

    override fun getItemViewType(position: Int): Int {
        return when (clients[position]) {
            is PtClientDto -> 0
            else -> 1
        }
    }

    fun setData(clients: List<T>) {
        this.clients.clear()
        this.clients.addAll(clients)
        notifyDataSetChanged()
    }

    fun setSearchMode(isSearch: Boolean) {
        isSearchMode = isSearch
    }
}

class ClientViewHolder<T: ClientInductionData>(
    binding: ItemClientPagerBinding,
    private val onClick: ((PtClientDto) -> Unit)?
) : BaseClientInfoViewHolder<T>(binding) {

    private val binding: ItemClientPagerBinding = ItemClientPagerBinding.bind(itemView)

    override fun bind(client: T?, isSearch: Boolean) {
        client as PtClientDto?
        bind(client?.profileInfo, isSearch)

        val context = binding.root.context
        binding.root.setOnClickListener { client?.let { obj -> onClick?.invoke(obj) } }
        binding.clientInfoContainer.description.text =
            context.getString(R.string.sessions_conducted, client?.conductedSessions)
        binding.facilityContainer.root.isVisible = false
    }
}

class InductionViewHolder<T: ClientInductionData>(
    binding: ItemClientPagerBinding,
    private val onClick: ((InductionInfo) -> Unit)?,
) : BaseClientInfoViewHolder<T>(binding) {

    private val binding: ItemClientPagerBinding = ItemClientPagerBinding.bind(itemView)

    override fun bind(client: T?, isSearch: Boolean) {
        client as InductionInfo?
        bind(client?.induction?.profileInfo, isSearch)

        binding.root.setOnClickListener { client?.let { obj -> onClick?.invoke(obj) } }
        binding.divider.isVisible = true
        binding.clientInfoContainer.description.isVisible = false
        binding.facilityContainer.apply {
            root.isVisible = true
            distanceTo.isVisible = false
            date.isVisible = true
            date.text = (client?.induction?.createdAt ?: 0).toMilliseconds().toDate().formatEeeeMmmDd()
            textName.text = client?.facilityName
            textAddress.text = client?.facilityAddress
            Glide.with(binding.root).load(client?.facilityLogo?.toImageUri()).into(imagePt)
        }
    }
}

abstract class BaseClientInfoViewHolder<T: ClientInductionData>(binding: ItemClientPagerBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val binding: ItemClientPagerBinding = ItemClientPagerBinding.bind(itemView)

    abstract fun bind(info: T?, isSearch: Boolean)

    protected fun bind(info: ProfileInfoDto?, isSearch: Boolean) {
        val params = binding.root.layoutParams as RecyclerView.LayoutParams
        binding.divider.isVisible = isSearch
        if (!isSearch) {
            binding.root.radius = 4.toPx().toFloat()
            params.setMargins(16.toPx(), 4.toPx(), 16.toPx(), 4.toPx())
        } else {
            binding.root.radius = 0f
            params.setMargins(0)
        }

        val context = binding.root.context
        Glide.with(binding.root).load(info?.mediaId?.toImageUri())
            .into(binding.clientInfoContainer.avatarClient)
        binding.clientInfoContainer.textName.text = context.getString(
            R.string.pt_facility_info,
            info?.fullName,
            info?.birthday?.toAge().toString()
        )
        binding.clientInfoContainer.phoneNumber.apply {
            text = info?.phoneNumber
            setOnClickListener {
                info?.phoneNumber?.let { phone ->
                    context.dialPhoneNumber(phone)
                }
            }
        }
    }
}
