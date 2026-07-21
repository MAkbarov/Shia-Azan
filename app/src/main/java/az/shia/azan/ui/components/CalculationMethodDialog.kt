package az.shia.azan.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.shia.azan.data.CalculationMethod

/**
 * Hesablama metodu seçimi dialoqu
 */
@Composable
fun CalculationMethodDialog(
    currentMethod: CalculationMethod,
    onSelect: (CalculationMethod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hesablama Metodu") },
        text = {
            LazyColumn {
                items(CalculationMethod.values()) { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(method) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = method == currentMethod,
                            onClick = { onSelect(method) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = method.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (method == currentMethod) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "Sübh: ${method.fajrAngle}°, Məğrib: ${method.maghribAngle}°, İşa: ${method.ishaAngle}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Bağla")
            }
        }
    )
}
