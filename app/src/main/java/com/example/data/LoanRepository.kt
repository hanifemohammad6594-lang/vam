package com.example.data

import kotlinx.coroutines.flow.Flow

class LoanRepository(private val loanDao: LoanDao) {
    val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()
    val loansWithInstallments: Flow<List<LoanWithInstallments>> = loanDao.getLoansWithInstallments()
    val allInstallments: Flow<List<InstallmentEntity>> = loanDao.getAllInstallments()

    fun getInstallmentsForLoan(loanId: Int): Flow<List<InstallmentEntity>> {
        return loanDao.getInstallmentsForLoan(loanId)
    }

    suspend fun insertLoan(loan: LoanEntity): Long {
        return loanDao.insertLoan(loan)
    }

    suspend fun insertInstallments(installments: List<InstallmentEntity>) {
        loanDao.insertInstallments(installments)
    }

    suspend fun updateLoan(loan: LoanEntity) {
        loanDao.updateLoan(loan)
    }

    suspend fun updateInstallment(installment: InstallmentEntity) {
        loanDao.updateInstallment(installment)
    }

    suspend fun deleteLoan(loan: LoanEntity) {
        loanDao.deleteLoan(loan)
    }
}
