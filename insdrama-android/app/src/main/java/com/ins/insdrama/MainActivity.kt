package com.ins.insdrama

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ins.insdrama.adapter.DramaAdapter
import com.ins.insdrama.api.ApiClient
import com.ins.insdrama.databinding.ActivityMainBinding
import com.ins.insdrama.model.Drama
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dramaAdapter: DramaAdapter
    private var dramaList: List<Drama> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadDramas()
        setupSearch()
    }

    private fun setupRecyclerView() {
        dramaAdapter = DramaAdapter(
            onItemClick = { drama -> showDetail(drama) }
        )

        binding.recyclerView.apply {
            adapter = dramaAdapter
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            
            // TikTok-style snap scrolling
            PagerSnapHelper().attachToRecyclerView(this)
            
            // Hide system UI for immersive experience
            setOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val firstVisible = layoutManager.findFirstVisibleItemPosition()
                        dramaAdapter.setCurrentPosition(firstVisible)
                    }
                }
            })
        }

        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadDramas()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun loadDramas() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.dramaApi.getDramas()
                if (response.isSuccessful) {
                    dramaList = response.body() ?: emptyList()
                    dramaAdapter.submitList(dramaList)
                    showLoading(false)
                } else {
                    showError("Gagal mengambil data: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    private fun setupSearch() {
        binding.searchButton.setOnClickListener {
            binding.searchLayout.visibility = View.VISIBLE
            binding.searchEditText.requestFocus()
        }

        binding.closeSearch.setOnClickListener {
            binding.searchLayout.visibility = View.GONE
        }

        binding.searchEditText.addTextChangedListener {
            val query = it.toString()
            filterDramas(query)
        }
    }

    private fun filterDramas(query: String) {
        if (query.isEmpty()) {
            dramaAdapter.submitList(dramaList)
        } else {
            val filtered = dramaList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.genres.any { genre -> genre.contains(query, ignoreCase = true) }
            }
            dramaAdapter.submitList(filtered)
        }
    }

    private fun showDetail(drama: Drama) {
        // Navigate to detail fragment (will be implemented)
        Toast.makeText(this, "Detail: ${drama.title}", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        showLoading(false)
    }
}
