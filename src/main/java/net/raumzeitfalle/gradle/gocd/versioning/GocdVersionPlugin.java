package net.raumzeitfalle.gradle.gocd.versioning;

import java.util.function.Supplier;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import groovy.lang.Closure;

@SuppressWarnings({"serial", "unchecked", "rawtypes"})
public class GocdVersionPlugin implements org.gradle.api.Plugin<Project> {

    @SuppressWarnings("unused")
    private static final String GROUP = "Versioning";

    @Override
    public void apply(Project project) {

        project.getExtensions().create("gocdVersion", GocdVersionPluginExtension.class);
        project.getTasks().register("printGocdEnvironment", PrintGocdEnvironmentTask.class);

        Supplier<GocdEnvironment> environmentSupplier = ()->new GocdEnvironmentImpl(System.getenv());

        project.getExtensions().getExtraProperties().set("gocdEnvironment", new Closure<GocdEnvironment>(this,this)  {
            @SuppressWarnings("unused")
            public GocdEnvironment doCall(Object args) {
                return environmentSupplier.get();
            }
        });

        GocdVersionPluginExtension ext = project.getExtensions().getByType(GocdVersionPluginExtension.class);
        ExtraPropertiesExtension extras = project.getExtensions().getExtraProperties();
        extras.set("gocdVersion", new GocdVersionClosure(environmentSupplier,ext,this));
        extras.set("jpackageVersion", new JPackageVersionClosure(project,ext,this));
    }
}
