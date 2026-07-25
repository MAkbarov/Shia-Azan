package az.shia.azan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Premium künc radiusları - bütün tətbiqdə ardıcıl istifadə olunur.
 * Daha yumşaq, müasir "squircle" görünüşü üçün radiuslar bir qədər genişdir.
 */
val ShiaAzanShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Xüsusi komponentlər üçün əlavə radiuslar
val CardShape = RoundedCornerShape(22.dp)
val HeroCardShape = RoundedCornerShape(32.dp)
val DialogShape = RoundedCornerShape(28.dp)
val PillShape = RoundedCornerShape(50)
