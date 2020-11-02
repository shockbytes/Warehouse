package at.shockbytes.warehouse.rules

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

class WarehouseAnnotationDetector : Detector(), SourceCodeScanner {

    override fun visitConstructor(
        context: JavaContext,
        node: UCallExpression,
        constructor: PsiMethod
    ) {
        super.visitConstructor(context, node, constructor)

        if (node.classReference?.findAnnotation(BetaBox::class.qualifiedName ?: "") != null) {
            reportUsage(context, node)
        }
    }

    private fun reportUsage(context: JavaContext, node: UCallExpression) {
        context.report(
            issue = BETA_ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = true,
                includeArguments = true
            ),
            message = "Do not use Warehouse Boxes marked as @Beta."
        )
    }

    companion object {

        private val implementation = Implementation(
            WarehouseAnnotationDetector::class.java,
            Scope.JAVA_FILE_SCOPE
        )

        val BETA_ISSUE = Issue.create(
            id = "WarehouseBetaUsage",
            briefDescription = "This implementation is not stable and should be not used in production code!",
            explanation = """
                This class is still under active development,
                not stable to use and subject to breaking changes.
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.WARNING,
            implementation = implementation
        )
    }
}
