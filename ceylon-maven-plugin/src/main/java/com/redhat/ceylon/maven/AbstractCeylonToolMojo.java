package com.redhat.ceylon.maven;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractCeylonToolMojo extends AbstractMojo {

    /**
     * @parameter expression="${basedir}"
     */
    protected File basedir;
    
    public AbstractCeylonToolMojo() {
        super();
    }

    protected String resolveRepo(String repo) throws MojoExecutionException {
        try {
            URI uri = new URI(repo);
            if (!uri.isAbsolute()) {// a relative URL, therefore a file
                File file = new File(new File(basedir.getPath()), repo);
                file.mkdirs();
                String resolved = file.getCanonicalPath();
                getLog().debug("Repo " + repo + " resolved to " + resolved);
                return resolved;
            } else {
                return repo;
            }
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Couldn't resolve output repository", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Couldn't resolve  output repository", e);
        }
    }

}