package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.GoldenCaramel
import com.example.ui.theme.HoneyButter
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.PistachioGreen
import com.example.ui.theme.RoseContainer
import com.example.ui.viewmodel.CakeLoversViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: CakeLoversViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToCatalog: () -> Unit,
    onNavigateToOrders: () -> Unit = onNavigateToCatalog,
    onNavigateToAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val userOrders by viewModel.userOrders.collectAsStateWithLifecycle()
    val authInitialTab by viewModel.authInitialTab.collectAsStateWithLifecycle()
    val recentGuestOrderId by viewModel.recentGuestOrderId.collectAsStateWithLifecycle()
    val pendingCheckoutAfterAuth by viewModel.pendingCheckoutAfterAuth.collectAsStateWithLifecycle()
    val guestName by viewModel.guestName.collectAsStateWithLifecycle()
    val guestPhone by viewModel.guestPhone.collectAsStateWithLifecycle()
    val guestAddress by viewModel.guestAddress.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isLoggedIn, isAdmin) {
        if (isLoggedIn) {
            if (isAdmin) {
                onNavigateToAdmin()
            } else {
                onNavigateToCatalog()
            }
        }
    }

    if (isLoggedIn) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = BerryBurgundy,
                modifier = Modifier.size(36.dp)
            )
        }
        return
    }

    var selectedTab by remember(authInitialTab) { mutableIntStateOf(authInitialTab) }

    // Sign In inputs
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    // Sign Up inputs - auto-fill from guest order if available
    var signUpName by remember(guestName) { mutableStateOf(guestName) }
    var signUpPhone by remember(guestPhone) { mutableStateOf(guestPhone) }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var signUpAddress by remember(guestAddress) { mutableStateOf(guestAddress) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Confectionery Logo & Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(RoseContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎂",
                        fontSize = 36.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cake Lovers",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BerryBurgundy
                    )
                )
                Text(
                    text = "Artisanal Confectionery & Multi-Tier Studio",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }



        // Pending Checkout Gate Notice Banner (When redirected from Cart because guest cannot order without signing up)
        if (pendingCheckoutAfterAuth && !isLoggedIn) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_checkout_gate_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = HoneyButter),
                    border = CardDefaults.outlinedCardBorder(true)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(BerryBurgundy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🛒", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Complete Sign-Up to Place Order",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BerryBurgundy
                                    )
                                )
                                Text(
                                    text = "Your confectionery items are saved in your cart!",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = DarkAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Guest users must create an account or sign in before their order is confirmed. Once you sign in or register below, your order will be placed immediately and live oven tracking will begin!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkAmber.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // Guest Order Sign-Up Notice Banner
        if (recentGuestOrderId != null && !isLoggedIn) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_order_signup_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = HoneyButter),
                    border = CardDefaults.outlinedCardBorder(true)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DarkAmber),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎂", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Order Placed as Guest! 🎉",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkAmber
                                    )
                                )
                                Text(
                                    text = "Order #$recentGuestOrderId is in kitchen queue",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = DarkAmber.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Please complete your Sign Up below to link your order and track live 5-stage kitchen preparation.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkAmber.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    viewModel.setRecentGuestOrderId(null)
                                    onNavigateToOrders()
                                },
                                modifier = Modifier.testTag("skip_to_tracking_button")
                            ) {
                                Text(
                                    text = "Skip Sign Up & Track Order →",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkAmber
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Authentication Form Tabs: Sign In / Create Account
        item {
            Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_form_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(true)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            indicator = { tabPositions ->
                                SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = BerryBurgundy
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "Sign In",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 0) BerryBurgundy else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.testTag("tab_sign_in")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        text = "Create Account",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 1) BerryBurgundy else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.testTag("tab_create_account")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (selectedTab == 0) {
                            // SIGN IN TAB
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (authErrorMessage != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = authErrorMessage ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = loginIdentifier,
                                    onValueChange = {
                                        loginIdentifier = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Email or Phone Number") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_identifier_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BerryBurgundy,
                                        focusedLabelColor = BerryBurgundy
                                    )
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Password or 4-digit PIN") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BerryBurgundy,
                                        focusedLabelColor = BerryBurgundy
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            loginIdentifier = "pooja.sharma@example.com"
                                            loginPassword = "password123"
                                            authErrorMessage = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = "Customer Demo", style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            loginIdentifier = "admin@cakelovers.in"
                                            loginPassword = "admin123"
                                            authErrorMessage = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = "Admin Demo", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                                            authErrorMessage = "Please enter both your email/phone and password"
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Please enter both your email/phone and password")
                                            }
                                        } else {
                                            authErrorMessage = null
                                            val hadGuestOrder = recentGuestOrderId != null
                                            val hadPendingCheckout = pendingCheckoutAfterAuth
                                            viewModel.loginWithCredentials(
                                                identifier = loginIdentifier,
                                                passwordOrPin = loginPassword,
                                                onSuccess = {
                                                    authErrorMessage = null
                                                    if (hadPendingCheckout) {
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("Welcome back! Order is being placed.")
                                                        }
                                                        viewModel.placeOrder {
                                                            onNavigateToOrders()
                                                        }
                                                    } else if (hadGuestOrder) {
                                                        viewModel.setRecentGuestOrderId(null)
                                                        onNavigateToOrders()
                                                    } else if (loginIdentifier.contains("admin", ignoreCase = true) || viewModel.isAdmin.value) {
                                                        onNavigateToAdmin()
                                                    } else {
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("Signed in successfully as ${viewModel.userProfile.value?.name ?: "User"}")
                                                        }
                                                        onNavigateToCatalog()
                                                    }
                                                },
                                                onError = { error ->
                                                    authErrorMessage = error
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(error)
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("sign_in_submit_button")
                                ) {
                                     Icon(
                                         imageVector = Icons.Default.VpnKey,
                                         contentDescription = null,
                                         tint = Color.White
                                     )
                                     Spacer(modifier = Modifier.width(8.dp))
                                     Text(
                                         text = if (pendingCheckoutAfterAuth) "Sign In & Place Order 🎂" else "Sign In & Continue",
                                         fontWeight = FontWeight.Bold,
                                         color = Color.White
                                     )
                                 }
                             }
                         } else {
                             // CREATE ACCOUNT TAB
                             Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                 if (authErrorMessage != null) {
                                     Surface(
                                         color = MaterialTheme.colorScheme.errorContainer,
                                         shape = RoundedCornerShape(10.dp),
                                         modifier = Modifier.fillMaxWidth()
                                     ) {
                                         Row(
                                             modifier = Modifier.padding(12.dp),
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Icon(
                                                 imageVector = Icons.Default.Lock,
                                                 contentDescription = null,
                                                 tint = MaterialTheme.colorScheme.error
                                             )
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Text(
                                                 text = authErrorMessage ?: "",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = MaterialTheme.colorScheme.onErrorContainer
                                             )
                                         }
                                     }
                                 }

                                 OutlinedTextField(
                                     value = signUpName,
                                     onValueChange = {
                                         signUpName = it
                                         authErrorMessage = null
                                     },
                                     label = { Text("Full Name") },
                                     leadingIcon = {
                                         Icon(
                                             imageVector = Icons.Default.Person,
                                             contentDescription = null,
                                             tint = MaterialTheme.colorScheme.primary
                                         )
                                     },
                                     singleLine = true,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .testTag("signup_name_input"),
                                     shape = RoundedCornerShape(12.dp),
                                     colors = OutlinedTextFieldDefaults.colors(
                                         focusedBorderColor = BerryBurgundy,
                                         focusedLabelColor = BerryBurgundy
                                     )
                                 )

                                 OutlinedTextField(
                                     value = signUpPhone,
                                     onValueChange = {
                                         signUpPhone = it
                                         authErrorMessage = null
                                     },
                                     label = { Text("Phone Number") },
                                     leadingIcon = {
                                         Icon(
                                             imageVector = Icons.Default.Phone,
                                             contentDescription = null,
                                             tint = MaterialTheme.colorScheme.primary
                                         )
                                     },
                                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                     singleLine = true,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .testTag("signup_phone_input"),
                                     shape = RoundedCornerShape(12.dp),
                                     colors = OutlinedTextFieldDefaults.colors(
                                         focusedBorderColor = BerryBurgundy,
                                         focusedLabelColor = BerryBurgundy
                                     )
                                 )

                                 OutlinedTextField(
                                     value = signUpEmail,
                                     onValueChange = {
                                         signUpEmail = it
                                         authErrorMessage = null
                                     },
                                     label = { Text("Email Address") },
                                     leadingIcon = {
                                         Icon(
                                             imageVector = Icons.Default.Email,
                                             contentDescription = null,
                                             tint = MaterialTheme.colorScheme.primary
                                         )
                                     },
                                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                     singleLine = true,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .testTag("signup_email_input"),
                                     shape = RoundedCornerShape(12.dp),
                                     colors = OutlinedTextFieldDefaults.colors(
                                         focusedBorderColor = BerryBurgundy,
                                         focusedLabelColor = BerryBurgundy
                                     )
                                 )

                                 OutlinedTextField(
                                     value = signUpPassword,
                                     onValueChange = {
                                         signUpPassword = it
                                         authErrorMessage = null
                                     },
                                     label = { Text("Password (min 4 characters)") },
                                     leadingIcon = {
                                         Icon(
                                             imageVector = Icons.Default.Lock,
                                             contentDescription = null,
                                             tint = MaterialTheme.colorScheme.primary
                                         )
                                     },
                                     trailingIcon = {
                                         IconButton(onClick = { signUpPasswordVisible = !signUpPasswordVisible }) {
                                             Icon(
                                                 imageVector = if (signUpPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                 contentDescription = if (signUpPasswordVisible) "Hide password" else "Show password"
                                             )
                                         }
                                     },
                                     visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                     singleLine = true,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .testTag("signup_password_input"),
                                     shape = RoundedCornerShape(12.dp),
                                     colors = OutlinedTextFieldDefaults.colors(
                                         focusedBorderColor = BerryBurgundy,
                                         focusedLabelColor = BerryBurgundy
                                     )
                                 )

                                 OutlinedTextField(
                                     value = signUpAddress,
                                     onValueChange = {
                                         signUpAddress = it
                                         authErrorMessage = null
                                     },
                                     label = { Text("Primary Delivery Address") },
                                     leadingIcon = {
                                         Icon(
                                             imageVector = Icons.Default.Home,
                                             contentDescription = null,
                                             tint = MaterialTheme.colorScheme.primary
                                         )
                                     },
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .testTag("signup_address_input"),
                                     shape = RoundedCornerShape(12.dp),
                                     colors = OutlinedTextFieldDefaults.colors(
                                         focusedBorderColor = BerryBurgundy,
                                         focusedLabelColor = BerryBurgundy
                                     )
                                 )

                                 Spacer(modifier = Modifier.height(4.dp))

                                 Button(
                                     onClick = {
                                         if (signUpName.isBlank()) {
                                             authErrorMessage = "Please enter your full name"
                                             coroutineScope.launch {
                                                 snackbarHostState.showSnackbar("Please enter your full name")
                                             }
                                         } else if (signUpPhone.isBlank() && signUpEmail.isBlank()) {
                                             authErrorMessage = "Please enter either an email or phone number"
                                             coroutineScope.launch {
                                                 snackbarHostState.showSnackbar("Please enter either an email or phone number")
                                             }
                                         } else if (signUpPassword.length < 4) {
                                             authErrorMessage = "Password must be at least 4 characters long"
                                             coroutineScope.launch {
                                                 snackbarHostState.showSnackbar("Password must be at least 4 characters long")
                                             }
                                         } else {
                                             val hadGuestOrder = recentGuestOrderId != null
                                             val hadPendingCheckout = pendingCheckoutAfterAuth
                                             viewModel.registerNewUser(
                                                 name = signUpName,
                                                 email = signUpEmail.ifBlank { "$signUpPhone@cakelovers.in" },
                                                 phone = signUpPhone.ifBlank { "+91 98765 43210" },
                                                 address = signUpAddress.ifBlank { "Bengaluru Delivery Address" },
                                                 password = signUpPassword,
                                                 onSuccess = {
                                                     authErrorMessage = null
                                                     if (hadPendingCheckout) {
                                                         coroutineScope.launch {
                                                             snackbarHostState.showSnackbar("Account created! Placing your order.")
                                                         }
                                                         viewModel.placeOrder {
                                                             onNavigateToOrders()
                                                         }
                                                     } else if (hadGuestOrder) {
                                                         onNavigateToOrders()
                                                     } else {
                                                         coroutineScope.launch {
                                                             snackbarHostState.showSnackbar("Welcome to Cake Lovers, $signUpName!")
                                                         }
                                                         onNavigateToCatalog()
                                                     }
                                                 },
                                                 onError = { error ->
                                                     authErrorMessage = error
                                                     coroutineScope.launch {
                                                         snackbarHostState.showSnackbar(error)
                                                     }
                                                 }
                                             )
                                         }
                                     },
                                     shape = RoundedCornerShape(12.dp),
                                     colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .height(50.dp)
                                         .testTag("signup_submit_button")
                                 ) {
                                     Text(
                                         text = if (pendingCheckoutAfterAuth) "Create Account & Confirm Order 🎂" else if (recentGuestOrderId != null) "Complete Sign Up & Track Order 🎂" else "Create Account & Sign In",
                                         fontWeight = FontWeight.Bold,
                                         color = Color.White
                                     )
                                 }
                             }
                         }
                     }
                 }
             }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
