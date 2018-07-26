package com.renny.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

public class InjectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin("com.android.application")
                || project.plugins.hasPlugin("com.android.library")
                || project.plugins.hasPlugin("java-library")) {

            project.dependencies {
                implementation 'com.renny.initiator:engine:1.0.6'
                implementation 'com.renny.initiator:annotation:1.0.8'
            }
        }

        if (project.plugins.hasPlugin("com.android.application")) {
            project.android.registerTransform(new InitiatorTransform(project))
        }
    }

}