package fi.tilaton.svn_working_copy_revision_maven_plugin;

import java.io.File;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;

/**
 * 
 */
@Mojo( name = "read-info", defaultPhase = LifecyclePhase.VALIDATE, requiresProject = true )
public class VersionMojo extends AbstractMojo {
	
	/**
     * Location of the directory that the SVN revision info is taken from.
     */
    @Parameter(defaultValue = "${basedir}", property = "svnDirectory", required = true)
    private File svnDirectory;
    
    /**
     * The Maven project that is executing.
     */
    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    protected MavenProject project;

    public void execute() throws MojoExecutionException {
        if ( !svnDirectory.exists() ) {
        	throw new MojoExecutionException("Can't determine revision info, the directory " + svnDirectory + " does not exist.");
        }
        
        SVNClientManager manager = SVNClientManager.newInstance();
		SVNStatusClient client = manager.getStatusClient();
		try {
			SVNStatus status = client.doStatus(svnDirectory, false);
			final String branch = removeStart(trim(status.getRepositoryRelativePath()), "branches/");
			
			project.getProperties().put("wcsvn.buildNumber", String.valueOf(status.getCommittedRevision().getNumber()));
			project.getProperties().put("wcsvn.buildBranch", branch);
			
			getLog().info("Branch: " + branch);
			getLog().info("Revision: " + status.getCommittedRevision().getNumber());
		} catch (Exception e) {
			throw new MojoExecutionException("Can't determine revision info:", e);
		} finally {
			if (manager != null) {
				manager.dispose();
			}
		}
    }
    
    protected String trim(final String string) {
    	if (string == null) {
    		return null;
    	}
    	return string.trim();
    }
    
    protected String removeStart(String string, String toRemove) {
    	if (string == null || toRemove == null) {
    		return string;
    	}
    	
    	if (string.indexOf(toRemove) == toRemove.length()) {
    		return string.substring(toRemove.length());
    	}
    	
    	return string;
    }
    
}
