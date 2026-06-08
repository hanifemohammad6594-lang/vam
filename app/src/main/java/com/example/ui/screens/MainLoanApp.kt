package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InstallmentEntity
import com.example.data.LoanEntity
import com.example.data.LoanWithInstallments
import com.example.ui.LoanViewModel
import com.example.ui.components.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLoanApp(viewModel: LoanViewModel) {
    // اعمال چیدمان راست‌به‌چپ برای تطابق کامل با استانداردهای زبان فارسی
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val loansWithInsts by viewModel.loansWithInstallments.collectAsStateWithLifecycle()
        val stats by viewModel.allLoansStats.collectAsStateWithLifecycle()

        var currentTab by remember { mutableStateOf(Tab.DASHBOARD) }
        var showAddLoanDialog by remember { mutableStateOf(false) }
        var showStatsDetailDialog by remember { mutableStateOf(false) }
        var selectedLoanDetail by remember { mutableStateOf<LoanWithInstallments?>(null) }

        Scaffold(
            topBar = {
                // هدر سفارشی کاملاً منطبق بر مشخصات تم با چگالی بالا
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "خوش آمدید، علی عزیز",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "مدیریت وام‌ها",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEADDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "علی",
                            tint = Color(0xFF21005D),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFF3EDF7),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .testTag("navigation_bar")
                        .border(width = 1.dp, color = Color(0xFFE7E0EC), shape = RoundedCornerShape(0.dp))
                ) {
                    NavigationBarItem(
                        selected = currentTab == Tab.DASHBOARD,
                        onClick = { currentTab = Tab.DASHBOARD },
                        icon = { Icon(Icons.Default.Home, contentDescription = "داشبورد") },
                        label = { Text("داشبورد", fontWeight = if (currentTab == Tab.DASHBOARD) FontWeight.Bold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.LOANS,
                        onClick = { currentTab = Tab.LOANS },
                        icon = { Icon(Icons.Default.List, contentDescription = "وام‌ها") },
                        label = { Text("لیست وام‌ها", fontWeight = if (currentTab == Tab.LOANS) FontWeight.Bold else FontWeight.Medium, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8)
                        )
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddLoanDialog = true },
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("add_loan_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ثبت وام جدید",
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFFEF7FF))
            ) {
                when (currentTab) {
                    Tab.DASHBOARD -> DashboardScreen(
                        stats = stats,
                        loansWithInsts = loansWithInsts,
                        onLoanClick = { selectedLoanDetail = it },
                        onToggleInstallment = { inst, parent -> viewModel.toggleInstallmentPayment(inst, parent) },
                        onPayClick = { currentTab = Tab.LOANS },
                        onDetailClick = { showStatsDetailDialog = true },
                        onAddLoanClick = { showAddLoanDialog = true }
                    )
                    Tab.LOANS -> LoansScreen(
                        loansWithInsts = loansWithInsts,
                        viewModel = viewModel,
                        onLoanClick = { selectedLoanDetail = it },
                        onDeleteClick = { viewModel.deleteLoan(it) }
                    )
                }

                // دیالوگ جزئیات وام
                selectedLoanDetail?.let { currentDetail ->
                    // پیدا کردن اطلاعات به‌روزشده داخل لیست جاری دیتابیس
                    val updatedDetail = loansWithInsts.find { it.loan.id == currentDetail.loan.id }
                    if (updatedDetail != null) {
                        LoanDetailsDialog(
                            loanWithInst = updatedDetail,
                            onClose = { selectedLoanDetail = null },
                            onToggleInstallment = { inst ->
                                viewModel.toggleInstallmentPayment(inst, updatedDetail)
                            },
                            onPayAll = {
                                viewModel.payAllInstallments(updatedDetail)
                            }
                        )
                    } else {
                        selectedLoanDetail = null
                    }
                }

                // دیالوگ ثبت وام جدید
                if (showAddLoanDialog) {
                    AddLoanDialog(
                        onDismiss = { showAddLoanDialog = false },
                        onSave = { title, borrower, lender, amount, rate, installments, start, first, notes ->
                            viewModel.addLoan(title, borrower, lender, amount, rate, installments, start, first, notes)
                            showAddLoanDialog = false
                        }
                    )
                }

                // دیالوگ جزئیات خلاصه آمار
                if (showStatsDetailDialog) {
                    StatsDetailDialog(
                        stats = stats,
                        onDismiss = { showStatsDetailDialog = false }
                    )
                }
            }
        }
    }
}

