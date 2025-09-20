package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import org.eclipse.jgit.util.SystemReader;
import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import groovy.lang.Closure;

public final class GocdVersionPlugin implements org.gradle.api.Plugin<Project> {

    static {
        suppressJGitAttemptToExecLocalGit();
    }

    private static final void suppressJGitAttemptToExecLocalGit() {
        SystemReader.setInstance(JGitNoExecSystemReader.get());
    }

    @SuppressWarnings("unused")
    private static final String GROUP = "GOCD CI/CD";

    @SuppressWarnings("serial")
    @Override
    public void apply(Project project) {

        project.getExtensions().create("gocdVersion", GocdVersionPluginExtension.class);
        project.getTasks().register("printGocdEnvironment", PrintGocdEnvironmentTask.class);

        Supplier<GocdEnvironment> environmentSupplier = () -> new GocdEnvironmentImpl(project, System.getenv());
        project.getExtensions().getExtraProperties().set("gocdEnvironment", new Closure<GocdEnvironment>(this, this) {
            @SuppressWarnings("unused")
            public GocdEnvironment doCall(Object args) {
                return environmentSupplier.get();
            }
        });

        GocdVersionPluginExtension ext = project.getExtensions().getByType(GocdVersionPluginExtension.class);
        ExtraPropertiesExtension extras = project.getExtensions().getExtraProperties();
        GocdVersionClosure gocdVersion = new GocdVersionClosure(project, environmentSupplier, ext, this);

        extras.set("gitTagVersion", new GitTagVersionClosure(project, ext, this));
        extras.set("gitTagLongVersion", new GitTagLongVersionClosure(project, ext, this));
        extras.set("gocdVersion", gocdVersion);
        extras.set("jpackageVersion", new JPackageVersionClosure(project, ext, this));
        extras.set("gocdEnvironmentName", new GocdEnvironmentNameClosure(environmentSupplier, this));
        extras.set("gocdPipelineCounter", new GocdPipelineCounterClosure(environmentSupplier, this));
        extras.set("gocdPipelineName", new GocdPipelineNameClosure(environmentSupplier, this));
        extras.set("gocdPipelineLabel", new GocdPipelineLabelClosure(environmentSupplier, this));
        extras.set("gocdPipelineGroupName", new GocdPipelineGroupNameClosure(environmentSupplier, this));
        extras.set("gocdStageCounter", new GocdStageCounterClosure(environmentSupplier, this));
        extras.set("gocdComputerName", new ComputerNameClosure(environmentSupplier, this));
        extras.set("gocdStageName", new GocdStageNameClosure(environmentSupplier, this));
        extras.set("gocdJobName", new GocdJobNameClosure(environmentSupplier, this));
        extras.set("gocdTriggerUser", new GocdTriggerUserClosure(environmentSupplier, this));
        extras.set("isAutomatedBuild", new GocdIsAutomatedBuildClosure(environmentSupplier, this));
        extras.set("gocdGitMaterialBranchName", new GocdGitMaterialBranchNameClosure(environmentSupplier, this));
        extras.set("gocdComputerName", new GocdComputerNameClosure(environmentSupplier, this));
        extras.set("gocdServerUrl", new GocdServerUrlClosure(environmentSupplier, this));

        /*
         * Ensure that after configuration phase, the version closure is evaluated and the projects version property
         * is updated accordingly.
         */
        project.afterEvaluate(p->this.updateProjectVersionIfNotStaticallyDefined(p,gocdVersion));

        project.getGradle().addBuildListener(new BuildListener() {
            @Override
            public void settingsEvaluated(Settings settings) {

            }

            @Override
            public void projectsLoaded(Gradle gradle) {

            }

            @Override
            public void projectsEvaluated(Gradle gradle) {

            }

            @Override
            public void buildFinished(BuildResult result) {
                Supplier<String> version = ()->project.getVersion().toString();
                result.getGradle().getRootProject().getLogger().lifecycle("Project version: " + version.get());
            }
        });


    }

    private void updateProjectVersionIfNotStaticallyDefined(Project project, GocdVersionClosure gocdVersion) {
        Object version = project.getVersion();
        if (null == version) {
            String updatedVersion = gocdVersion.doCall().build();
            project.setVersion(updatedVersion);
            project.getLogger().info("Configured project version: " + project.getVersion());
            return;
        }

        if ("unspecified".equals(version.toString())) {
            String updatedVersion = gocdVersion.doCall().build();
            project.setVersion(updatedVersion);
            project.getLogger().info("Configured version: " + updatedVersion);
            return;
        }

        if ("".equalsIgnoreCase(version.toString().trim())) {
            String updatedVersion = gocdVersion.doCall().build();
            project.setVersion(updatedVersion);
            project.getLogger().info("Configured version: " + updatedVersion);
            return;
        }

        project.getLogger().info("GocdVersionPlugin: The project property [version] is already configured to [{}]. Skipping evaluation of Git tags for version number creation.", version);
    }

}
