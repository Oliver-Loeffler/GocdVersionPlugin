package net.raumzeitfalle.gradle.gocd.versioning;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;

class JGitNoExecSystemReader extends SystemReader {

    private static final JGitNoExecSystemReader instance = new JGitNoExecSystemReader();

    public static JGitNoExecSystemReader get() {
        return instance;
    }

    private final SystemReader delegate = SystemReader.getInstance();

    @Override
    public String getHostname() {
        return delegate.getHostname();
    }

    @Override
    public String getenv(String variable) {
        return delegate.getenv(variable);
    }

    @Override
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }

    @Override
    public FileBasedConfig openUserConfig(Config parent, FS fs) {
        return delegate.openJGitConfig(parent, fs);
    }

    @Override
    public FileBasedConfig openSystemConfig(Config parent, FS fs) {
        return delegate.openJGitConfig(parent, fs);
    }

    @Override
    public FileBasedConfig openJGitConfig(Config parent, FS fs) {
        return delegate.openJGitConfig(parent, fs);
    }

    @Override
    public long getCurrentTime() {
        return delegate.getCurrentTime();
    }

    @Override
    public int getTimezone(long when) {
        return delegate.getTimezone(when);
    }
}
