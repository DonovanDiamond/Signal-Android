package org.thoughtcrime.securesms.keyboard.emoji.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.emoji.EmojiKeyboardProvider
import org.thoughtcrime.securesms.components.emoji.EmojiPageView
import org.thoughtcrime.securesms.components.emoji.EmojiPageViewGridAdapter
import org.thoughtcrime.securesms.keyboard.emoji.KeyboardPageSearchView
import org.thoughtcrime.securesms.keyboard.findListener
import org.thoughtcrime.securesms.util.ViewUtil

class EmojiSearchFragment : Fragment(R.layout.emoji_search_fragment), EmojiPageViewGridAdapter.VariationSelectorListener {

  private lateinit var viewModel: EmojiSearchViewModel
  private lateinit var callback: Callback

  override fun onAttach(context: Context) {
    super.onAttach(context)

    callback = context as Callback
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val repository = EmojiSearchRepository(requireContext())
    val factory = EmojiSearchViewModel.Factory(repository)

    viewModel = ViewModelProviders.of(this, factory)[EmojiSearchViewModel::class.java]

    val eventListener: EmojiKeyboardProvider.EmojiEventListener = requireNotNull(findListener())
    val searchBar: KeyboardPageSearchView = view.findViewById(R.id.emoji_search_view)
    val resultsContainer: FrameLayout = view.findViewById(R.id.emoji_search_results_container)
    val noResults: TextView = view.findViewById(R.id.emoji_search_empty)
    val emojiPageView = EmojiPageView(requireContext(), eventListener, this, true, null, LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false), R.layout.emoji_search_result_display_item)

    resultsContainer.addView(emojiPageView)

    searchBar.presentForEmojiSearch()
    searchBar.callbacks = SearchCallbacks()

    viewModel.pageModel.observe(viewLifecycleOwner) { pageModel ->
      emojiPageView.setModel(pageModel)

      if (pageModel.emoji.isNotEmpty() || pageModel.iconAttr == R.attr.emoji_category_recent) {
        emojiPageView.visibility = View.VISIBLE
        noResults.visibility = View.GONE
      } else {
        emojiPageView.visibility = View.INVISIBLE
        noResults.visibility = View.VISIBLE
      }
    }
  }

  private inner class SearchCallbacks : KeyboardPageSearchView.Callbacks {
    override fun onNavigationClicked() {
      ViewUtil.hideKeyboard(requireContext(), requireView())
      callback.closeEmojiSearch()
    }

    override fun onQueryChanged(query: String) {
      viewModel.onQueryChanged(query)
    }
  }

  interface Callback {
    fun closeEmojiSearch()
  }

  override fun onVariationSelectorStateChanged(open: Boolean) = Unit
}
