package com.dubture.composer.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.tests.model.AbstractModelTests;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.facet.PHPFacets;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.dubture.composer.core.ComposerNature;
import com.dubture.composer.core.ComposerPlugin;
import com.dubture.composer.core.buildpath.BuildPathParser;
import com.dubture.composer.core.facet.FacetManager;
import com.dubture.composer.core.resources.IComposerProject;
import com.dubture.getcomposer.core.collection.ComposerPackages;

@SuppressWarnings("restriction")
public class BuildPathTest extends AbstractModelTests {

	public BuildPathTest() {
		super(ComposerCoreTestPlugin.PLUGIN_ID, "BuildPath tests");
	}

	@Test
	public void testBuildpathParser() throws CoreException, IOException, InterruptedException {

		IScriptProject scriptProject = setUpScriptProject("buildpath");

		assertNotNull(scriptProject);

		IProjectDescription desc = scriptProject.getProject().getDescription();
		desc.setNatureIds(new String[] { PHPNature.ID });
		scriptProject.getProject().setDescription(desc, null);

		ProjectOptions.setPhpVersion(PHPVersion.PHP5_3, scriptProject.getProject());

		PHPFacets.setFacetedVersion(scriptProject.getProject(), PHPVersion.PHP5_3);
		FacetManager.installFacets(scriptProject.getProject(), PHPVersion.PHP5_3, new NullProgressMonitor());

		scriptProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		scriptProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

		ComposerCoreTestPlugin.waitForIndexer();
		ComposerCoreTestPlugin.waitForAutoBuild();

		IFile file = scriptProject.getProject().getFile("composer.json");
		assertNotNull(file);

		assertTrue(scriptProject.getProject().hasNature(PHPNature.ID));
		assertTrue(scriptProject.getProject().hasNature(ComposerNature.NATURE_ID));

		IComposerProject composerProject = ComposerPlugin.getDefault().getComposerProject(scriptProject.getProject());
		BuildPathParser parser = new BuildPathParser(composerProject);
		List<String> paths = parser.getPaths();
		List<String> expected = new ArrayList<String>(Arrays.asList(
				"nother",
				"src",
				"test",
				"mordor/phing/phing/classes/phing",
				"mordor/propel/propel1/runtime/lib",
				"mordor/propel/propel1/generator/lib",
				"mordor/gossi/ldap/src",
				"mordor/symfony/console",
				"mordor/composer"));
		assertThat(paths, is(expected));
		
		// let indexing threads shutdown to avoid SWT thread access errors
		Thread.sleep(2000);

	}
	
}
