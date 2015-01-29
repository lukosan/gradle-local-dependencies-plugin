package org.lukosan.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.GradleBuild

class LocalDependenciesPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.task('localDependenciesHelp', type: LocalDependenciesTask)
		project.extensions.create('localDependencies', LocalDependenciesPluginExtension)
		
		project.afterEvaluate {
			project.localDependencies.compile.eachWithIndex() { obj, i -> 
				def dependency = obj.split(":")
				println ' + Registering local dependent gradle project'
				println ' |--- build file : ' + dependency[0] + '/build.gradle'
				println ' |--- jar file   : ' + dependency[0] + '/build/libs/' + dependency[1] + '-' + dependency[2] + '.jar'
			
				project.task('buildDepJar'+i, type:GradleBuild) {
        			buildFile = new File(dependency[0] + '/build.gradle')
        			tasks = ['jar']
        		}
        		project.task('addLocalDepJar'+i) << {
	    		    project.dependencies.add("compile", project.files(new File(dependency[0] + '/build/libs/' + dependency[1] + '-' + dependency[2] + '.jar')))
        		}
        		project.tasks['addLocalDepJar'+i].dependsOn('buildDepJar'+i)
        		project.compileJava.dependsOn('addLocalDepJar'+i)
        	};
        	project.localDependencies.runtime.each() { obj -> 
        		println ' + Adding runtime dependency ' + obj
        		project.dependencies.add("runtime", obj)
        	}
		}
	}
}

class LocalDependenciesPluginExtension {
    def String[] compile;
    def String[] runtime;
}