enum class Tab { DASHBOARD, LOANS }

// ==================== بخش داشبورد ====================
@Composable
fun DashboardScreen(
    stats: LoanViewModel.Stats,
    loansWithInsts: List<LoanWithInstallments>,
    onLoanClick: (LoanWithInstallments) -> Unit,
    onToggleInstallment: (InstallmentEntity, LoanWithInstallments) -> Unit,
    onPayClick: () -> Unit,
    onDetailClick: () -> Unit,
    onAddLoanClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ۱. کارت خلاصه آمار با پیشرفت دایره‌ای زیبا
        item {
            StatsHeaderCard(
                stats = stats,
                onPayClick = onPayClick,
                onDetailClick = onDetailClick
            )
        }

        // ۲. سربرگ اقساط پیش‌رو
        item {
            Text(
                text = "اقساط سررسید نزدیک (۳۰ روز آینده)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // استخراج اقساط پرداخت نشده در ۳۰ روز آینده
        val now = System.currentTimeMillis()
        val limit30Days = now + (30L * 24 * 60 * 60 * 1000)
        val oncomingInstallments = loansWithInsts
            .flatMap { parent ->
                parent.installments.filter { !it.isPaid && it.dueDate <= limit30Days }.map { inst ->
                    inst to parent
                }
            }
            .sortedBy { it.first.dueDate }

        if (oncomingInstallments.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "هیچ قسط سررسید نزدیکی برای پرداخت وجود ندارد.",
                    icon = Icons.Default.Check
                )
            }
        } else {
            items(oncomingInstallments) { (inst, parent) ->
                UpcomingInstallmentItem(
                    installment = inst,
                    loanTitle = parent.loan.title,
                    borrower = parent.loan.borrower,
                    onPayToggle = { onToggleInstallment(inst, parent) },
                    onCardClick = { onLoanClick(parent) }
                )
            }
        }

        // ۳. دکمه درخواست ثبت وام جدید با طرح متمایز خط‌چین بنفش (منطبق بر طراحی مشخصات صادرشده)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onAddLoanClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ثبت و تعریف وام جدید",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF6750A4)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsHeaderCard(
    stats: LoanViewModel.Stats,
    onPayClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD3E3FD)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(32.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "کل بدهی باقیمانده",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001D35).copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatCurrency(stats.totalRemaining),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF001D35)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "کیف پول",
                        tint = Color(0xFF001D35),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00639B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "پرداخت اقساط",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onDetailClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        text = "جزئیات",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF001D35)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsDetailDialog(
    stats: LoanViewModel.Stats,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("متوجه شدم", fontWeight = FontWeight.Bold, color = Color(0xFF00639B))
            }
        },
        title = {
            Text(
                "جزئیات خلاصه مالی",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001D35)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(label = "کل تعهدات وام دریافت شده", value = formatCurrency(stats.totalBorrowed))
                DetailRow(label = "مجموع پرداخت شده", value = formatCurrency(stats.totalPaid))
                DetailRow(label = "مبلغ باقیمانده بدهی", value = formatCurrency(stats.totalRemaining), valueColor = Color(0xFF00639B))
                HorizontalDivider(color = Color(0xFFE7E0EC))
                DetailRow(label = "تعداد وام‌های فعال", value = convertDigitsToPersian("${stats.activeCount} وام"))
                DetailRow(label = "وام‌های تسویه شده", value = convertDigitsToPersian("${stats.completedCount} وام"))
                DetailRow(label = "اقساط فعال باقیمانده", value = convertDigitsToPersian("${stats.oncomingCount} قسط"))
                if (stats.overdueCount > 0) {
                    DetailRow(label = "تعداد اقساط معوقه", value = convertDigitsToPersian("${stats.overdueCount} قسط"), valueColor = Color(0xFFB3261E))
                }
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF49454F))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun InfoMiniCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.shadow(1.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun UpcomingInstallmentItem(
    installment: InstallmentEntity,
    loanTitle: String,
    borrower: String,
    onPayToggle: () -> Unit,
    onCardClick: () -> Unit
) {
    val isOverdue = installment.dueDate < System.currentTimeMillis()
    val bgColor = if (isOverdue) Color(0xFFF9DEDC) else Color.White
    val borderColor = Color(0xFFCAC4D0)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون وضعیت
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isOverdue) Color.White.copy(alpha = 0.5f) else Color(0xFFCCE5FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (isOverdue) Color(0xFF410E0B) else Color(0xFF004A77),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "قسط ${convertDigitsToPersian(installment.installmentNumber.toString())} از $loanTitle",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) Color(0xFF410E0B) else Color(0xFF1D1B20)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "گیرنده: $borrower",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) Color(0xFF410E0B).copy(alpha = 0.8f) else Color(0xFF49454F)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(10.dp)
                            .background(if (isOverdue) Color(0xFF410E0B).copy(alpha = 0.3f) else Color(0xFFCAC4D0))
                    )
                    Text(
                        text = getRelativeDateString(installment.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) Color(0xFFB3261E) else Color(0xFF00639B)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatCurrency(installment.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOverdue) Color(0xFF410E0B) else Color(0xFF1D1B20)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onPayToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverdue) Color(0xFFB3261E) else Color(0xFF00639B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("پرداخت شد", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== بخش لیست کامل وام‌ها ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    loansWithInsts: List<LoanWithInstallments>,
    viewModel: LoanViewModel,
    onLoanClick: (LoanWithInstallments) -> Unit,
    onDeleteClick: (LoanEntity) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf<LoanEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // فیلد جستجو به همراه فیلترها
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("جستجو در وام‌ها، طلبکار و بدهکار...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("loan_search_field"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // دکمه‌های فیلتر وضعیت
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LoanViewModel.StatusFilter.values().forEach { filter ->
                val isSelected = statusFilter == filter
                val label = when (filter) {
                    LoanViewModel.StatusFilter.ALL -> "همه وام‌ها"
                    LoanViewModel.StatusFilter.ACTIVE -> "فعال‌ها/در جریان"
                    LoanViewModel.StatusFilter.COMPLETED -> "تسویه شده‌ها"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setStatusFilter(filter) },
                    label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loansWithInsts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    message = "هیچ وامی منطبق با جستجو و فیلتر انتخابی شما پیدا نشد.",
                    icon = Icons.Default.Info
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(loansWithInsts, key = { it.loan.id }) { item ->
                    LoanItemCard(
                        loanWithInst = item,
                        onClick = { onLoanClick(item) },
                        onDeleteClick = { showDeleteConfirmDialog = item.loan }
                    )
                }
            }
        }
    }

    // دیالوگ تایید حذف وام
    showDeleteConfirmDialog?.let { loan ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(loan)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("بله، حذف کامل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("انصراف")
                }
            },
            title = { Text("آیا مطمئن هستید؟", fontWeight = FontWeight.Bold) },
            text = { Text("این کار باعث حذف کامل تمامی اطلاعات این وام و اقساط پرداخت شده و نشده مرتبط به آن خواهد شد و کار برگشت‌ناپذیر است.") }
        )
    }
}

