/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.databinding.ActivityPodcastBinding
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {

  private lateinit var binding: ActivityPodcastBinding
  private val searchViewModel by viewModels<SearchViewModel>()
  private lateinit var podcastListAdapter: PodcastListAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPodcastBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setupToolbar()
    setupViewModels()
    updateControls()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // 1
    val inflater = menuInflater
    inflater.inflate(R.menu.menu_search, menu)
    // 2
    val searchMenuItem = menu.findItem(R.id.search_item)
    val searchView = searchMenuItem?.actionView as SearchView
    // 3
    val searchManager = getSystemService(Context.SEARCH_SERVICE)
            as SearchManager
    // 4

    searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    return true
  }

  override fun onShowDetails(
    podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData
  ) {
    // Not implemented yet
  }

  private fun setupViewModels() {
    val service = ItunesService.instance
    searchViewModel.iTunesRepo = ItunesRepo(service)
  }

  private fun performSearch(term: String) {
    showProgressBar()
    GlobalScope.launch {
      val results = searchViewModel.searchPodcasts(term)
      withContext(Dispatchers.Main) {
        hideProgressBar()
        databinding.toolbar.title = term
        podcastListAdapter.setSearchData(results)
      }
    }
  }

  private fun handleIntent(intent: Intent) {
    if (Intent.ACTION_SEARCH == intent.action) {
      val query = intent.getStringExtra(SearchManager.QUERY) ?:
      return
      performSearch(query)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun setupToolbar() {
    setSupportActionBar(binding.toolbar)
  }

  private fun updateControls() {
    databinding.podcastRecyclerView.setHasFixedSize(true)
    val layoutManager = LinearLayoutManager(this)
    databinding.podcastRecyclerView.layoutManager = layoutManager
    val dividerItemDecoration = DividerItemDecoration(
      databinding.podcastRecyclerView.context,
      layoutManager.orientation)

    databinding.podcastRecyclerView.addItemDecoration(dividerItemDecoration)
    podcastListAdapter = PodcastListAdapter(null, this, this)
    databinding.podcastRecyclerView.adapter = podcastListAdapter
  }

  private fun showProgressBar() {
    databinding.progressBar.visibility = View.VISIBLE
  }
  private fun hideProgressBar() {
    databinding.progressBar.visibility = View.INVISIBLE
  }
}