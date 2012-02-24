/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.redhat.ceylon.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.redhat.ceylon.compiler.java.launcher.Main;

/**
 * Compiles Ceylon and Java source code using the ceylonc compiler
 * @goal ceylonc
 */
public class CeyloncMojo extends AbstractCeylonToolMojo {
    

    /**
     * Used to pipe the compiler output to the maven log
     */
    class LoggingWriter extends Writer {

        private StringBuilder currentLine = new StringBuilder();
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int ii = off; ii < off + len; ii++) {
                char c = cbuf[ii];
                if (c == '\n'
                        || c == '\r') {
                    getLog().debug(currentLine);
                    currentLine.setLength(0);
                    if (ii + 1 < off + len) {
                        char next = cbuf[ii + 1];
                        if ((next == '\n'
                                || c == '\r'
                                && next != c)) {
                            ii++;
                        }
                    }
                } else {
                    currentLine.append(c);
                }
            }
        }

        @Override
        public void flush() throws IOException {
            // Do nothing
        }

        @Override
        public void close() throws IOException {
            getLog().debug(currentLine);
            currentLine.setLength(0);
            currentLine.trimToSize();
        }
     
    }

    
    /**
     * The repository in which to create the output <code>.car</code> file. 
     * Equivalent to the <code>ceylonc</code>'s <code>-out</code> option. 
     *
     * @parameter expression="${ceylonc.out}" default-value="${project.build.directory}"
     */
    private String out;
    
    /**
     * The directory containing ceylon source code. 
     * Equivalent to the <code>ceylonc</code>'s <code>-src</code> option.
     * 
     * @parameter expression="${ceylonc.src}" default-value="${project.build.sourceDirectory}"
     */
    private File src;
    
    /**
     * If <code>true</code>, disables the default module repositories and source directory.
     * Equivalent to the <code>ceylonc</code>'s <code>-d</code> option.
     * 
     * @parameter expression="${ceylonc.disableDefaultRepos}" default="false"
     */
    private boolean disableDefaultRepos = false;
    
    /**
     * If <code>true</code>, the compiler generates verbose output
     * Equivalent to the <code>ceylonc</code>'s <code>-verbose</code> option.
     * 
     * @parameter expression="${ceylonc.verbose}" default="false"
     */
    private boolean verbose = false;

    /**
     * The module repositories containing dependencies.
     * Equivalent to the <code>ceylonc</code>'s <code>-rep</code> option.
     * 
     * @parameter expression="${ceylonc.repositories}"
     */
    private ArrayList<String> repositories;
    
    /**
     * The modules to compile (without versions).
     * 
     * @parameter expression="${ceylonc.modules}"
     */
    private ArrayList<String> modules;
    
    /**
     * Treat warnings as errors
     * @parameter expression="${ceylonc.warningsAsErrors}" default="${false}"
     */
    private boolean warningsAsErrors;
    
    /**
     * Whether the build should fail if there are errors
     * @parameter expression="${ceylonc.failOnError}" default="${true}"
     */
    private boolean failOnError = true;
    
    /**
     * The user name to use for the output repository
     * @parameter expression="${ceylonc.username}" 
     */
    private String username;
    
    /**
     * The password to use for the output repository
     * @parameter expression="${ceylonc.password}" 
     */
    private String password;
    
    /**
     * The source file character encoding.
     * @parameter expression="${project.build.sourceEncoding}" default="${file.encoding}"
     */
    private String encoding;
    
    /**
     * Whether debugging information should be included in the generated classes.
     * @parameter expression="${ceylonc.debug}" default="true"
     */
    private boolean debug = true;
    
    /**
     * The options to be passed with the -g option to the compiler, consisting 
     * of any combination of "lines", "vars", "source". Ignored if 
     * <b>debug</b> is false. If unspecified (and <b>debug</b> is true)
     * all debugging information is generated.
     * @parameter expression="${ceylonc.debuglevel}" 
     */
    private String debuglevel;
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        CommandLine args = buildOptions();
        
        getLog().debug("Invoking ceylonc");
        
        int sc = new Main("ceylonc", new PrintWriter(new LoggingWriter())).compile(args.toArray());
        if (sc == 1) {
            getLog().info("-------------------------------------------------------------");
            getLog().error("COMPILATION ERRORS (see above)");
            getLog().info("-------------------------------------------------------------");
            if (failOnError) {
                throw new MojoFailureException("Compilation Error");
            }
        } else if (sc != 0) {
            throw new MojoExecutionException("The compiler returned an unexpected result");
        }
    }

    private CommandLine buildOptions() throws MojoExecutionException {
        getLog().debug("Command line options to ceylonc:");
        
        CommandLine args = new CommandLine(this);
        args.addOption("-out", resolveRepo(out));
        
        args.addOption("-src", src.getPath());
        
        if (disableDefaultRepos) {
            args.addOption("-d");
        }
        
        if (verbose) {
            args.addOption("-verbose");
        }
        
        if (debug) {
            if (debuglevel == null) {
                args.addOption("-g");
            } else {
                args.addOption("-g:"+debuglevel);
            }
        } else {
            args.addOption("-g:none");
        }
        
        if (warningsAsErrors) {
            args.addOption("-Werror");
        }
        
        if (username != null) {
            args.addOption("-user", username);
        }
        
        if (password != null) {
            args.addOption("-pass", password);
        }
        
        if (repositories != null) {
            for (String rep : repositories) {
                args.addOption("-rep", resolveRepo(rep));
            }
        }
        
        if (encoding != null) {
            args.addOption("-encoding", encoding);
        }
        
        if (modules != null 
                && modules.size() != 0) {
            for (String module : modules) {
                args.addOption(module);
            }
        } else {
            throw new MojoExecutionException("No modules to compile. Specify these using 'ceylonc.modules'");   
        }
        
        getLog().debug("End of command line options to ceylonc");
        
        return args;
    }

}
