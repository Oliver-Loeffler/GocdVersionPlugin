package net.raumzeitfalle.gradle.gocd.versioning;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.logging.Logger;

import java.util.Set;
import java.util.function.Supplier;

public class GocdVersionPlugin implements org.gradle.api.Plugin<Project> {

    private static final String GROUP = "Versioning";

    @Override
    public void apply(Project project) {

        project.getExtensions().create("gocdVersion", GocdVersionPluginExtension.class);
        project.getTasks().register(   "printGocdEnvironment", PrintGocdEnvironmentTask.class);

        Supplier<GocdEnvironment> environmentSupplier = ()->new GocdEnvironmentImpl(System.getenv());

        project.getExtensions().getExtraProperties().set("gocdEnvironment", new Closure<GocdEnvironment>(this,this)  {
            public GocdEnvironment doCall(Object args) {
                return environmentSupplier.get();
            }
        });

        GocdVersionPluginExtension ext = project.getExtensions().getByType(GocdVersionPluginExtension.class);
//        project.getExtensions().getExtraProperties().set("gocdVersion", new Closure<GocdVersionBuilder>(this,this) {
//            public GocdVersionBuilder doCall(Object autoBuildVersion, Object manualBuildVersion) {
//                return new GocdVersionBuilder(environmentSupplier.get(),ext,manualBuildVersion,autoBuildVersion);
//            };
//        });

        project.getExtensions().getExtraProperties().set("gocdVersion", new GocdVersionClosure(environmentSupplier,ext,this));
    }
}
