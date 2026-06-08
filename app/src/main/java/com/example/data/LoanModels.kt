package com.example.data

import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,                // عنوان وام
    val borrower: String,             // وام‌گیرنده / بدهکار
    val lender: String,               // وام‌دهنده / طلبکار
    val amount: Double,               // مبلغ کل وام
    val interestRate: Double,         // درصد سود / کارمزد
    val totalInstallments: Int,       // تعداد کل اقساط
    val startDate: Long,              // تاریخ دریافت (به میلی‌ثانیه UTC)
    val installmentAmount: Double,    // مبلغ هر قسط
    val isCompleted: Boolean = false, // وضعیت تسویه شده / نشده
    val notes: String = ""            // یادداشت‌ها
)

@Entity(
    tableName = "installments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanId"])]
)
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanId: Int,                  // آیدی وام مرتبط
    val installmentNumber: Int,       // شماره قسط
    val amount: Double,               // مبلغ این قسط
    val dueDate: Long,                // تاریخ سررسید قسط
    val paidDate: Long? = null,       // تاریخ پرداخت قسط
    val isPaid: Boolean = false       // آیا پرداخت شده است؟
)

data class LoanWithInstallments(
    @Embedded val loan: LoanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "loanId"
    )
    val installments: List<InstallmentEntity>
)
