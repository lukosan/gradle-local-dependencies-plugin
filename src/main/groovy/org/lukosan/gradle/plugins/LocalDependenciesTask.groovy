package org.lukosan.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class LocalDependenciesTask extends DefaultTask {

    @TaskAction
    def localDependenciesHelp() {
        println "This task does nothing but this LocalDependenciesPlugin will hook itself in to run before the compileJava task"
        println "Example build.gradle configuration: "
        println "    localDependencies { "
		println "        compile = ['../model:domain-model:0.1.0'] "
		println "        runtime = [ "
		println "            'org.hibernate:hibernate-core:4.3.8.Final', "
		println "            'org.hibernate:hibernate-validator:5.1.3.Final' "
		println "        ] "
		println "    } "
    }
}