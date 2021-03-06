package com.dubture.composer.ui.wizard.importer;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.internal.resources.ProjectDescriptionReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardDataTransferPage;

import com.dubture.composer.core.log.Logger;
import com.dubture.composer.ui.ComposerUIPlugin;
import com.dubture.composer.ui.wizard.ValidationException;
import com.dubture.composer.ui.wizard.ValidationException.Severity;
import com.dubture.getcomposer.core.ComposerConstants;
import com.dubture.getcomposer.core.ComposerPackage;

/**
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class WizardResourceImportPage extends WizardDataTransferPage {

	protected String source;
	protected String target;
	protected String projectName;
	protected StringButtonDialogField sourcePath;
	protected IPath json;
	protected ComposerPackage composerPackage;
	protected StringDialogField projectNameField;
	protected IWorkspace workspace;
	protected StringButtonDialogField targetPath;
	protected Button useWorkspaceLocation;
	protected final String defaultMessage = "Select an existing Composer project to automatically setup your project";
	protected String lastNameFromProjectFile = null;
	
	protected boolean useWorkspace = true;
	
	public WizardResourceImportPage(IWorkbench aWorkbench, IStructuredSelection selection, String[] strings) {
		super("Import existing Composer project");
		setTitle("Import an existing Composer project");
		setDescription(defaultMessage);
	}

	@Override
	public void handleEvent(Event event) {

	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getSourcePath() {
		return source;
	}
	
	public String getTargetPath() {
		return target;
	}
	
	public boolean doUseWorkspace() {
		return useWorkspace;
	}

	@Override
	public void createControl(Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		
		int numColumns = 3;
		GridLayoutFactory.fillDefaults().numColumns(numColumns).applyTo(control);
		
		projectNameField = new StringDialogField();
		projectNameField.setLabelText("Project name");
		projectNameField.doFillIntoGrid(control, numColumns);
		LayoutUtil.setHorizontalGrabbing(projectNameField.getTextControl(null));
		
		projectNameField.setDialogFieldListener(new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(DialogField field) {
				projectName = projectNameField.getText();
				updatePageCompletion();
			}
		});
		

		sourcePath = new StringButtonDialogField(new IStringButtonAdapter() {
			@Override
			public void changeControlPressed(DialogField field) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
				dialog.setMessage("Select an existing composer project");
				source = dialog.open();
				try {
					handleSourcePathChange();
				} catch (IOException e) {
					Logger.logException(e);
				}
			}
		});
		
		sourcePath.setLabelText("Source path");
		sourcePath.setButtonLabel("Browse");
		sourcePath.doFillIntoGrid(control, numColumns);
		sourcePath.getTextControl(null).setEnabled(false);
		
		useWorkspaceLocation = new Button(control, SWT.CHECK);
		useWorkspaceLocation.setText("Use default workspace location");
		useWorkspaceLocation.setSelection(true);
		
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(useWorkspaceLocation);
		
		useWorkspaceLocation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				targetPath.setEnabled(useWorkspaceLocation.getSelection() == false);
				useWorkspace = useWorkspaceLocation.getSelection();
				updatePageCompletion();
			}
		});
		
		// don't know if we can use this in the future. for now, simply use the source location as the project location
		useWorkspaceLocation.setVisible(false);
		
		/*
		targetPath = new StringButtonDialogField(new IStringButtonAdapter() {
			@Override
			public void changeControlPressed(DialogField field) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
				dialog.setMessage("Select the target location");
				target = dialog.open();
				try {
					handleTargetPathChange();
				} catch (IOException e) {
					Logger.logException(e);
				}
			}
		});
		
		targetPath.setLabelText("Target path");
		targetPath.setButtonLabel("Browse");
		targetPath.doFillIntoGrid(control, numColumns);
		targetPath.getTextControl(null).setEnabled(false);
		targetPath.setEnabled(false);
		*/
		
		LayoutUtil.setHorizontalGrabbing(sourcePath.getTextControl(null));
		setControl(control);
		
		workspace = ResourcesPlugin.getWorkspace();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, ComposerUIPlugin.PLUGIN_ID + "." + "help_context_wizard_importer");
		setPageComplete(false);
	}
	
	
	protected void handleTargetPathChange() throws IOException{
		
		if (target != null) {
			//targetPath.setText(target);
		}
		
		updatePageCompletion();
	}

	protected void handleSourcePathChange() throws IOException {
		
		if (source != null) {
			sourcePath.setText(source);
			json = new Path(source).append(ComposerConstants.COMPOSER_JSON);
			
			if (json != null && json.toFile().exists()) {
				composerPackage = new ComposerPackage(json.toFile());
			} else {
				composerPackage = null;
			}
			setProjectNameFromJson();
		}
		
		updatePageCompletion();
	}
	
	
	protected void setProjectNameFromJson() {
		
		if (projectNameField.getText().length() > 0 || composerPackage == null) {
			return;
		}
		
		String name = composerPackage.getName();
		if (name != null) {
			if (name.contains("/")) {
				String[] split = name.split("/");
				if (split.length == 2) {
					name = split[1];
				}
			}

			try {
				validateProjectName(name);
			} catch (ValidationException e) {
				setErrorMessage(e.getMessage());
				updatePageCompletion();
				return;
			}
			projectNameField.setText(name);
		}
	}
	
	protected void validateProjectName(String name) throws ValidationException {
		
		IProject project = workspace.getRoot().getProject(name);
		if (project != null && project.exists()) {
			throw new ValidationException("A project with the same name already exists in the workspace", Severity.ERROR);
		}
	}

	@Override
	protected boolean validateSourceGroup() {
		
		projectNameField.getTextControl(null).setEnabled(true);
		
		if (json == null || json.toFile().exists() == false) {
			setErrorMessage("The selected folder does not contain a composer.json file.");
			return false;
		}
		
		IPath sourceProject = new Path(source).append(".project");
		
		if (sourceProject.toFile().exists()) {
			try {
				ProjectDescriptionReader reader = new ProjectDescriptionReader();
				ProjectDescription projectDescription = reader.read(sourceProject);
				
				if (projectDescription == null) {
					setErrorMessage("Cannot read the source project.");
					return false;
				}
				
				projectName = projectDescription.getName();
				projectNameField.setTextWithoutUpdate(projectName);
				projectNameField.getTextControl(null).setEnabled(false);
				lastNameFromProjectFile = projectName;
				
				setMessage("The target location contains already an eclipse project. The wizard will use the existing information to import the project");
				return true;
				
			} catch (IOException e) {
				Logger.logException(e);
				setErrorMessage("Error reading source project");
				return false;
			}
		}
		
		
		projectNameField.getTextControl(null).setEnabled(true);
		
		setMessage(defaultMessage);
		
		return true;
	}
	
	@Override
	protected boolean validateDestinationGroup() {
		
		if (projectName == null || projectName.length() == 0) {
			setErrorMessage("Please enter a project name");
			return false;
		}
		
		try {
			validateProjectName(projectName);
		} catch (ValidationException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		
		if (useWorkspaceLocation.getSelection() == true) {
			return true;
		}
		
		if (target == null) {
			setMessage("Please select a target path");
			return false;
		}
		
		IPath targetPath = new Path(target);
		File file = targetPath.toFile();
		
		if (file == null || ! file.exists()  || ! file.isDirectory()) {
			setErrorMessage("The selected target location is invalid");
			return false;
		}
		
		IPath targetProjectPath = targetPath.append(projectName);
		
		File targetProject = targetProjectPath.toFile();
		
		if (targetProject == null || targetProject.exists()) {
			setErrorMessage("The target folder already contains a file/folder with the specified project name");
			return false;
		}
		
		setMessage(defaultMessage);
		
		return true;
	}

	@Override
	protected boolean allowNewContainerName() {
		return false;
	}
	
	
	
}
