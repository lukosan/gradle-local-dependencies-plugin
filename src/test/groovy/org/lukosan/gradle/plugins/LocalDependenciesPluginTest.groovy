package org.lukosan.gradle.plugins

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import static org.junit.Assert.*

class LocalDepdendenciesPluginTest {
    @Test
    public void localDependenciesPluginAddsLocalDependenciesTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'org.lukosan.localDependencies'

        assertTrue(project.tasks.localDependenciesHelp instanceof LocalDependenciesTask)
    }
}