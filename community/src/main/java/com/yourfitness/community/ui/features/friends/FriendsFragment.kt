package com.yourfitness.community.ui.features.friends

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.ui.features.details.FriendDetailsFragment
import com.yourfitness.community.ui.features.details.UnfriendDialog
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.FragmentFriendsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import reactivecircus.flowbinding.android.widget.queryTextChanges

@AndroidEntryPoint
class FriendsFragment : MviFragment<FriendsIntent, FriendsState, FriendsViewModel>() {

    override val binding: FragmentFriendsBinding by viewBinding()
    override val viewModel: FriendsViewModel by viewModels()

    private var pagingAdapter: SearchFriendsAdapter = createPagingAdapter()
    private val contentAdapter by lazy { FriendsAdapter(::onActionClick, ::onCardClick) }
    private val requestsAdapter by lazy { FriendsAdapter(::onActionClick, ::onCardClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbar)
        setupUnfriendListener()
        binding.toolbar.title = getString(R.string.community)
        binding.friendsList.adapter = contentAdapter
        binding.requestsList.adapter = requestsAdapter
        binding.friendsSearchList.adapter = pagingAdapter
        setupEmptyState()

        setupSearchView()
        setupToolbarSearch(false)

        binding.friendsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.requestsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.friendsSearchList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.tabLayout.addOnTabSelectionListener { onTabSelected(it.position) }
    }

