# gradle-local-dependencies-plugin

Gradle plugin that builds other gradle projects (i.e. not subprojects) and then makes the resulting 
jars available as dependencies to this project.

## Why does this exist?

I have a project which is a dependancy of 3 other projects. The other projects are already subprojects of different multi-project builds and I do not want to make them all subprojects of one big multi-project build in the usual gradle multi-project way. Also the projects must be in separate repos.

The "correct" solution is to publish the shared project's jar to a hosted maven repository and have
the dependent projects reference this amongst their respective dependencies. However, the shared project
is inchoate and will be in a state of flux for the next few months so I want to make it easy for
developers to change/fix/extend the source of the shared project.

Another solution would be to ensure that all the subprojets of all the projects are all checked out into the same parent folder, and then use "includeFlat 'shared-dependency'" and a whole load of similar config in their settings.gradle files.

This plugin provides a workaround where by you can call the build.gradle file of any project located anywhere relative to your current project and have the resulting jar available to your current project.

## What about the STS Gradle plugin?
 
Yup, we can use the STS Gradle plugin to reassign jar dependencies to local gradle projects in the
workspace BUT that only works within the context of the workspace - I need to also have it working
from the command line, and play nicely with some other gradle plugins (spring boot, vaadin) that are
used in the build loop.

## Using the plugin

Given a project "web-server" which depends upon another project "domain-model". Each has its own build.gradle file. You only need to add and configure the
plugin in the build.gradle file of the "web-server" project.

To configure the plugin:

```groovy
localDependencies {
	// register the dependent project, syntax is:
	// compile = [ 'relative/path/to/other/project:name-of-jar:version-of-jar' ]
	compile = ['../../core-project/model:domain-model:0.1.0']
	// register the compile dependencies of the other project as runtime dependencies of this one
	runtime = [
	    'org.springframework.boot:spring-boot-starter-data-jpa:1.1.4.RELEASE',
    	'org.hibernate:hibernate-core:4.3.6.Final',
    	'org.postgresql:postgresql:9.3-1102-jdbc41',
    	'commons-dbcp:commons-dbcp:1.4'
	]
}
```

And to register the plugin from the hosted repository:

```groovy
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath "org.lukosan:gradle-local-dependencies-plugin:0.0.1.DEV"
  }
}

apply plugin: "org.lukosan.gradle-local-dependencies-plugin"
```

OR you could just write the plugin inline in the build.gradle file:

```groovy
// apply the plugin
apply plugin: LocalDependenciesPlugin

// plugin definition: version 0.0.1.DEV
class LocalDependenciesPlugin implements Plugin<Project> {
	void apply(Project project) {
		// register the extension that allows you to configure the plugin
		project.extensions.create('localDependencies', LocalDependenciesPluginExtension)
		// do everything after project evaluation occurs
		project.afterEvaluate {
			// loop through each of the "compile" localDependencies
			project.localDependencies.compile.eachWithIndex() { obj, i -> 
				// parse the string into its 3 parts as separated by colons (:)
				def dependency = obj.split(":")
				// register a task to build the dependent project using its build.gradle file
				project.task('buildDepJar'+i, type:GradleBuild) {
        			buildFile = new File(dependency[0] + '/build.gradle')
        			tasks = ['jar']
        		}
        		// register a task to add the built jar file of the dependent project to the
        		// dependencies collection of this project. Note the assumed location and naming
        		// of the jar file is {other project root}/libs/{project-name}-{version}.jar
        		project.task('addLocalDepJar'+i) << {
	    		    project.dependencies.add("compile", project.files(new File(dependency[0] + '/build/libs/' + dependency[1] + '-' + dependency[2] + '.jar')))
        		}
        		// register the first task to occur before the second task
        		project.tasks['addLocalDepJar'+i].dependsOn('buildDepJar'+i)
        		// register the second task to occur before the 'compileJava' task
        		project.compileJava.dependsOn('addLocalDepJar'+i)
        	};
        	// loop through each of the "runtime" localDependencies
        	project.localDependencies.runtime.each() { obj -> 
        		// add each to the dependencies collection of this project
        		project.dependencies.add("runtime", obj)
        	}
		}
	}
}
// plugin extension definition
class LocalDependenciesPluginExtension {
    def String[] compile;
    def String[] runtime;
}
```