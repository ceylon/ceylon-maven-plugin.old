package com.redhat.ceylon.maven.test;


import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.Test;

public class CeyloncMojoIT extends MojoTest {

    @Test
    public void helloWorld() throws MavenInvocationException {
        assertMvnSuccess("src/test/resources/com/redhat/ceylon/maven/test/hello-world.xml", 
                "clean", "org.ceylon-lang:ceylon-maven-plugin:ceylonc");
    }
    
    @Test
    public void compileError() throws MavenInvocationException {
        assertMvnFailure("src/test/resources/com/redhat/ceylon/maven/test/error.xml", 
                "clean", "org.ceylon-lang:ceylon-maven-plugin:ceylonc");
    }
    
}
