package com.example.anthar_jalawatch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.anthar_jalawatch.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeGuideScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recharge_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Simple DIY structures to recharge groundwater near your borewell.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))

            RechargeStepCard(
                stepNumber = 1,
                title = "Percolation pit",
                description = "Dig a 1×1 m pit near the borewell. Fill with pebbles and sand so rainwater soaks into the aquifer instead of running off.",
                diagramRes = R.drawable.ic_recharge_pit
            )
            RechargeStepCard(
                stepNumber = 2,
                title = "Recharge trench / channel",
                description = "Connect roof or field runoff to a shallow trench leading to the borewell zone. Line with gravel to filter silt.",
                diagramRes = R.drawable.ic_recharge_channel
            )
            RechargeStepCard(
                stepNumber = 3,
                title = "Sand filter chamber",
                description = "Pass water through a sand-and-gravel chamber before it enters the borewell annulus. Clean the filter each season.",
                diagramRes = R.drawable.ic_recharge_filter
            )

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Tip: Combine recharge with reducing pumping in summer. Community coordination works best.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RechargeStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    diagramRes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Step $stepNumber — $title",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(diagramRes),
                    contentDescription = "$title diagram",
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
