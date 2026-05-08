package com.shadowascent.core;

import com.shadowascent.core.world.sections.SectionTemplateLibrary;

/**
 * CLI diagnostics for early worldgen migration slices.
 */
public final class WorldgenDiagnostics {
    private WorldgenDiagnostics() {
    }

    public static void main(String[] args) {
        SectionTemplateLibrary library = SectionTemplateLibrary.loadDefault();
        System.out.println("=== Shadow Ascent Worldgen Diagnostics ===");
        System.out.println("Section templates loaded: " + library.templates().size());
        System.out.println("Section template validation issues: " + library.validationIssues().size());
        if (!library.validationIssues().isEmpty()) {
            library.validationIssues().stream().limit(20)
                    .forEach(issue -> System.out.println("- " + issue.file() + " :: " + issue.field()
                            + " :: " + issue.kind() + " :: " + issue.message()));
        }
    }
}
