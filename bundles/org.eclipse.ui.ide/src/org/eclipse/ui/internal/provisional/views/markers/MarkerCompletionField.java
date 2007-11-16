/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkerCompletionField is the class that specifies the completion entry.
 * 
 * @since 3.4
 * 
 */
public class MarkerCompletionField extends MarkerField {

	static final String COMPLETE_IMAGE_PATH = "$nl$/icons/full/obj16/complete_tsk.gif"; //$NON-NLS-1$

	static final String INCOMPLETE_IMAGE_PATH = "$nl$/icons/full/obj16/incomplete_tsk.gif"; //$NON-NLS-1$

	private static final int DONE = 2;
	private static final int NOT_DONE = 1;
	private static final int UNDEFINED = 0;

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerCompletionField() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getDefaultColumnWidth(org.eclipse.swt.widgets.Control)
	 */
	public int getDefaultColumnWidth(Control control) {
		return getCompleteImage().getBounds().width
				+ IDialogConstants.BUTTON_MARGIN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return MarkerSupportConstants.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return MarkerSupportConstants.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getImage(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public Image getImage(MarkerItem item) {

		switch (getDoneConstant(item)) {
		case DONE:
			return getCompleteImage();
		case NOT_DONE:
			return MarkerSupportInternalUtilities
					.createImage(INCOMPLETE_IMAGE_PATH);
		default:
			return null;
		}
	}

	/**
	 * Return the constant that indicates whether or not the receiver is done
	 * 
	 * @param item
	 * @return 1 if it is done, 0 if it not and -1 if it cannot be determined.
	 */
	private int getDoneConstant(MarkerItem item) {

		int done = UNDEFINED;

		if (item.isConcrete()
				&& item.getAttributeValue(IMarker.USER_EDITABLE, true)) {
			done = 0;
			if (item.getAttributeValue(IMarker.DONE, false)) {
				done = 1;
			}
		}
		return done;
	}

	/**
	 * Return the image for task completion.
	 * 
	 * @return Image
	 */
	private Image getCompleteImage() {
		return MarkerSupportInternalUtilities.createImage(COMPLETE_IMAGE_PATH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#getColumnTooltipText()
	 */
	public String getColumnTooltipText() {
		return MarkerMessages.completion_description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.MarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem,
	 *      org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		return getDoneConstant(item2) - getDoneConstant(item1);
	}

}
