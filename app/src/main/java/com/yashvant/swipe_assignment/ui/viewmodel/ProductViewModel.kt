package com.yashvant.swipe_assignment.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashvant.swipe_assignment.data.model.AddProductResponse
import com.yashvant.swipe_assignment.data.model.Product
import com.yashvant.swipe_assignment.data.model.Resource
import com.yashvant.swipe_assignment.data.repository.ProductRepository
import com.yashvant.swipe_assignment.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File


class ProductViewModel(
    private val repository: ProductRepository,
    private val networkObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val productsState: StateFlow<Resource<List<Product>>> = _productsState.asStateFlow()

    private val _addProductState = MutableStateFlow<Resource<AddProductResponse>?>(null)
    val addProductState: StateFlow<Resource<AddProductResponse>?> = _addProductState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _localProducts = MutableStateFlow<List<Product>>(emptyList())
    val localProducts: StateFlow<List<Product>> = _localProducts.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        loadProducts()
        observeLocalProducts()
        observeNetworkConnectivity()
    }


    private fun observeNetworkConnectivity() {
        viewModelScope.launch {
            networkObserver.observe().collect { connected ->
                _isConnected.value = connected
                if (connected) {
                    syncUnsyncedProducts()
                }
            }
        }
    }


    fun loadProducts() {
        viewModelScope.launch {
            repository.fetchAndSaveProducts().collect { resource ->
                _productsState.value = resource
            }
        }
    }

    private fun observeLocalProducts() {
        viewModelScope.launch {
            repository.getProductsFromLocal().collect { products ->
                _localProducts.value = products
            }
        }
    }


    fun addProduct(
        productName: String,
        productType: String,
        price: String,
        tax: String,
        imageFile: File? = null
    ) {
        viewModelScope.launch {
            repository.addProduct(productName, productType, price, tax, imageFile).collect { resource ->
                _addProductState.value = resource
            }
        }
    }


    fun resetAddProductState() {
        _addProductState.value = null
    }


    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchProducts(query)
    }


    private fun searchProducts(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                repository.getProductsFromLocal().collect { products ->
                    _localProducts.value = products
                }
            } else {
                repository.searchProducts(query).collect { products ->
                    _localProducts.value = products
                }
            }
        }
    }


    fun syncUnsyncedProducts() {
        viewModelScope.launch {
            repository.syncUnsyncedProducts().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        if (resource.data != null && resource.data > 0) {
                            loadProducts()
                        }
                    }
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }
}