@Composable
fun LoanItemCard(
    loanWithInst: LoanWithInstallments,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val paidCount = loanWithInst.installments.count { it.isPaid }
    val totalCount = loanWithInst.installments.size
    val remainingToPay = loanWithInst.installments.filter { !it.isPaid }.sumOf { it.amount }

    val progress = if (totalCount > 0) paidCount.toFloat() / totalCount.toFloat() else 1f
    val percentage = (progress * 100).toInt()

    val isOverdue = loanWithInst.installments.any { !it.isPaid && it.dueDate < System.currentTimeMillis() }
    val isLowInterest = loanWithInst.loan.interestRate <= 4.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ردیف اول: هدر وام با آیکون ها و بدج وضعیت
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loanWithInst.loan.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "طلبکار: ${loanWithInst.loan.lender} • بدهکار: ${loanWithInst.loan.borrower} • قسط ${convertDigitsToPersian(paidCount.toString())} از ${convertDigitsToPersian(totalCount.toString())}",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // بدج وضعیت وام
                if (loanWithInst.loan.isCompleted) {
                    BadgeLabel(text = "تسویه شده", bgColor = Color(0xFFCCE5FF), textColor = Color(0xFF004A77))
                } else if (isOverdue) {
                    BadgeLabel(text = "دیرکرد", bgColor = Color(0xFFF9DEDC), textColor = Color(0xFF410E0B))
                } else if (isLowInterest) {
                    BadgeLabel(text = "کمسود", bgColor = Color(0xFFCCE5FF), textColor = Color(0xFF004A77))
                } else {
                    BadgeLabel(text = "فعال", bgColor = Color(0xFFF7F2FA), textColor = Color(0xFF6750A4))
                }
            }

            // نوار پیشرفت تسویه
            LinearProgressIndicator(
                progress = { progress },
                trackColor = Color(0xFFE1E3E1),
                color = if (isOverdue) Color(0xFFB3261E) else Color(0xFF00639B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            // جزئیات آخری و مانده وام به همراه دکمه حذف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (loanWithInst.loan.isCompleted) "کاملاً تسویه شده" else "باقیمانده: " + formatCurrency(remainingToPay),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = convertDigitsToPersian("$percentage٪ تسویه شده"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF49454F)
                    )

                    if (!loanWithInst.loan.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف کوپن",
                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onDeleteClick() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeLabel(text: String, bgColor: Color, textColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ==================== کارت نمای تهی (Empty State) ====================
@Composable
fun EmptyStateCard(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

// ==================== دیالوگ ثبت وام جدید ====================
@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        borrower: String,
        lender: String,
        amount: Double,
        interestRate: Double,
        totalInstallments: Int,
        startDate: Long,
        firstInstallmentDate: Long,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var borrower by remember { mutableStateOf("خودم") }
    var lender by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var interestRateText by remember { mutableStateOf("0") }
    var totalInstallmentsText by remember { mutableStateOf("12") }
    var notes by remember { mutableStateOf("") }

    val context = LocalContext.current
    val today = Calendar.getInstance()
    var startDate by remember { mutableStateOf(today.timeInMillis) }

    val installmentDueTime = Calendar.getInstance().apply {
        add(Calendar.MONTH, 1)
    }
    var firstInstallmentDate by remember { mutableStateOf(installmentDueTime.timeInMillis) }

    // توابع نشان دادن دیالوگ انتخاب تاریخ پیش‌فرض اندروید
    val showStartDatePicker = {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                startDate = selected.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val showFirstInstallmentDatePicker = {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = firstInstallmentDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                firstInstallmentDate = selected.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp) // لبه‌های edge to edge را محفوظ نگه دارد
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // هدر دیالوگ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ثبت مشخصات وام جدید",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("نام یا عنوان وام (مثال: وام ازدواج)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_loan_title"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = borrower,
                                onValueChange = { borrower = it },
                                label = { Text("وام‌گیرنده (بدهکار)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_loan_borrower"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = lender,
                                onValueChange = { lender = it },
                                label = { Text("وام‌دهنده (طلبکار)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_loan_lender"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("مبلغ کل وام (تومان)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("add_loan_amount"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = interestRateText,
                                onValueChange = { interestRateText = it },
                                label = { Text("نرخ کارمزد (٪)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .testTag("add_loan_interest"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = totalInstallmentsText,
                                onValueChange = { totalInstallmentsText = it },
                                label = { Text("تعداد کل اقساط") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_loan_installments"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        // انتخاب تاریخ‌ها
                        Text(
                            "تاریخ‌گذاری تخصصی (شمسى خودکار)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showStartDatePicker() }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("تاریخ دریافت وام", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        formatJalaliDate(startDate),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showFirstInstallmentDatePicker() }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("سررسید اولین قسط", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        formatJalaliDate(firstInstallmentDate),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("یادداشت اضافی (اختیاری)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("add_loan_notes"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val isFormValid = title.isNotBlank() && amountText.toDoubleOrNull() != null

                Button(
                    onClick = {
                        val amountVal = amountText.toDoubleOrNull() ?: 0.0
                        val rateVal = interestRateText.toDoubleOrNull() ?: 0.0
                        val installmentsVal = totalInstallmentsText.toIntOrNull() ?: 12

                        onSave(
                            title,
                            borrower,
                            lender,
                            amountVal,
                            rateVal,
                            installmentsVal,
                            startDate,
                            firstInstallmentDate,
                            notes
                        )
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("add_loan_submit"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        "ثبت نهایی و تولید دفترچه اقساط",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==================== دیالوگ جزئیات کامل وام و لیست اقساط آن ====================
@Composable
fun LoanDetailsDialog(
    loanWithInst: LoanWithInstallments,
    onClose: () -> Unit,
    onToggleInstallment: (InstallmentEntity) -> Unit,
    onPayAll: () -> Unit
) {
    val completedCount = loanWithInst.installments.count { it.isPaid }
    val totalCount = loanWithInst.installments.size
    val totalToPay = loanWithInst.loan.amount + (loanWithInst.loan.amount * (loanWithInst.loan.interestRate / 100))
    val remainingToPay = loanWithInst.installments.filter { !it.isPaid }.sumOf { it.amount }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // هدر
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            loanWithInst.loan.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("بدهکار: ${loanWithInst.loan.borrower}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("•", fontSize = 12.sp, color = Color.Gray)
                            Text("طلبکار: ${loanWithInst.loan.lender}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // خلاصه مالی وام به صورت ردیفی
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("پرداخت‌شده", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(
                                    formatCurrency(loanWithInst.installments.filter { it.isPaid }.sumOf { it.amount }),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("سود کل وام", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(
                                    formatCurrency(loanWithInst.loan.amount * (loanWithInst.loan.interestRate / 100)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("مجموع کل تعهد", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(
                                    formatCurrency(totalToPay),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        if (!loanWithInst.loan.isCompleted && remainingToPay > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onPayAll,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تسویه یکجای تمام اقساط باقیمانده", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "لیست دقیق و وضعیت اقساط",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(loanWithInst.installments) { inst ->
                        InstallmentRowItem(
                            installment = inst,
                            onToggle = { onToggleInstallment(inst) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstallmentRowItem(
    installment: InstallmentEntity,
    onToggle: () -> Unit
) {
    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (installment.isPaid) Color(0xFFF8FAFC) else if (isOverdue) Color(0xFFFEF2F2) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (installment.isPaid) Color(0xFFE2E8F0) else if (isOverdue) Color(0xFFFCA5A5) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // چک باکس پرداخت
                Checkbox(
                    checked = installment.isPaid,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )

                Column {
                    Text(
                        text = "قسط شماره ${convertDigitsToPersian(installment.installmentNumber.toString())}",
                        fontWeight = FontWeight.Bold,
                        color = if (installment.isPaid) Color(0xFF64748B) else Color(0xFF1E293B)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "سررسید: " + formatJalaliDate(installment.dueDate),
                            fontSize = 11.sp,
                            color = if (isOverdue) Color(0xFFEF4444) else Color(0xFF64748B)
                        )
                        if (installment.isPaid && installment.paidDate != null) {
                            Text("•", fontSize = 11.sp, color = Color.LightGray)
                            Text(
                                text = "پرداخت شده در: " + formatJalaliDate(installment.paidDate),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (isOverdue) {
                            Text("•", fontSize = 11.sp, color = Color.LightGray)
                            Text(
                                text = "جریمه معوقه",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            Text(
                text = formatCurrency(installment.amount),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = if (installment.isPaid) Color(0xFF94A3B8) else Color(0xFF1E293B)
            )
        }
    }
}
