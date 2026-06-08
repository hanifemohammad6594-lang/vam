package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.InstallmentEntity
import com.example.data.LoanEntity
import com.example.data.LoanRepository
import com.example.data.LoanWithInstallments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class LoanViewModel(private val repository: LoanRepository) : ViewModel() {

    // فیلترها و جستجو
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    val statusFilter = _statusFilter.asStateFlow()

    enum class StatusFilter { ALL, ACTIVE, COMPLETED }

    // ترکیب داده‌های دیتابیس با فیلترها و جستجو
    val loansWithInstallments: StateFlow<List<LoanWithInstallments>> = combine(
        repository.loansWithInstallments,
        _searchQuery,
        _statusFilter
    ) { list, query, filter ->
        list.filter { item ->
            // فیلتر جستجو بر اساس عنوان، وام‌گیرنده یا طلبکار
            val matchesQuery = query.isEmpty() ||
                    item.loan.title.contains(query, ignoreCase = true) ||
                    item.loan.borrower.contains(query, ignoreCase = true) ||
                    item.loan.lender.contains(query, ignoreCase = true)

            // فیلتر وضعیت تسویه
            val matchesFilter = when (filter) {
                StatusFilter.ALL -> true
                StatusFilter.ACTIVE -> !item.loan.isCompleted
                StatusFilter.COMPLETED -> item.loan.isCompleted
            }

            matchesQuery && matchesFilter
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // آمار کل وام‌ها با تبدیل جریان به صورت استاندارد
    val allLoansStats: StateFlow<Stats> = repository.loansWithInstallments
        .map { list ->
            var totalBorrowed = 0.0
            var totalPaid = 0.0
            var totalRemaining = 0.0
            var oncomingInstallmentsCount = 0
            var overdueInstallmentsCount = 0

            val now = System.currentTimeMillis()

            list.forEach { item ->
                totalBorrowed += item.loan.amount
                item.installments.forEach { inst ->
                    if (inst.isPaid) {
                        totalPaid += inst.amount
                    } else {
                        totalRemaining += inst.amount
                        if (inst.dueDate < now) {
                            overdueInstallmentsCount++
                        } else {
                            oncomingInstallmentsCount++
                        }
                    }
                }
            }

            Stats(
                totalBorrowed = totalBorrowed,
                totalPaid = totalPaid,
                totalRemaining = totalRemaining,
                activeCount = list.count { !it.loan.isCompleted },
                completedCount = list.count { it.loan.isCompleted },
                oncomingCount = oncomingInstallmentsCount,
                overdueCount = overdueInstallmentsCount
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Stats()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: StatusFilter) {
        _statusFilter.value = filter
    }

    // افزودن وام جدید به همراه اقساط آن
    fun addLoan(
        title: String,
        borrower: String,
        lender: String,
        amount: Double,
        interestRate: Double,
        totalInstallments: Int,
        startDate: Long,
        firstInstallmentDate: Long,
        notes: String
    ) {
        viewModelScope.launch {
            // محاسبه بهره و مبلغ هر قسط
            val interestAmount = amount * (interestRate / 100)
            val totalToPay = amount + interestAmount
            val installmentAmount = if (totalInstallments > 0) totalToPay / totalInstallments else totalToPay

            // ساخت شیء وام
            val loan = LoanEntity(
                title = title,
                borrower = borrower,
                lender = lender,
                amount = amount,
                interestRate = interestRate,
                totalInstallments = totalInstallments,
                startDate = startDate,
                installmentAmount = installmentAmount,
                isCompleted = false,
                notes = notes
            )

            // درج وام و گرفتن آیدی تولید شده
            val loanId = repository.insertLoan(loan).toInt()

            // تولید اقساط
            val installmentsList = mutableListOf<InstallmentEntity>()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = firstInstallmentDate

            for (i in 1..totalInstallments) {
                val dueDate = calendar.timeInMillis
                installmentsList.add(
                    InstallmentEntity(
                        loanId = loanId,
                        installmentNumber = i,
                        amount = installmentAmount,
                        dueDate = dueDate,
                        isPaid = false
                    )
                )
                // اضافه کردن یک ماه برای قسط بعدی
                calendar.add(Calendar.MONTH, 1)
            }

            // در صورتی که تعداد اقساط صفر باشد، یک تعهد قسط مادر می‌سازیم
            if (totalInstallments == 0) {
                installmentsList.add(
                    InstallmentEntity(
                        loanId = loanId,
                        installmentNumber = 1,
                        amount = totalToPay,
                        dueDate = firstInstallmentDate,
                        isPaid = false
                    )
                )
            }

            repository.insertInstallments(installmentsList)
        }
    }

    // تغییر وضعیت پرداخت قسط
    fun toggleInstallmentPayment(installment: InstallmentEntity, loanWithInst: LoanWithInstallments) {
        viewModelScope.launch {
            val updatedInstallment = installment.copy(
                isPaid = !installment.isPaid,
                paidDate = if (!installment.isPaid) System.currentTimeMillis() else null
            )
            repository.updateInstallment(updatedInstallment)

            // بررسی می‌کنیم که آیا تمام اقساط این وام پرداخت شده‌اند یا خیر
            val allOtherInstallments = loanWithInst.installments.filter { it.id != installment.id }
            val areAllPaid = updatedInstallment.isPaid && allOtherInstallments.all { it.isPaid }

            val updatedLoan = loanWithInst.loan.copy(isCompleted = areAllPaid)
            repository.updateLoan(updatedLoan)
        }
    }

    // حذف کامل یک وام و اقساط آن
    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }

    // تسویه کل اقساط به صورت دستی
    fun payAllInstallments(loanWithInst: LoanWithInstallments) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            loanWithInst.installments.forEach { inst ->
                if (!inst.isPaid) {
                    repository.updateInstallment(inst.copy(isPaid = true, paidDate = now))
                }
            }
            repository.updateLoan(loanWithInst.loan.copy(isCompleted = true))
        }
    }

    data class Stats(
        val totalBorrowed: Double = 0.0,
        val totalPaid: Double = 0.0,
        val totalRemaining: Double = 0.0,
        val activeCount: Int = 0,
        val completedCount: Int = 0,
        val oncomingCount: Int = 0,
        val overdueCount: Int = 0
    )

    companion object {
        fun provideFactory(repository: LoanRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LoanViewModel::class.java)) {
                    return LoanViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
