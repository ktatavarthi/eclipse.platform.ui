package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Shows the properties of a new or existing task, or a problem.
 */
/* package */
class TaskPropertiesDialog extends Dialog {
	
	/**
	 * The TaskList view.
	 */
	private TaskList taskList;
	
	/**
	 * The task or problem being shown, or <code>null</code> for a new task.
	 */
	private IMarker marker = null;
	
	/**
	 * The resource on which to create a new task.
	 */
	private IResource resource = null;

	/**
	 * The initial attributes to use when creating a new task.
	 */
	private Map initialAttributes = null;
	
	/**
	 * The text control for the Description field.
	 */
	private Text descriptionText;
	
	/**
	 * The combo box control for the Priority field.
	 */
	private Combo priorityCombo;

	/**
	 * The checkbox button for the Completed field.
	 */
	private Button completedCheckbox;

	/**
	 * The control for the Severity field.
	 */
//	private Combo severityCombo;
	private Label severityLabel;
	
	/**
	 * The text control for the Resource field.
	 */
	private Text resourceText;
	
	/**
	 * The text control for the Folder field.
	 */
	private Text folderText;

	/**
	 * The text control for the Location field.
	 */
	private Text locationText;
	
	/**
	 * Dirty flag.  True if any changes have been made.
	 */
	private boolean dirty;

/**
 * Creates a properties dialog for a new task.
 * 
 * @param taskList the task list view
 */
public TaskPropertiesDialog(TaskList taskList) {
	super(taskList.getSite().getShell());
	this.taskList = taskList;
}

/**
 * Creates a properties dialog for an existing task or problem.
 * 
 * @param taskList the task list view
 * @param marker the marker to edit, or <code>null</code> to create a new task
 */
public TaskPropertiesDialog(TaskList taskList, IMarker marker) {
	super(taskList.getSite().getShell());
	this.taskList = taskList;
	this.marker = marker;
}

/**
 * Returns the task list view.
 */
public TaskList getTaskList() {
	return taskList;
}

/**
 * Returns the marker being created or modified.
 */
public IMarker getMarker() {
	return marker;
}

/**
 * Sets the resource to use when creating a new task.
 * If not set, the new task is created on the workspace root.
 */
public void setResource(IResource resource) {
	this.resource = resource;
}

/**
 * Returns the resource to use when creating a new task,
 * or <code>null</code> if none has been set.
 * If not set, the new task is created on the workspace root.
 */
public IResource getResource() {
	return resource;
}

/**
 * Sets initial attributes to use when creating a new task.
 * If not set, the new task is created with default attributes.
 */
public void setInitialAttributes(Map initialAttributes) {
	this.initialAttributes = initialAttributes;
}

/**
 * Returns the initial attributes to use when creating a new task,
 * or <code>null</code> if not set.
 * If not set, the new task is created with default attributes.
 */
public Map getInitialAttributes() {
	return initialAttributes;
}

/* (non-Javadoc)
 * Method declared on Window.
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	if (marker == null) {
		newShell.setText(TaskListMessages.getString("TaskProp.newTaskTitle")); //$NON-NLS-1$
	}
	else {
		String msg = MarkerUtil.getMessage(marker);
		String kind = MarkerUtil.getKindText(marker);
		newShell.setText(TaskListMessages.format("TaskProp.titleFmt", new Object[] { msg, kind })); //$NON-NLS-1$
	}
	WorkbenchHelp.setHelp(newShell, ITaskListHelpContextIds.PROPERTIES_DIALOG);
}

/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	Composite composite = (Composite) super.createDialogArea(parent);
	initializeDialogUnits(composite);
	createDescriptionArea(composite);
	if (isTask()) {
		createPriorityAndStatusArea(composite);
	}
	else {
		createSeverityArea(composite);
	}
	createResourceArea(composite);
	updateDialogFromMarker();
	return composite;
}

/**
 * Creates only the OK button if showing problem properties, otherwise creates
 * both OK and Cancel buttons.
 */
protected void createButtonsForButtonBar(Composite parent) {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	if (isTask()) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
}

/**
 * Creates the area.for the Description field.
 */
private void createDescriptionArea(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setLayout(layout);
	Label label = new Label(composite, SWT.NONE);
	label.setText(TaskListMessages.getString("TaskProp.description")); //$NON-NLS-1$
	int style = SWT.SINGLE | SWT.WRAP | SWT.BORDER;
	if (!isTask()) {
		style |= SWT.READ_ONLY;
	}
	descriptionText = new Text(composite, style);
	GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	gridData.widthHint = convertHorizontalDLUsToPixels(400);
	descriptionText.setLayoutData(gridData);
}

/**
 * Creates the area.for the Priority and Status fields.
 */
private void createPriorityAndStatusArea(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	composite.setLayout(layout);
	
	Label label = new Label(composite, SWT.NONE);
	label.setText(TaskListMessages.getString("TaskProp.priority")); //$NON-NLS-1$
	priorityCombo = new Combo(composite, SWT.READ_ONLY);
	priorityCombo.setItems(new String[] {
		TaskListMessages.getString("TaskList.high"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.normal"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.low") //$NON-NLS-1$
	});
	
	completedCheckbox = new Button(composite, SWT.CHECK);
	completedCheckbox.setText(TaskListMessages.getString("TaskProp.completed")); //$NON-NLS-1$
	GridData gridData = new GridData();
	gridData.horizontalIndent = convertHorizontalDLUsToPixels(20);
	completedCheckbox.setLayoutData(gridData);
}

/**
 * Creates the area.for the Severity fields.
 */
private void createSeverityArea(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setLayout(layout);
	
	Label label = new Label(composite, SWT.NONE);
	label.setText(TaskListMessages.getString("TaskProp.severity")); //$NON-NLS-1$
	
	// workaround for bug 11078: Can't get a read-only combo box
	severityLabel = new Label(composite, SWT.NONE);	
/*
	severityCombo = new Combo(composite, SWT.READ_ONLY);
	severityCombo.setItems(new String[] {
		TaskListMessages.getString("TaskList.error"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.warning"), //$NON-NLS-1$
		TaskListMessages.getString("TaskList.info") //$NON-NLS-1$
	});
*/
}

/**
 * Creates the area.for the Resource field.
 */
private void createResourceArea(Composite parent) {
	Composite composite = new Composite(parent, SWT.NONE);
	GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	composite.setLayoutData(gridData);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setLayout(layout);
	
	Label resourceLabel = new Label(composite, SWT.NONE);
	resourceLabel.setText(TaskListMessages.getString("TaskProp.onResource")); //$NON-NLS-1$
	resourceText = new Text(composite, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	resourceText.setLayoutData(gridData);
	
	Label folderLabel = new Label(composite, SWT.NONE);
	folderLabel.setText(TaskListMessages.getString("TaskProp.inFolder")); //$NON-NLS-1$
	folderText = new Text(composite, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	folderText.setLayoutData(gridData);
	
	Label locationLabel = new Label(composite, SWT.NONE);
	locationLabel.setText(TaskListMessages.getString("TaskProp.location")); //$NON-NLS-1$
	locationText = new Text(composite, SWT.SINGLE | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
	gridData = new GridData(GridData.FILL_HORIZONTAL);
	locationText.setLayoutData(gridData);
}

/**
 * Updates the dialog from the marker state.
 */
private void updateDialogFromMarker() {
	if (marker == null) {
		updateDialogForNewMarker();
		return;
	}
	descriptionText.setText(MarkerUtil.getMessage(marker));
	if (isTask()) {
		priorityCombo.clearSelection();
		priorityCombo.select(IMarker.PRIORITY_HIGH - MarkerUtil.getPriority(marker));
		completedCheckbox.setSelection(MarkerUtil.isComplete(marker));
		markDirty();
	}
	else {
/* 	workaround for bug 11078: Can't get a read-only combo box
 		severityCombo.clearSelection();
		severityCombo.select(IMarker.SEVERITY_ERROR - MarkerUtil.getSeverity(marker));
*/		
		String sev = "";
		switch (MarkerUtil.getSeverity(marker)) {
			case IMarker.SEVERITY_ERROR:
				sev = TaskListMessages.getString("TaskList.error"); //$NON-NLS-1$
				break;
			case IMarker.SEVERITY_WARNING:
				sev = TaskListMessages.getString("TaskList.warning"); //$NON-NLS-1$
				break;
			case IMarker.SEVERITY_INFO:
				sev = TaskListMessages.getString("TaskList.info"); //$NON-NLS-1$
				break;
		}
		severityLabel.setText(sev);

	}
	resourceText.setText(MarkerUtil.getResourceName(marker));
	folderText.setText(MarkerUtil.getContainerName(marker));
	locationText.setText(MarkerUtil.getLineAndLocation(marker));
}

/**
 * Updates the dialog to reflect the state for a new marker.
 */
private void updateDialogForNewMarker() {
	Map attrs = getInitialAttributes();

	int pri = IMarker.PRIORITY_NORMAL;
	if (attrs != null) {
		Object o = attrs.get(IMarker.PRIORITY);
		if (o instanceof Integer) {
			int val = ((Integer) o).intValue();
			if (val >= IMarker.PRIORITY_LOW && val <= IMarker.PRIORITY_HIGH) {
				pri = val;
			}
		}
	}
	priorityCombo.deselectAll();
	priorityCombo.select(IMarker.PRIORITY_HIGH - pri);
	
	boolean completed = false;
	if (attrs != null) {
		Object o = attrs.get(IMarker.DONE);
		if (o instanceof Boolean) {
			completed = ((Boolean) o).booleanValue();
		}
	}
	completedCheckbox.setSelection(completed);
	
	IResource resource = getResource();
	if (resource != null) {
		resourceText.setText(resource.getName());
		IResource parent = resource.getParent();
		folderText.setText(parent == null ? "" : parent.getFullPath().toString().substring(1));
	}
	
	int line = -1;
	String loc = "";
	if (attrs != null) {
		Object o = attrs.get(IMarker.LINE_NUMBER);
		if (o instanceof Integer) {
			line = ((Integer) o).intValue();
		}
		o = attrs.get(IMarker.LOCATION);
		if (o instanceof String) {
			loc = (String) o;
		}
	}
	locationText.setText(MarkerUtil.getLineAndLocation(line, loc));
	
	markDirty();
	return;
	
}

/* (non-Javadoc)
 * Method declared on Dialog
 */
protected void okPressed() {
	saveChanges();
	super.okPressed();
}

private void markDirty() {
	dirty = true;
}

private boolean isDirty() {
	return dirty;
}

/**
 * Returns <code>true</code> if a task is being created or modified.
 * Returns <code>false</code> if a problem is being shown.
 */
private boolean isTask() {
	return marker == null || MarkerUtil.isMarkerType(marker, IMarker.TASK);
}

/**
 * Saves the changes made in the dialog if needed.
 * Creates a new task if needed.
 * Updates the existing task only if there have been changes.
 * Does nothing for problems, since they cannot be modified.
 */
private void saveChanges() {
	if (!isTask() || !isDirty()) {
		return;
	}
	try {
		getTaskList().getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (marker == null) {
					IResource resource = getResource();
					if (resource == null) {
						resource = getTaskList().getWorkspace().getRoot();
					}
					marker = resource.createMarker(IMarker.TASK);
				}
				marker.setAttributes(getMarkerAttributesFromDialog());
			}
		}, null);
	} catch (CoreException e) {
		ErrorDialog.openError(
			getShell(),
			TaskListMessages.getString("TaskProp.errorMessage"), //$NON-NLS-1$
			null,
			e.getStatus());
		return;
	}
}

/**
 * Returns the marker attributes to save back to the marker, 
 * based on the current dialog fields.
 */
private Map getMarkerAttributesFromDialog() {
	Map attribs = new HashMap(11);
	if (isTask()) {
		attribs.put(IMarker.MESSAGE, descriptionText.getText());
		int i = priorityCombo.getSelectionIndex();
		if (i != -1) {
			attribs.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_HIGH - i));
		}
		attribs.put(IMarker.DONE, completedCheckbox.getSelection() ? Boolean.TRUE : Boolean.FALSE);
	}
	return attribs;
}

}