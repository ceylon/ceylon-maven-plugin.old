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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.redhat.ceylon.compiler.java.launcher.Main;

/**
 * Compiles Ceylon and Java source code using the ceylonc compiler
 * @goal ceylonc
 */
public class CeyloncMojo extends AbstractMojo {
    

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
     * The directory in which to create the output <code>.car</code> file. 
     * Equivalent to the <code>ceylonc</code>'s <code>-out</code> option. 
     *
     * @parameter expression="${ceylonc.out}" default-value="${project.build.directory}"
     */
    private File out;
    
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
    private String[] repositories;
    
    /**
     * The modules to compile (without versions).
     * 
     * @parameter expression="${ceylonc.modules}"
     */
    private String[] modules;
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        CommandLine args = new CommandLine(this);
        args.addOption("-out", out.getPath());
        out.mkdirs();
        
        args.addOption("-src", src.getPath());
        
        if (disableDefaultRepos) {
            args.addOption("-d");
        }
        
        if (verbose) {
            args.addOption("-verbose");
        }
        
        if (repositories != null) {
            for (String rep : repositories) {
                args.addOption("-rep", rep);
            }
        }
        
        if (modules != null 
                && modules.length != 0) {
            for (String module : modules) {
                args.addOption(module);
            }
        } else {
            throw new MojoExecutionException("No modules to compile. Specify these using 'ceylonc.modules'");
                    
        }        
        
        int sc = new Main("ceylonc", new PrintWriter(new LoggingWriter())).compile(args.toArray());
        if (sc != 0) {
            throw new MojoFailureException("There were compiler errors");
        }
    }
}
