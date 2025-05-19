package SongsMain.Classes

data class ModifiedItem <T>(val typeOfUpdate: TypeOfUpdate, val item:T){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModifiedItem<*>

        return item == other.item
    }

    override fun hashCode(): Int {
        return item?.hashCode() ?: 0
    }
}