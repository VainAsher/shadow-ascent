package com.shadowascent.core.world.validation;

import com.shadowascent.core.world.progression.ProgressionValidationResult;
import com.shadowascent.core.world.progression.ProgressionValidator;
import com.shadowascent.core.world.progression.WorldProgressionGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Wave 1 starter planner: progression-level validation and bounded repair hints.
 *
 * Later migration slices will extend this planner with layout/socket/anchor inputs.
 */
public final class GenerationValidationPlanner {
    private GenerationValidationPlanner() {
    }

    public static GenerationValidationReport validate(WorldProgressionGraph graph) {
        List<GenerationValidationReport.Issue> issues = new ArrayList<>();
        List<GenerationValidationReport.RepairAction> repairs = new ArrayList<>();

        ProgressionValidationResult progression = ProgressionValidator.validate(graph);
        if (!progression.valid()) {
            for (String nodeId : progression.blockedNodeIds()) {
                issues.add(new GenerationValidationReport.Issue(
                        "blocked_progression_node",
                        nodeId,
                        "error",
                        "required progression node is not reachable"));
                repairs.add(new GenerationValidationReport.RepairAction(
                        "regenerate",
                        "regenerate_progression_branch",
                        nodeId,
                        "node requirements are not satisfied by reachable grants"));
            }
        }

        boolean valid = progression.valid();
        return new GenerationValidationReport(
                valid,
                progression.valid(),
                0,
                issues,
                repairs);
    }
}
