package com.dubture.composer.ui.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.ui.wizards.NewElementWizard;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.dubture.composer.core.facet.FacetManager;
import com.dubture.composer.core.log.Logger;
import com.dubture.composer.ui.editor.composer.ComposerFormEditor;

/**
 * @author Robert Gruendler <r.gruendler@gmail.com>
 */
@SuppressWarnings("restriction")
public abstract class AbstractComposerWizard extends NewElementWizard implements INewWizard, IExecutableExtension {

	protected AbstractWizardFirstPage firstPage;
	protected AbstractWizardSecondPage secondPage;
	protected AbstractWizardSecondPage lastPage;
	protected IConfigurationElement config;
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		this.config = config;
	}

	public void addPages() {
		super.addPages();
		
		firstPage = getFirstPage();
		addPage(firstPage);

		secondPage = getSecondPage();
		addPage(secondPage);
		
		lastPage = secondPage;
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		
		if (firstPage != null) {
			firstPage.performFinish(monitor);
		}
		
		if (secondPage != null) {
			secondPage.performFinish(monitor);
		}
	}


	@Override
	public IModelElement getCreatedElement() {
		return DLTKCore.create(firstPage.getProjectHandle());		
	}
	
	@Override
	public boolean performFinish() {
		
		boolean res = super.performFinish();
		if (res) {
			
			BasicNewProjectResourceWizard.updatePerspective(config);
			selectAndReveal(lastPage.getScriptProject().getProject());
			IProject project = lastPage.getScriptProject().getProject();
			PHPVersion version = firstPage.getPHPVersionValue();
			if (version == null) {
				version = ProjectOptions.getDefaultPhpVersion();
			}
			
			FacetManager.installFacets(project, version, null);
			IFile json = project.getFile("composer.json");
			
			if (json != null) {
				try {
					IEditorInput editorInput = new FileEditorInput(json);
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					page.openEditor(editorInput, ComposerFormEditor.ID);
				} catch (Exception e) {
					Logger.logException(e);
				}
			}
		}
		
		return res;
	}
	
	@Override
	public boolean performCancel() {
		secondPage.cancel();
		return super.performCancel();
	}

	protected abstract AbstractWizardFirstPage getFirstPage();
	
	protected abstract AbstractWizardSecondPage getSecondPage();

}
