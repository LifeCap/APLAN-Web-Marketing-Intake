package com.aplan.shoealerts.data.database

import androidx.room.TypeConverter
import com.aplan.shoealerts.data.model.AlertType
import com.aplan.shoealerts.data.model.ShoeStyle
import com.aplan.shoealerts.data.model.ShoeMaterial
import com.aplan.shoealerts.data.model.ShoppingSource

class Converters {
    @TypeConverter fun fromShoppingSource(v: ShoppingSource): String = v.name
    @TypeConverter fun toShoppingSource(v: String): ShoppingSource = ShoppingSource.valueOf(v)

    @TypeConverter fun fromShoeStyle(v: ShoeStyle): String = v.name
    @TypeConverter fun toShoeStyle(v: String): ShoeStyle = ShoeStyle.valueOf(v)

    @TypeConverter fun fromShoeMaterial(v: ShoeMaterial): String = v.name
    @TypeConverter fun toShoeMaterial(v: String): ShoeMaterial = ShoeMaterial.valueOf(v)

    @TypeConverter fun fromAlertType(v: AlertType): String = v.name
    @TypeConverter fun toAlertType(v: String): AlertType = AlertType.valueOf(v)
}
