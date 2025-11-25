package com.yashvant.swipe_assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.yashvant.swipe_assignment.ui.screens.AddProductBottomSheet
import com.yashvant.swipe_assignment.ui.screens.ProductListScreen
import com.yashvant.swipe_assignment.ui.theme.SwipeAssignmentTheme
import com.yashvant.swipe_assignment.ui.viewmodel.ProductViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    private val viewModel: ProductViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwipeAssignmentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwipeApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SwipeApp(viewModel: ProductViewModel) {
    var showAddProductSheet by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    ProductListScreen(
        viewModel = viewModel,
        onAddProductClick = { showAddProductSheet = true }
    )

    if (showAddProductSheet) {
        AddProductBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAddProductSheet = false },
            onSuccess = { message ->
                showAddProductSheet = false
                successMessage = message
                showSuccessDialog = true
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success!") },
            text = { Text(successMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}