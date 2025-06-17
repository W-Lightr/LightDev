package lightr.data.table

import com.intellij.database.model.DasColumn
import com.intellij.database.model.DasIndex
import com.intellij.database.psi.DbDataSource
import lightr.data.GenerateContext
import lightr.interfaces.IRawDas
import lightr.interfaces.IRawDb

@Suppress("unused")
class IndexData(
    private val rawDas: DasIndex,
    private val context: GenerateContext
) : IRawDas<DasIndex>, IRawDb {
    override fun getRawDas(): DasIndex {
        return rawDas
    }

    override fun getDatasource(): DbDataSource {
        return context.datasource
    }

    fun getColumns(): List<ColumnData> {
        val list = ArrayList<ColumnData>()
        rawDas.columnsRef.resolveObjects().map { it as DasColumn }.forEach {
            list.add(ColumnData(it, context))
        }

        return list

    }

    fun hasUnique(): Boolean {
        return rawDas.isUnique
    }
}