    private fun setupFlowListener() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.flow?.collectLatest { pagingData ->
                pagingAdapter = createPagingAdapter()
                withContext(Dispatchers.Main) {
                    pagingAdapter.addOnPagesUpdatedListener { showLoading(false) }
                    binding.friendsSearchList.adapter = pagingAdapter
                }
                pagingAdapter.submitData(pagingData)
            }
        }
    }

    override fun renderState(state: FriendsState) {
        when (state) {
            is FriendsState.Loading -> {
                showLoading(state.active)
            }
            is FriendsState.Error -> {
                showLoading(false)
            }
            is FriendsState.FriendsLoaded -> {
                showLoading(false)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(state.selectedTab))
                setListsData(state.friends, state.requests)
                contentAdapter.notifyDataSetChanged()
                requestsAdapter.notifyDataSetChanged()
            }
            is FriendsState.SearchPrepared -> {
                setupFlowListener()
            }
            is FriendsState.SearchCleared -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    pagingAdapter.submitData(PagingData.empty())
                }
            }
            is FriendsState.FriendsSilentlyUpdated -> {
                setListsData(state.friends, state.requests)
                pagingAdapter.updateSearchItem(state.updatedPosition, state.updatedItem)
                if (state.requested) showPopupMessage(true)
                else if (state.accepted) showPopupMessage(false)
            }
            is FriendsState.SomethingWentWrong -> showError(Throwable(getString(com.yourfitness.common.R.string.error_something_went_wrong)))
        }
        viewModel.state.value = null
    }

    override fun showLoading(isLoading: Boolean) {
        super.showLoading(isLoading)
        if (!isLoading) {
            showLocalLoading(false)
        }
    }

    private fun setListsData(friends: List<FriendsEntity>, requests: List<FriendsEntity>) {
        contentAdapter.setData(friends)
        requestsAdapter.setData(requests)

        val isEmpty = friends.isEmpty() && requests.isEmpty() && !binding.searchMain.isVisible
        binding.emptyState.root.isVisible = isEmpty
        binding.searchStub.root.isVisible = isEmpty

        setupToolbarSearch(friends.isNotEmpty() || requests.isNotEmpty())

        val isNotEmpty = friends.isNotEmpty() && requests.isNotEmpty()
        binding.tabLayout.isVisible = isNotEmpty

        if (!binding.tabLayout.isVisible) {
            binding.friendsList.isVisible = friends.isNotEmpty()
            binding.requestsList.isVisible = !binding.friendsList.isVisible && requests.isNotEmpty()

        } else {
            if (isNotEmpty) {
                binding.friendsList.isVisible = binding.tabLayout.getTabAt(0)?.isSelected == true
                binding.requestsList.isVisible = binding.tabLayout.getTabAt(1)?.isSelected == true
            } else {
                binding.friendsList.isVisible = requests.isEmpty() && friends.isNotEmpty()
                binding.requestsList.isVisible = friends.isEmpty() && requests.isNotEmpty()
            }
        }

        binding.toolbar.title = if (isNotEmpty) null else getString(R.string.community)

        binding.tabLayout.getTabAt(0)?.text = getString(R.string.friends, friends.size)
        binding.tabLayout.getTabAt(1)?.text = getString(R.string.requests, requests.size)
    }

    private fun showLocalLoading(isLoading: Boolean) {
        binding.progressContainer.isVisible = isLoading
    }

    private fun setupSearchView() {
        binding.searchStub.searchViewStub.setOnClickListener {
            binding.searchStub.root.isVisible = false
            showSearch()
        }

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.search) {
                showSearch()
            }
            true
        }

        binding.searchMain.setOnCloseListener {
            binding.searchMain.isVisible = false
            binding.friendsSearchList.isVisible = false
            onQueryChanged(null)
            true
        }

        lifecycleScope.launch {
            binding.searchMain.queryTextChanges().debounce(300).collect {
                if (it.toString().trim().isNotBlank()) {
                    withContext(Dispatchers.Main) { showLocalLoading(true) }
                }
                onQueryChanged(it)
            }
        }
    }

    private fun setupToolbarSearch(isVisible: Boolean) {
        binding.toolbar.menu.findItem(R.id.search).isVisible = isVisible
        binding.toolbar.invalidateMenu()
    }

    private fun showSearch() {
        binding.emptyState.root.isVisible = false
        binding.friendsList.isVisible = false
        binding.requestsList.isVisible = false
        binding.friendsSearchList.isVisible = true
        binding.toolbar.menu.findItem(R.id.search).isVisible = false
        binding.toolbar.invalidateMenu()
        with(binding.searchMain) {
            isVisible = true
            requestFocus()
            queryHint = HtmlCompat.fromHtml("<font color = #7A8D9E>" + getString(R.string.enter_friend_name) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            isIconifiedByDefault = true
            isFocusable = true
            isIconified = false
            requestFocusFromTouch()
        }
    }

    private fun onQueryChanged(query: CharSequence?) {
        viewModel.intent.postValue(FriendsIntent.Search(query?.toString()))
    }

    private fun onTabSelected(position: Int) {
        binding.friendsList.isVisible = position == 0
        binding.requestsList.isVisible = position == 1
        viewModel.intent.postValue(FriendsIntent.TabChanged(position))
    }

    private fun createPagingAdapter() = SearchFriendsAdapter(::onActionClick, ::onCardClick)

    private fun onActionClick(item: FriendsEntity, action: FriendActions, position: Int = -1) {
        val intent = when (action) {
            FriendActions.ACCEPT -> FriendsIntent.AcceptInvite(item, position)
            FriendActions.DECLINE -> FriendsIntent.DeclineInvite(item, position)
            FriendActions.SEND_REQUEST -> FriendsIntent.RequestFriend(item, position)
        }
        viewModel.intent.value = intent
    }

    private fun onCardClick(id: String) {
        viewModel.intent.value = FriendsIntent.Details(id)
    }

    private fun setupEmptyState() {
        binding.emptyState.apply {
            icon.setImageResource(R.drawable.ic_search_for_friends)
            title.text = getString(R.string.friends_empty_state_title)
            message.text = getString(R.string.friends_empty_state_msg)
        }
    }

    private fun showPopupMessage(isRequest: Boolean) {
        val snackbar = Snackbar.make(
            binding.snackbarContainer,
            if (isRequest) R.string.friend_request_sent else R.string.friend_request_accepted,
            Snackbar.LENGTH_SHORT
        )
        val customSnackView: View = layoutInflater.inflate(com.yourfitness.common.R.layout.snackbar_cart, null)
        val text = customSnackView.findViewById<MaterialTextView>(com.yourfitness.common.R.id.message)
        text.text = getString(if (isRequest) R.string.friend_request_sent else R.string.friend_request_accepted)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(customSnackView, 0)
        snackbar.show()
    }

    private fun setupUnfriendListener() {
        setFragmentResultListener(FriendDetailsFragment.RESULT) { _, bundle ->
            val isDeleted = bundle.getBoolean(UnfriendDialog.RESULT)
            val id = bundle.getString(UnfriendDialog.ID)
            if (isDeleted && id != null) {
                viewModel.intent.postValue(FriendsIntent.FriendDeleted(id))
            } else {
                viewModel.intent.postValue(FriendsIntent.ShowSavedData)
            }

            clearFragmentResultListener(FriendDetailsFragment.RESULT)
            setupUnfriendListener()
        }
    }
}

enum class FriendActions {
    ACCEPT,
    DECLINE,
    SEND_REQUEST
}
