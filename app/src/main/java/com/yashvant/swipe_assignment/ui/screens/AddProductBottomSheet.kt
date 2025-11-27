package com.yashvant.swipe_assignment.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yashvant.swipe_assignment.data.model.Resource
import com.yashvant.swipe_assignment.ui.viewmodel.ProductViewModel
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductBottomSheet(
    viewModel: ProductViewModel,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    var productName by remember { mutableStateOf("") }
    var productType by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var tax by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }

    var showErrors by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val productTypes = listOf("Product", "Service")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.cacheDir, "product_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                selectedImageFile = file
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val addProductState by viewModel.addProductState.collectAsState()

    LaunchedEffect(addProductState) {
        when (val state = addProductState) {
            is Resource.Success -> {
                onSuccess(state.data?.message ?: "Product added successfully!")
                viewModel.resetAddProductState()
            }
            is Resource.Error -> {
                // Error is handled in the UI
            }
            else -> {}
        }
    }


    fun isFormValid(): Boolean {
        return productName.isNotBlank() &&
                productType.isNotBlank() &&
                price.isNotBlank() && price.toDoubleOrNull() != null &&
                tax.isNotBlank() && tax.toDoubleOrNull() != null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add New Product",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Product Type *",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = productType,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select product type") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = showErrors && productType.isBlank()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    productTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                productType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (showErrors && productType.isBlank()) {
                Text(
                    text = "Please select a product type",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Product Name *",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                placeholder = { Text("Enter product name") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && productName.isBlank()
            )
            if (showErrors && productName.isBlank()) {
                Text(
                    text = "Product name is required",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selling Price *",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                placeholder = { Text("Enter selling price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && (price.isBlank() || price.toDoubleOrNull() == null)
            )
            if (showErrors && (price.isBlank() || price.toDoubleOrNull() == null)) {
                Text(
                    text = "Please enter a valid price",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tax Rate
            Text(
                text = "Tax Rate (%) *",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = tax,
                onValueChange = { tax = it },
                placeholder = { Text("Enter tax rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && (tax.isBlank() || tax.toDoubleOrNull() == null)
            )
            if (showErrors && (tax.isBlank() || tax.toDoubleOrNull() == null)) {
                Text(
                    text = "Please enter a valid tax rate",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Product Image (Optional)",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            selectedImageUri = null
                            selectedImageFile = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.Close, "Remove image", tint = Color.White)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Image (JPEG/PNG)")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (addProductState is Resource.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (addProductState as Resource.Error).message ?: "An error occurred",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    showErrors = true
                    if (isFormValid()) {
                        viewModel.addProduct(
                            productName = productName,
                            productType = productType,
                            price = price,
                            tax = tax,
                            imageFile = selectedImageFile
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = addProductState !is Resource.Loading
            ) {
                if (addProductState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Product", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
