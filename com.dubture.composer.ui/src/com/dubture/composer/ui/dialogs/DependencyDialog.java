package com.dubture.composer.ui.dialogs;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.getcomposer.VersionedPackage;
import org.getcomposer.collection.Versions;
import org.getcomposer.packages.PackagistDownloader;

import com.dubture.composer.ui.parts.composer.VersionSuggestion;
import com.dubture.composer.ui.utils.WidgetFactory;

public class DependencyDialog extends Dialog {

	private VersionedPackage dependency;
	private Text name;
	private Text version;
	
	/**
	 * @wbp.parser.constructor
	 * @param parentShell
	 * @param dependency
	 */
	public DependencyDialog(Shell parentShell, VersionedPackage dependency) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM);
		this.dependency = dependency;
	}

	public DependencyDialog(IShellProvider parentShell, VersionedPackage dependency) {
		super(parentShell);
		this.dependency = dependency;
	}

	public VersionedPackage getDependency() {
		return dependency;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Edit Dependency");
		
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout(2, false));
		GridData gd_contents = new GridData();
		gd_contents.widthHint = 350;
		contents.setLayoutData(gd_contents);
		
		Label lblName = new Label(contents, SWT.NONE);
		GridData gd_lblName = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblName.widthHint = 100;
		lblName.setLayoutData(gd_lblName);
		lblName.setText("Name");
		
		name = new Text(contents, SWT.BORDER);
		GridData gd_name = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_name.widthHint = 150;
		name.setLayoutData(gd_name);
		name.setEnabled(false);
		if (dependency.getName() != null) {
			name.setText(dependency.getName());
		}
		
		Label lblVersion = new Label(contents, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("Version");
		
		version = new Text(contents, SWT.BORDER);
		version.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (dependency.getVersion() != null) {
			version.setText(dependency.getVersion());
		}
		version.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dependency.setVersion(version.getText());
			}
		});
		
		new VersionSuggestion(dependency.getName(), parent, version, null, new WidgetFactory(null));
		
		return contents;
	}
}
