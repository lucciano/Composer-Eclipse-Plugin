package com.dubture.composer.core.facet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathContainer;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.php.internal.core.buildpath.BuildPathUtils;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

import com.dubture.composer.core.ComposerBuildpathContainerInitializer;
import com.dubture.composer.core.ComposerNature;
import com.dubture.composer.core.ComposerPlugin;
import com.dubture.composer.core.model.ComposerBuildpathContainer;
import com.dubture.composer.core.util.BuildpathUtil;

/**
 * Facet installation action delegate to add the composer nature to a PHP
 * project.
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 * 
 */
@SuppressWarnings("restriction")
public class UninstallActionDelegate implements IDelegate {
	@Override
	public void execute(IProject project, IProjectFacetVersion version,
			Object object, IProgressMonitor progress) throws CoreException {
		if (!project.hasNature(PHPNature.ID)) {
			return;
		}

		progress.subTask("Uninstalling composer nature");
		IScriptProject scriptProject = DLTKCore.create(project);

		// remove the composer nature
		IProjectDescription desc = project.getDescription();
		ArrayList<String> natures =  new ArrayList<String>(Arrays.asList(desc.getNatureIds()));
		int index = natures.indexOf(ComposerNature.NATURE_ID);
		if (index != -1) {
			natures.remove(index);
		}
		desc.setNatureIds(natures.toArray(new String[]{}));
		project.setDescription(desc, progress);

		progress.subTask("Uninstalling composer buildpath");

		// maybe comment out this one:
		// remove composer buildpath entry

		if (ComposerPlugin.getDefault().isBuildpathContainerEnabled()) {
			IBuildpathContainer composerContainer = new ComposerBuildpathContainer(
					new Path(ComposerBuildpathContainerInitializer.CONTAINER),
					scriptProject);
			List<IBuildpathEntry> entries = new ArrayList<IBuildpathEntry>();
			entries.remove(DLTKCore.newContainerEntry(composerContainer.getPath()));

			// remove the composer buildpathentry to the project
			BuildPathUtils.addEntriesToBuildPath(scriptProject, entries);

			BuildpathUtil.setupVendorBuildpath(scriptProject, progress);

		}
	}
}
