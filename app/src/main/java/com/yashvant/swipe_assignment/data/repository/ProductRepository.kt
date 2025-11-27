package com.yashvant.swipe_assignment.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.yashvant.swipe_assignment.data.local.ProductDao
import com.yashvant.swipe_assignment.data.model.AddProductResponse
import com.yashvant.swipe_assignment.data.model.Product
import com.yashvant.swipe_assignment.data.model.Resource
import com.yashvant.swipe_assignment.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductRepository(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val context: Context
) {


    fun getProductsFromLocal(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }


    fun fetchAndSaveProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())

        try {
            if (!isInternetAvailable()) {
                emit(Resource.Error("No internet connection. Showing cached data."))
                return@flow
            }

            val products = apiService.getProducts()

            productDao.deleteAllProducts()
            productDao.insertProducts(products)

            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }


    fun addProduct(
        productName: String,
        productType: String,
        price: String,
        tax: String,
        imageFile: File? = null
    ): Flow<Resource<AddProductResponse>> = flow {
        emit(Resource.Loading())

        try {
            if (isInternetAvailable()) {
                val productNameBody = productName.toRequestBody("text/plain".toMediaTypeOrNull())
                val productTypeBody = productType.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody = price.toRequestBody("text/plain".toMediaTypeOrNull())
                val taxBody = tax.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageFile?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("files[]", it.name, requestFile)
                }

                val response = apiService.addProduct(
                    productNameBody,
                    productTypeBody,
                    priceBody,
                    taxBody,
                    imagePart
                )

                // Save to local database
                val product = Product(
                    productName = productName,
                    productType = productType,
                    price = price.toDouble(),
                    tax = tax.toDouble(),
                    image = response.productDetails?.image,
                    isSynced = true
                )
                productDao.insertProduct(product)

                emit(Resource.Success(response))
            } else {

                val product = Product(
                    productName = productName,
                    productType = productType,
                    price = price.toDouble(),
                    tax = tax.toDouble(),
                    image = imageFile?.absolutePath,
                    isSynced = false
                )
                productDao.insertProduct(product)

                val offlineResponse = AddProductResponse(
                    success = true,
                    message = "Product saved offline. Will sync when internet is available.",
                    productId = null,
                    productDetails = product
                )
                emit(Resource.Success(offlineResponse))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to add product"))
        }
    }


    fun syncUnsyncedProducts(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())

        try {
            if (!isInternetAvailable()) {
                emit(Resource.Error("No internet connection"))
                return@flow
            }

            val unsyncedProducts = productDao.getUnsyncedProducts()
            var syncedCount = 0

            for (product in unsyncedProducts) {
                try {
                    val productNameBody = product.productName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val productTypeBody = product.productType.toRequestBody("text/plain".toMediaTypeOrNull())
                    val priceBody = product.price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val taxBody = product.tax.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                    apiService.addProduct(
                        productNameBody,
                        productTypeBody,
                        priceBody,
                        taxBody,
                        null
                    )

                    productDao.updateProduct(product.copy(isSynced = true))
                    syncedCount++
                } catch (e: Exception) {
                    continue
                }
            }

            emit(Resource.Success(syncedCount))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to sync products"))
        }
    }


    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }


    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

