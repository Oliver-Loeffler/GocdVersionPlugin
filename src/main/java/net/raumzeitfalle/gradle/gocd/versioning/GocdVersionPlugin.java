package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import groovy.lang.Closure;

public class GocdVersionPlugin implements org.gradle.api.Plugin<Project> {

    @SuppressWarnings("unused")
    private static final String GROUP = "GOCD CI/CD";

    @SuppressWarnings("serial")
    @Override
    public void apply(Project project) {

        project.getExtensions().create("gocdVersion", GocdVersionPluginExtension.class);
        project.getTasks().register("printGocdEnvironment", PrintGocdEnvironmentTask.class);

        Supplier<GocdEnvironment> environmentSupplier = ()->new GocdEnvironmentImpl(project, System.getenv());
        project.getExtensions().getExtraProperties().set("gocdEnvironment", new Closure<GocdEnvironment>(this,this)  {
            @SuppressWarnings("unused")
            public GocdEnvironment doCall(Object args) {
                return environmentSupplier.get();
            }
        });

        GocdVersionPluginExtension ext = project.getExtensions().getByType(GocdVersionPluginExtension.class);
        ExtraPropertiesExtension extras = project.getExtensions().getExtraProperties();
        extras.set("gitTagVersion",             new GitTagVersionClosure(project, ext, this));
        extras.set("gocdVersion",               new GocdVersionClosure(project,environmentSupplier,ext,this));
        extras.set("jpackageVersion",           new JPackageVersionClosure(project,ext,this));
        extras.set("gocdEnvironmentName",       new GocdEnvironmentNameClosure(environmentSupplier, this));
        extras.set("gocdPipelineCounter",       new GocdPipelineCounterClosure(environmentSupplier, this));
        extras.set("gocdPipelineName",          new GocdPipelineNameClosure(environmentSupplier, this));
        extras.set("gocdPipelineLabel",         new GocdPipelineLabelClosure(environmentSupplier, this));
        extras.set("gocdPipelineGroupName",     new GocdPipelineGroupNameClosure(environmentSupplier, this));
        extras.set("gocdStageCounter",          new GocdStageCounterClosure(environmentSupplier, this));
        extras.set("gocdComputerName",          new ComputerNameClosure(environmentSupplier, this));
        extras.set("gocdStageName",             new GocdStageNameClosure(environmentSupplier, this));
        extras.set("gocdJobName",               new GocdJobNameClosure(environmentSupplier, this));
        extras.set("gocdTriggerUser",           new GocdTriggerUserClosure(environmentSupplier, this));
        extras.set("isAutomatedBuild",          new GocdIsAutomatedBuildClosure(environmentSupplier, this));
        extras.set("gocdGitMaterialBranchName", new GocdGitMaterialBranchNameClosure(environmentSupplier, this));
        extras.set("gocdComputerName",          new GocdComputerNameClosure(environmentSupplier, this));
        extras.set("gocdServerUrl",             new GocdServerUrlClosure(environmentSupplier, this));
    }
}
