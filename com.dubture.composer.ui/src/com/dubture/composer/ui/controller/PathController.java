package com.dubture.composer.ui.controller;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.getcomposer.core.collection.JsonArray;

import com.dubture.composer.ui.ComposerUIPluginImages;

public class PathController extends LabelProvider implements IStructuredContentProvider {

	private JsonArray paths;
	protected Image pathImage = ComposerUIPluginImages.PACKAGE_FOLDER.createImage();
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		paths = (JsonArray)newInput;
	}
	
	@Override
	public Image getImage(Object element) {
		return pathImage;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return paths.toArray();
	}
}
