package lightr

import com.intellij.database.model.DasObject
import com.intellij.database.model.ObjectKind
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import lightr.util.DasUtil
import lightr.ui.dialog.GenerateConfigDialog
import java.io.PrintWriter
import java.io.StringWriter



class LightDevPluginsAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun update(e: AnActionEvent) {
        val dasObjectStream = DasUtil.extractDatabaseDas(e.dataContext)

        val b = dasObjectStream.anyMatch { x: DasObject -> SUPPORTED_KINDS.contains(x.kind) }
        e.presentation.isEnabledAndVisible = b
        super.update(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        try {

        val tablelightr = GenerateConfigDialog(e)

        tablelightr.title = "生成"
        tablelightr.setSize(800, 600)

        tablelightr.show()
        } catch (e: Exception) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            val fullStackTrace = sw.toString()

            Messages.showErrorDialog(e.message + "\n" + fullStackTrace, "Error")
        }
    }

    companion object {
        private val SUPPORTED_KINDS: Set<ObjectKind> = mutableSetOf(
            ObjectKind.TABLE,
            ObjectKind.VIEW,
            ObjectKind.SCHEMA,
            ObjectKind.DATABASE
        )
    }
}
