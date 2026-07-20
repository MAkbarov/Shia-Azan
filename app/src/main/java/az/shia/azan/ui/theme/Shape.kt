package az.shia.azan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 💎 Premium künc radiusları - bütün tətbiqdə ardıcıl istifadə olunur
 */
val ShiaAzanShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Xüsusi komponentlər üçün əlavə radiuslar
val CardShape = RoundedCornerShape(18.dp)
val HeroCardShape = RoundedCornerShape(28.dp)
val DialogShape = RoundedCornerShape(26.dp)
val PillShape = RoundedCornerShape(50)
