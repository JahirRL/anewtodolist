import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {

    buildType(TestIaC)
    buildType(SlowTest)
    buildType(FastTest)
    buildType(Package_1)
    buildType(Build)
}

object TestIaC : BuildType({
    id("IaCTest")
    name = "Infrastructure As Code"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        powerShell {
            scriptMode = script {
                content = """write-host "Hello World!" """
            }
        }

        powerShell {
            scriptMode = script {
                content = """write-host "This is a test made with Kotlin" """
            }
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }

})

object Build : BuildType({
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "TestStep"
            id = "Maven2"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})

object FastTest : BuildType({
    name = "Fast Test"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            id = "Maven2"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test"
        }
    }

    dependencies {
        snapshot(Build) {
        }
    }
})

object Package_1 : BuildType({
    id("Package")
    name = "Package"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            id = "Maven2"
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true -DskipTests"
        }
    }

    dependencies {
        snapshot(FastTest) {
        }
        snapshot(SlowTest) {
        }
    }
})

object SlowTest : BuildType({
    name = "Slow Test"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            id = "Maven2"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"
        }
    }

    dependencies {
        snapshot(Build) {
        }
    }
})
