package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.LoanRepository
import com.example.ui.LoanViewModel
import com.example.ui.screens.MainLoanApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // راه‌اندازی سراسری دیتابیس، ریپازیتوری و ویومدل محلی
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LoanRepository(database.loanDao())
        val viewModel: LoanViewModel by viewModels {
            LoanViewModel.provideFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainLoanApp(viewModel = viewModel)
                }
            }
        }
    }
}
