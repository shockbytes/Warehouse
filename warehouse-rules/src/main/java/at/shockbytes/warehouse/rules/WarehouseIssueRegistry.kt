package at.shockbytes.warehouse.rules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

class WarehouseIssueRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = listOf(WarehouseAnnotationDetector.BETA_ISSUE)
}
