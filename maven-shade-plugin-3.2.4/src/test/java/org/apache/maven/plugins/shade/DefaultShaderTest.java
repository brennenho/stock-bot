package org.apache.maven.plugins.shade;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.filter.Filter;
import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;
import org.apache.maven.plugins.shade.resource.AppendingTransformer;
import org.apache.maven.plugins.shade.resource.ComponentsXmlResourceTransformer;
import org.apache.maven.plugins.shade.resource.ManifestResourceTransformer;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Jason van Zyl
 * @author Mauro Talevi
 */
public class DefaultShaderTest
    extends TestCase
{
    private static final String[] EXCLUDES = new String[] { "org/codehaus/plexus/util/xml/Xpp3Dom",
        "org/codehaus/plexus/util/xml/pull.*" };

    public void testOverlappingResourcesAreLogged() throws IOException, MojoExecutionException {
        final DefaultShader shader = new DefaultShader();
        final MockLogger logs = new MockLogger();
        shader.enableLogging(logs);

        // we will shade two jars and expect to see META-INF/MANIFEST.MF overlaps, this will always be true
        // but this can lead to a broken deployment if intended for OSGi or so, so even this should be logged
        final Set<File> set = new LinkedHashSet<>();
        set.add( new File( "src/test/jars/test-project-1.0-SNAPSHOT.jar" ) );
        set.add( new File( "src/test/jars/plexus-utils-1.4.1.jar" ) );

        final ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars( set );
        shadeRequest.setRelocators( Collections.<Relocator>emptyList() );
        shadeRequest.setResourceTransformers( Collections.<ResourceTransformer>emptyList() );
        shadeRequest.setFilters( Collections.<Filter>emptyList() );
        shadeRequest.setUberJar( new File( "target/foo-custom_testOverlappingResourcesAreLogged.jar" ) );
        shader.shade( shadeRequest );

        final String failureWarnMessage = logs.warnMessages.toString();
        assertTrue(failureWarnMessage, logs.warnMessages.contains(
                "plexus-utils-1.4.1.jar, test-project-1.0-SNAPSHOT.jar define 1 overlapping resource:"));
        assertTrue(failureWarnMessage, logs.warnMessages.contains("- META-INF/MANIFEST.MF"));

        final String failureDebugMessage = logs.debugMessages.toString();
        assertTrue(failureDebugMessage, logs.debugMessages.contains(
                "We have a duplicate META-INF/MANIFEST.MF in src/test/jars/plexus-utils-1.4.1.jar" ));
    }

    public void testOverlappingResourcesAreLoggedExceptATransformerHandlesIt() throws Exception {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        Set<File> set = new LinkedHashSet<>();
        temporaryFolder.create();
        File j1 = temporaryFolder.newFile("j1.jar");
        try ( JarOutputStream jos = new JarOutputStream(new FileOutputStream( j1 ) ) )
        {
            jos.putNextEntry(new JarEntry( "foo.txt" ));
            jos.write("c1".getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        File j2 = temporaryFolder.newFile("j2.jar");
        try ( JarOutputStream jos = new JarOutputStream(new FileOutputStream( j2 ) ) )
        {
            jos.putNextEntry(new JarEntry( "foo.txt" ));
            jos.write("c2".getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
        set.add( j1 );
        set.add( j2 );

        AppendingTransformer transformer = new AppendingTransformer();
        Field resource = AppendingTransformer.class.getDeclaredField( "resource" );
        resource.setAccessible( true );
        resource.set( transformer, "foo.txt" );

        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars( set );
        shadeRequest.setRelocators( Collections.<Relocator>emptyList() );
        shadeRequest.setResourceTransformers( Collections.<ResourceTransformer>singletonList( transformer) );
        shadeRequest.setFilters( Collections.<Filter>emptyList() );
        shadeRequest.setUberJar( new File( "target/foo-custom_testOverlappingResourcesAreLogged.jar" ) );

        DefaultShader shaderWithTransformer = new DefaultShader();
        final MockLogger logWithTransformer = new MockLogger();
        shaderWithTransformer.enableLogging( logWithTransformer );
        shaderWithTransformer.shade( shadeRequest );

        DefaultShader shaderWithoutTransformer = new DefaultShader();
        MockLogger logWithoutTransformer = new MockLogger();
        shaderWithoutTransformer.enableLogging( logWithoutTransformer );
        shadeRequest.setResourceTransformers( Collections.<ResourceTransformer>emptyList() );
        shaderWithoutTransformer.shade( shadeRequest );

        temporaryFolder.delete();

        assertTrue(logWithTransformer.warnMessages.toString(), logWithTransformer.warnMessages.isEmpty());
        assertTrue(logWithoutTransformer.warnMessages.toString(), logWithoutTransformer.warnMessages.containsAll(
               Arrays.<String>asList( "j1.jar, j2.jar define 1 overlapping resource:", "- foo.txt" ) ) );
    }

    public void testShaderWithDefaultShadedPattern()
        throws Exception
    {
        shaderWithPattern( null, new File( "target/foo-default.jar" ), EXCLUDES );
    }

    public void testShaderWithStaticInitializedClass()
        throws Exception
    {
        Shader s = newShader();

        Set<File> set = new LinkedHashSet<>();

        set.add( new File( "src/test/jars/test-artifact-1.0-SNAPSHOT.jar" ) );

        List<Relocator> relocators = new ArrayList<>();

        relocators.add( new SimpleRelocator( "org.apache.maven.plugins.shade", null, null, null ) );

        List<ResourceTransformer> resourceTransformers = new ArrayList<>();

        List<Filter> filters = new ArrayList<>();

        File file = new File( "target/testShaderWithStaticInitializedClass.jar" );

        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars( set );
        shadeRequest.setUberJar( file );
        shadeRequest.setFilters( filters );
        shadeRequest.setRelocators( relocators );
        shadeRequest.setResourceTransformers( resourceTransformers );

        s.shade( shadeRequest );

        try ( URLClassLoader cl = new URLClassLoader( new URL[] { file.toURI().toURL() } ) ) {
          Class<?> c = cl.loadClass( "hidden.org.apache.maven.plugins.shade.Lib" );
          Object o = c.newInstance();
          assertEquals( "foo.bar/baz", c.getDeclaredField( "CONSTANT" ).get( o ) );
        }
    }

    public void testShaderWithCustomShadedPattern()
        throws Exception
    {
        shaderWithPattern( "org/shaded/plexus/util", new File( "target/foo-custom.jar" ), EXCLUDES );
    }

    public void testShaderWithoutExcludesShouldRemoveReferencesOfOriginalPattern()
        throws Exception
    {
        // FIXME: shaded jar should not include references to org/codehaus/* (empty dirs) or org.codehaus.* META-INF
        // files.
        shaderWithPattern( "org/shaded/plexus/util", new File( "target/foo-custom-without-excludes.jar" ),
                           new String[] {} );
    }

    public void testShaderWithRelocatedClassname()
        throws Exception
    {
        DefaultShader s = newShader();

        Set<File> set = new LinkedHashSet<>();

        set.add( new File( "src/test/jars/test-project-1.0-SNAPSHOT.jar" ) );

        set.add( new File( "src/test/jars/plexus-utils-1.4.1.jar" ) );

        List<Relocator> relocators = new ArrayList<>();

        relocators.add( new SimpleRelocator( "org/codehaus/plexus/util/", "_plexus/util/__", null,
                                             Arrays.<String> asList() ) );

        List<ResourceTransformer> resourceTransformers = new ArrayList<>();

        resourceTransformers.add( new ComponentsXmlResourceTransformer() );

        List<Filter> filters = new ArrayList<>();

        File file = new File( "target/foo-relocate-class.jar" );

        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars( set );
        shadeRequest.setUberJar( file );
        shadeRequest.setFilters( filters );
        shadeRequest.setRelocators( relocators );
        shadeRequest.setResourceTransformers( resourceTransformers );

        s.shade( shadeRequest );

        try ( URLClassLoader cl = new URLClassLoader( new URL[] { file.toURI().toURL() } ) ) {
          Class<?> c = cl.loadClass( "_plexus.util.__StringUtils" );
          // first, ensure it works:
          Object o = c.newInstance();
          assertEquals( "", c.getMethod( "clean", String.class ).invoke( o, (String) null ) );

          // now, check that its source file was rewritten:
          final String[] source = { null };
          final ClassReader classReader = new ClassReader( cl.getResourceAsStream( "_plexus/util/__StringUtils.class" ) );
          classReader.accept( new ClassVisitor( Opcodes.ASM4 )
          {
            @Override
            public void visitSource( String arg0, String arg1 )
            {
                super.visitSource( arg0, arg1 );
                source[0] = arg0;
            }
          }, ClassReader.SKIP_CODE );
          assertEquals( "__StringUtils.java", source[0] );
        }
    }

    private void shaderWithPattern( String shadedPattern, File jar, String[] excludes )
        throws Exception
    {
        DefaultShader s = newShader();

        Set<File> set = new LinkedHashSet<>();

        set.add( new File( "src/test/jars/test-project-1.0-SNAPSHOT.jar" ) );

        set.add( new File( "src/test/jars/plexus-utils-1.4.1.jar" ) );

        List<Relocator> relocators = new ArrayList<>();

        relocators.add( new SimpleRelocator( "org/codehaus/plexus/util", shadedPattern, null, Arrays.asList( excludes ) ) );

        List<ResourceTransformer> resourceTransformers = new ArrayList<>();

        resourceTransformers.add( new ComponentsXmlResourceTransformer() );

        List<Filter> filters = new ArrayList<>();

        ShadeRequest shadeRequest = new ShadeRequest();
        shadeRequest.setJars( set );
        shadeRequest.setUberJar( jar );
        shadeRequest.setFilters( filters );
        shadeRequest.setRelocators( relocators );
        shadeRequest.setResourceTransformers( resourceTransformers );

        s.shade( shadeRequest );
    }

    private static DefaultShader newShader()
    {
        DefaultShader s = new DefaultShader();

        s.enableLogging( new ConsoleLogger( Logger.LEVEL_INFO, "TEST" ) );

        return s;
    }

    private static class MockLogger extends AbstractLogger
    {
        private final List<String> debugMessages = new ArrayList<>();
        private final List<String> warnMessages = new ArrayList<>();

        private MockLogger()
        {
            super( Logger.LEVEL_INFO, "test" );
        }

        @Override
        public void debug( String s, Throwable throwable )
        {
            debugMessages.add( s.replace( '\\', '/' ).trim() );
        }

        @Override
        public void info( String s, Throwable throwable )
        {
            // no-op
        }

        @Override
        public void warn( String s, Throwable throwable )
        {
            warnMessages.add( s.replace( '\\', '/' ).trim() );
        }

        @Override
        public void error( String s, Throwable throwable )
        {
            // no-op
        }

        @Override
        public void fatalError( String s, Throwable throwable )
        {
            // no-op
        }

        @Override
        public Logger getChildLogger( String s )
        {
            return this;
        }
    }
}
