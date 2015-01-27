package org.lukosan.gradle.plugins

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import static org.junit.Assert.*

class LocalDependenciesTaskTest {
    @Test
    public void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('localDependenciesHelp', type: LocalDependenciesTask)
        assertTrue(task instanceof LocalDependenciesTask)
    }
}