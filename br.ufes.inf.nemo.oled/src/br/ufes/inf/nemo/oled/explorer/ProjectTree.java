package br.ufes.inf.nemo.oled.explorer;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import RefOntoUML.Association;
import RefOntoUML.Comment;
import RefOntoUML.Generalization;
import RefOntoUML.GeneralizationSet;
import RefOntoUML.Package;
import RefOntoUML.Property;
import br.ufes.inf.nemo.common.ontoumlparser.OntoUMLParser;
import br.ufes.inf.nemo.oled.AppFrame;
import br.ufes.inf.nemo.oled.ProjectBrowser;
import br.ufes.inf.nemo.oled.dialog.properties.DialogCaller;
import br.ufes.inf.nemo.oled.model.UmlDiagram;
import br.ufes.inf.nemo.oled.model.UmlProject;
import br.ufes.inf.nemo.oled.popupmenu.TreePopupMenu;
import br.ufes.inf.nemo.oled.umldraw.structure.StructureDiagram;

public class ProjectTree extends CheckboxTree {

	private static final long serialVersionUID = 1L;
	public DefaultMutableTreeNode rootNode;
	public DefaultMutableTreeNode modelRootNode;
	public DefaultMutableTreeNode diagramRootNode;
	private DefaultTreeModel treeModel;
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	public AppFrame frame;
	public UmlProject project;
	private TreeCheckingModel checkingModel;
	
	/**
	 * Constructor.
	 */
	public ProjectTree (AppFrame frame, DefaultMutableTreeNode rootNode, UmlProject project, OntoUMLParser refparser)
	{
		super(rootNode);
		
		this.frame = frame;
		this.rootNode = rootNode;
		this.project = project;
		this.treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
				
		modelRootNode = new DefaultMutableTreeNode(new OntoUMLElement(project.getModel(),""));
		StructureDiagram diagram = new StructureDiagram(project, frame.getDiagramManager().getElementFactory());
		diagram.setName("Diagram");
		diagramRootNode = new DefaultMutableTreeNode(diagram);
		rootNode.add(diagramRootNode);
		rootNode.add(modelRootNode);		
		
		setDragEnabled(true);
		
		getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);
		
		ProjectTreeCellRenderer ontoCellRenderer = new ProjectTreeCellRenderer();
		setCellRenderer(ontoCellRenderer);
				
		checkingModel = getCheckingModel();
		
		drawModel(modelRootNode, project.getModel(), checkingModel, refparser);	
		drawDiagram(diagramRootNode, (List<? extends UmlDiagram>)project.getDiagrams());
		
		addCheckingPath(new TreePath(rootNode.getPath()));		
		expandPath(new TreePath(rootNode.getPath()));
		expandPath(new TreePath(diagramRootNode.getPath()));
		expandPath(new TreePath(modelRootNode.getPath()));
		
		/** Right Click Mouse Listener */
		addMouseListener(new MouseAdapter()
	    {
	        public void mouseClicked (MouseEvent e)
	        {	       	
	            mouseClickedActionPerformed(e);
	        }
	    });		
	}	

	/** 
	 * Remove all nodes except the root node. 
	 */
    public void clear() 
    {
        rootNode.removeAllChildren();
        treeModel.reload();
    }    
    	
	/**
	 * Show the Popup Menu
	 */
	public void doPopup (MouseEvent e, ProjectTree tree)
	{
		TreePath path = getPathForLocation(e.getX(), e.getY());
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(path.getLastPathComponent());
		if ((currentNode.getUserObject() instanceof OntoUMLElement)||(currentNode.getUserObject() instanceof StructureDiagram)){
			TreePopupMenu menu = new TreePopupMenu(frame, tree, currentNode.getUserObject());
	    	menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
		
	 /** Remove the currently selected node. */
    public void removeCurrentNode() 
    {
        TreePath currentSelection = getSelectionPath();
        if (currentSelection != null) 
        {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) 
            {
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        }  
        // Either there was no selection, or the root was selected.
        toolkit.beep();
    }
    
    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addObject(Object child) 
    {    		
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = getSelectionPath(); 
        if (parentPath == null) 
        {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
        } 
        return addObject(parentNode, child, true);
    }    
       
    @SuppressWarnings("rawtypes")
	public DefaultMutableTreeNode getNode(EObject eobject)
    {	
		Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) 
	    {
	    	EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    	if (obj.equals(eobject)) { 
	    		return node;	    		
	    	}	    		
	    	node = (DefaultMutableTreeNode)e.nextElement();
	    }
	    //last element
	    EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    if (obj.equals(eobject)){ 
	    	return node;	
	    }	  
	    return null;
    }
    
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) 
    {
    	return addObject(parent, child, false);
    }
    
    private DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) 
    {
    	if(child instanceof EObject){
    		DefaultMutableTreeNode node = getNode((EObject)child);
    		if(node!=null) return node;
    	}
    	
		DefaultMutableTreeNode childNode = createNode(child);		
		
		if (parent == null) parent = rootNode;		
		
		//It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
		
		//Make sure the user can see the lovely new node.
		if (shouldBeVisible) scrollPathToVisible(new TreePath(childNode.getPath()));
		
		return childNode;
    }
    
	public DefaultMutableTreeNode getModelRootNode() {
		return modelRootNode;
	}
	
	public DefaultMutableTreeNode getDiagramRootNode(){
		return diagramRootNode;
	}
	
	public DefaultMutableTreeNode getRootNode(){
		return rootNode;
	}
	
	private DefaultMutableTreeNode createNode(Object object)
	{
		if (object instanceof StructureDiagram)
		{
			return new DefaultMutableTreeNode(object);
			
		}else if (object instanceof RefOntoUML.Package || object instanceof RefOntoUML.Generalization || object instanceof RefOntoUML.GeneralizationSet || object instanceof RefOntoUML.Comment || object instanceof RefOntoUML.Constraintx) 
		{		
			return new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),""));
			
		}else if (object instanceof RefOntoUML.Property)
		{
			String alias = new String();
			OntoUMLParser refparser = ProjectBrowser.getParserFor(project);
			if (refparser!=null) alias = refparser.getAlias((RefOntoUML.Property)object);
			else alias = "";		
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),alias));
			checkingModel.setPathEnabled(new TreePath(node.getPath()),false);
			return node;
			
		}else if(object instanceof RefOntoUML.Classifier)
		{
			String alias = new String();
			OntoUMLParser refparser = ProjectBrowser.getParserFor(project);
			if (refparser!=null) alias = refparser.getAlias((RefOntoUML.Classifier)object);
			else alias = "";		
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),alias));
			
			for(Generalization g: ((RefOntoUML.Classifier) object).getGeneralization())
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)g),""));
				node.add(child);				
			}
				
			if (object instanceof RefOntoUML.Class)
			{				
				for (Property o: ((RefOntoUML.Class)object).getOwnedAttribute())
				{
					if (ProjectBrowser.getParserFor(project)!=null) alias = refparser.getAlias((RefOntoUML.Classifier)object);
					else alias = "";		
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)o),alias));
					node.add(child);
				}			
				for (Comment o: ((RefOntoUML.Class)object).getOwnedComment())
				{
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)o),""));
					node.add(child);
				}
			}		
			
			if (object instanceof RefOntoUML.DataType)
			{				
				for (Property o: ((RefOntoUML.DataType)object).getOwnedAttribute())
				{
					if (ProjectBrowser.getParserFor(project)!=null) alias = refparser.getAlias((RefOntoUML.Classifier)object);
					else alias = "";		
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)o),alias));
					node.add(child);
				}			
				for (Comment o: ((RefOntoUML.DataType)object).getOwnedComment())
				{
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)o),""));
					node.add(child);
				}
			}		
			
			if (object instanceof RefOntoUML.Association){
				for (RefOntoUML.Property o: ((RefOntoUML.Association)object).getMemberEnd())
				{
					if (ProjectBrowser.getParserFor(project)!=null) alias = refparser.getAlias((RefOntoUML.Classifier)object);
					else alias = "";		
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)o),alias));
					node.add(child);
					checkingModel.setPathEnabled(new TreePath(child.getPath()),false);
				}
				for (Comment o: ((RefOntoUML.Association)object).getOwnedComment())
				{
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)o),""));
					node.add(child);
				}
			}
			
			return node;
		}else{
			return new DefaultMutableTreeNode(object);
		}
	}
	
	private void drawDiagram(DefaultMutableTreeNode parent, List<? extends UmlDiagram> objectList)
	{
		for(Object obj: objectList){
			if (obj instanceof StructureDiagram)
			{
				StructureDiagram diagram = (StructureDiagram)obj;
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(diagram);
				parent.add(newNode);
			}
		}
	}

	/**
	 * Auxiliary function. It runs the Elements from the model creating the tree nodes.
	 */	
	private void drawModel(DefaultMutableTreeNode parent, Object object,TreeCheckingModel checkingModel,OntoUMLParser refparser) 
	{		
		/* Model */
		if (object instanceof RefOntoUML.Model) 
		{
			EList<EObject> contents = ((EObject)object).eContents();
			for (EObject eobj : contents) 
			{
				drawModel(parent, (RefOntoUML.Element) eobj,checkingModel,refparser);
			}
			
		/* Package */
		} else if (object instanceof RefOntoUML.Package) 
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),""));
			parent.add(newNode);
			expandPath(new TreePath(newNode.getPath()));
			
			EList<EObject> contents = ((EObject)object).eContents();
			for (EObject eobj : contents) 
			{
				drawModel(newNode, (RefOntoUML.Element) eobj,checkingModel,refparser);
			}
		
		/* Generalization */
		} else if (object instanceof RefOntoUML.Generalization)
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),""));			
			parent.add(newNode);			
		
		/* GeneralizationSet */
		}else if (object instanceof RefOntoUML.GeneralizationSet)
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),""));			
			parent.add(newNode);
			
		}else if (object instanceof RefOntoUML.Comment)
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),""));			
			parent.add(newNode);	
			
		/* Property */
		}else if (object instanceof RefOntoUML.Property)
		{
			String alias = new String();
			if (refparser!=null) alias = refparser.getAlias((RefOntoUML.Property)object);
			else alias = "";
			
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),alias));			
			parent.add(newNode);
			checkingModel.setPathEnabled(new TreePath(newNode.getPath()),false);		
		
		}else if (object instanceof RefOntoUML.Constraintx)
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),""));			
			parent.add(newNode);
			
		/* Classifier */
		} else if (object instanceof RefOntoUML.Classifier)		
		{
			String alias = new String();
			if (refparser!=null) alias = refparser.getAlias((RefOntoUML.Classifier)object);
			else alias = "";
			
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new OntoUMLElement(((EObject)object),alias));			
			parent.add(newNode);					
						
			//modelTree.collapsePath(new TreePath(newNode.getPath()));
			
			for(Generalization g: ((RefOntoUML.Classifier) object).getGeneralization())
			{
				drawModel(newNode,g,checkingModel,refparser);
			}
				
			if (object instanceof RefOntoUML.Class)
			{				
				for (Property o: ((RefOntoUML.Class)object).getOwnedAttribute())
				{
					drawModel(newNode,o,checkingModel,refparser);
				}			
				for (Comment o: ((RefOntoUML.Class)object).getOwnedComment())
				{
					drawModel(newNode,o,checkingModel,refparser);
				}
			}		
			
			if (object instanceof RefOntoUML.DataType)
			{				
				for (Property o: ((RefOntoUML.DataType)object).getOwnedAttribute())
				{
					drawModel(newNode,o,checkingModel,refparser);
				}			
				for (Comment o: ((RefOntoUML.DataType)object).getOwnedComment())
				{
					drawModel(newNode,o,checkingModel,refparser);
				}
			}		
			
			if (object instanceof RefOntoUML.Association){
				for (RefOntoUML.Property o: ((RefOntoUML.Association)object).getMemberEnd())
				{
					drawModel(newNode,o,checkingModel,refparser);
				}
				for (Comment o: ((RefOntoUML.Association)object).getOwnedComment())
				{
					drawModel(newNode,o,checkingModel,refparser);
				}
			}
		}
		
	}
		
	/**
	 * Get Checked Elements.
	 * 
	 * @param modeltree
	 * @return
	 */
	public List<EObject> getModelCheckedElements ()
	{
		List<EObject> uncheckedNodes = new ArrayList<EObject>();
		List<EObject> checkedNodes = new ArrayList<EObject>();
	    TreePath[] treepathList = getCheckingPaths();
	    	
	    for (TreePath treepath : treepathList) 
	    {	    	
	    	DefaultMutableTreeNode node = ((DefaultMutableTreeNode)treepath.getLastPathComponent());
	    	if (node.getUserObject() instanceof OntoUMLElement)
	    		checkedNodes.add(((OntoUMLElement)node.getUserObject()).getElement());	    		    	
	    }
		    
		OntoUMLElement rootObject = (OntoUMLElement) modelRootNode.getUserObject();
	    	    
		initModelUncheckeNodes(rootObject.getElement(), checkedNodes, uncheckedNodes);
    	    	
	    return checkedNodes;
	}
	
	/**
	 * Get Unchecked Elements.
	 * 
	 * @param modeltree
	 * @return
	 */
	public List<EObject> getModelUncheckedElements ()
	{
		List<EObject> uncheckedNodes = new ArrayList<EObject>();
		List<EObject> checkedNodes = new ArrayList<EObject>();
	    TreePath[] treepathList = getCheckingPaths();
	    	
	    for (TreePath treepath : treepathList) 
	    {	    	
	    	DefaultMutableTreeNode node = ((DefaultMutableTreeNode)treepath.getLastPathComponent());
	    	if (node.getUserObject() instanceof OntoUMLElement)
	    		checkedNodes.add(((OntoUMLElement)node.getUserObject()).getElement());	    		    	
	    }
		    
		OntoUMLElement rootObject = (OntoUMLElement) modelRootNode.getUserObject();
	    	    
		initModelUncheckeNodes(rootObject.getElement(), checkedNodes, uncheckedNodes);
    	    	
	    return uncheckedNodes;
	}
	
	/**
	 * Check Node.
	 * 
	 * @param node
	 * @param safe
	 * @param modeltree
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public void checkNode(DefaultMutableTreeNode node, boolean safe)
	{		
		EObject childObject;		
		addCheckingPath(new TreePath(node.getPath()));		
		
		Object userobj = node.getUserObject();
		Object obj=null;
		if (userobj instanceof OntoUMLElement) obj = ((OntoUMLElement)userobj).getElement();
		else if (userobj instanceof StructureDiagram) obj = ((StructureDiagram)userobj);
		else if (userobj instanceof UmlProject) obj = ((UmlProject)userobj);
		
		//unselected children only if was different than Association, Diagram or Project
    	if(safe && obj!=null && node.getChildCount()>0 && !(obj instanceof Association) && !(obj instanceof StructureDiagram) && !(obj instanceof UmlProject)) 
    	{
			Enumeration e = node.breadthFirstEnumeration();
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)e.nextElement();		
			
			while (e.hasMoreElements()) 
	    	{
				childNode = (DefaultMutableTreeNode)e.nextElement();
				childObject = ((OntoUMLElement)childNode.getUserObject()).getElement();		    		
				getCheckingModel().removeCheckingPath(new TreePath(childNode.getPath()));				
			}
    	}
	}
	
	/**
	 * Uncheck Node.
	 * 
	 * @param node
	 * @param safe
	 * @param modeltree
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public void uncheckNode(DefaultMutableTreeNode node, boolean safe)
	{		
		EObject childObject;		
		removeCheckingPath(new TreePath(node.getPath()));		
		
		Object userobj = node.getUserObject();
		Object obj=null;
		if (userobj instanceof OntoUMLElement) obj = ((OntoUMLElement)userobj).getElement();
		else if (userobj instanceof StructureDiagram) obj = ((StructureDiagram)userobj);
		else if (userobj instanceof UmlProject) obj = ((UmlProject)userobj);
		
		//unselected children only if was different than Association, Diagram or Project
    	if(safe && obj!=null && node.getChildCount()>0 && !(obj instanceof Association) && !(obj instanceof StructureDiagram) && !(obj instanceof UmlProject)) 
    	{
			Enumeration e = node.breadthFirstEnumeration();
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)e.nextElement();		
			
			while (e.hasMoreElements()) 
	    	{
				childNode = (DefaultMutableTreeNode)e.nextElement();
				childObject = ((OntoUMLElement)childNode.getUserObject()).getElement();		    		
				getCheckingModel().addCheckingPath(new TreePath(childNode.getPath()));				
			}
    	}
	}
	@SuppressWarnings("rawtypes")
	public boolean selectModelElement(EObject element)
	{	
		boolean result = false;
		EObject rootEObj = ((OntoUMLElement)modelRootNode.getUserObject()).getElement();
		if (rootEObj.equals(element)) { result=true; select(modelRootNode); return result; }
		
		Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) 
	    {
	    	EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    	if (obj.equals(element)) { 
	    		result =true;
	    		this.setSelectionPath(new TreePath(node.getPath()));
	    		return result;
	    	}
	    		
	    	node = (DefaultMutableTreeNode)e.nextElement();
	    }
	    //last element
	    EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    if (obj.equals(element)){ 
	    	result =true;
	    	this.setSelectionPath(new TreePath(node.getPath()));	 
	    	return result;
	    }	    
	    return result;	    
	}
	
	public void select (DefaultMutableTreeNode  node)
	{
		this.setSelectionPath(new TreePath(node.getPath()));		
	}
		
	@SuppressWarnings("rawtypes")
	public ArrayList<DefaultMutableTreeNode> find(String elementName)
	{		
		ArrayList<DefaultMutableTreeNode> list = new ArrayList<DefaultMutableTreeNode>();
		Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) 
	    {
	    	EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    	if (obj instanceof RefOntoUML.NamedElement){
	    		RefOntoUML.NamedElement namedElem = ((RefOntoUML.NamedElement)obj);	    		
	    		if(namedElem.getName()!=null && !namedElem.getName().isEmpty()){
			    	if (namedElem.getName().contains(elementName) || namedElem.getName().equalsIgnoreCase(elementName)) {			    		
			    		list.add(node);			    		
			    	}
	    		}
	    	}
	    	node = (DefaultMutableTreeNode)e.nextElement();
	    }
	    //last element
	    EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    if (obj instanceof RefOntoUML.NamedElement){
    		RefOntoUML.NamedElement namedElem = ((RefOntoUML.NamedElement)obj);    		
    		if(namedElem.getName()!=null && !namedElem.getName().isEmpty()){
		    	if (namedElem.getName().contains(elementName) || namedElem.getName().equalsIgnoreCase(elementName)) {		    		
		    		list.add(node);		    		
		    	}
    		}
    	}    
	    return list;
	}
	
	/**
	 * Check this elements.
	 * 
	 * @param elements
	 * @param modeltree
	 */
	@SuppressWarnings("rawtypes")
	public void checkModelElements(List<EObject> elements, boolean safe) 
	{			   
		List<EObject> alreadyChecked = getModelCheckedElements();		
	    
		alreadyChecked.removeAll(elements);
		alreadyChecked.addAll(elements);		
		
	    Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) 
	    {
	    	EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    	if (alreadyChecked.contains(obj)) { checkNode(node,true); }	    			
	    		    		
	    	node = (DefaultMutableTreeNode)e.nextElement();	    
	    }
	    //last element
	    EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    if (alreadyChecked.contains(obj)) { checkNode(node,true); }    	    	
	}	
	 
	/**
	 * Check this elements.
	 * 
	 * @param elements
	 * @param modeltree
	 */
	@SuppressWarnings("rawtypes")
	public void uncheckModelElements(List<EObject> elements, boolean safe) 
	{			   
		List<EObject> alreadyUnchecked = getModelUncheckedElements();		
	    
		alreadyUnchecked.removeAll(elements);
		alreadyUnchecked.addAll(elements);		
		
	    Enumeration e = modelRootNode.breadthFirstEnumeration();
	    DefaultMutableTreeNode  node = (DefaultMutableTreeNode)e.nextElement();
	    while (e.hasMoreElements()) 
	    {
	    	EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    	if (alreadyUnchecked.contains(obj)) { uncheckNode(node,true); }	    			
	    		    		
	    	node = (DefaultMutableTreeNode)e.nextElement();	    
	    }
	    //last element
	    EObject obj = ((OntoUMLElement)node.getUserObject()).getElement();
	    if (alreadyUnchecked.contains(obj)) { uncheckNode(node,true); }    	    	
	}	
	
	/**
	 * Initialize Unchecked Nodes.
	 */
	private void initModelUncheckeNodes(EObject element, List<EObject> checkedElements,List<EObject> uncheckedElements) 
	{    	
    	if (element == null) return;    	
    	
    	Object[] elemArray = element.eContents().toArray();
		for (Object obj : elemArray) 
		{
			initModelUncheckeNodes((EObject)obj, checkedElements, uncheckedElements);
		}
    	
    	if (!checkedElements.contains(element)) 
    	{
    		if (element instanceof Package) 
    		{    			
    			// nothing to do in this case...
    		} else {    			
    			uncheckedElements.add(element);    			
    		}
    	}	
    }
	
	public void mouseClickedActionPerformed(MouseEvent e)
	{
		if (SwingUtilities.isRightMouseButton(e))
        {	            	
        	TreePath path = getPathForLocation ( e.getX (), e.getY () );
        	setSelectionPath(path);
            Rectangle pathBounds = getUI().getPathBounds(ProjectTree.this, path);
            if (pathBounds != null && pathBounds.contains(e.getX (),e.getY()))
            {
            	doPopup(e,ProjectTree.this);
            }	                
        }
        if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e))
        {
        	TreePath path = getPathForLocation (e.getX (), e.getY () );
        	if (path!=null){
            	DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(path.getLastPathComponent());
            	
            	// open diagram
            	if (currentNode.getUserObject() instanceof StructureDiagram && !currentNode.equals(diagramRootNode))
            	{
            		StructureDiagram diagram = (StructureDiagram)currentNode.getUserObject();
            		if (!ProjectTree.this.frame.getDiagramManager().isDiagramOpened(diagram)) {
            			// create the diagram visualization again
            			ProjectTree.this.frame.getDiagramManager().createDiagramEditor(diagram);
            		}
            	}
            	
            	if(currentNode.getUserObject() instanceof OntoUMLElement)
            	{
            		RefOntoUML.Element element = (RefOntoUML.Element)((OntoUMLElement)currentNode.getUserObject()).getElement();
            		if (element instanceof RefOntoUML.Class){
            			DialogCaller.callClassDialog(
            				(JFrame)ProjectTree.this.frame,
            				ProjectTree.this.frame.getDiagramManager(),            				
            				(RefOntoUML.Classifier)element,
            				true
            			);
            		} else if (element instanceof RefOntoUML.Property){
            			DialogCaller.callPropertyDialog(
    						(JFrame)ProjectTree.this.frame,
            				ProjectTree.this.frame.getDiagramManager(),            				
            				(RefOntoUML.Property)element,
            				true
            			);
            		} else if (element instanceof RefOntoUML.Association){
            			DialogCaller.callAssociationDialog(
    						(JFrame)ProjectTree.this.frame,
            				ProjectTree.this.frame.getDiagramManager(),            				
            				(RefOntoUML.Association)element,
            				true
            			);        				
            		} else if (element instanceof RefOntoUML.Comment)
            		{
            			DialogCaller.callCommentDialog(
	            			(JFrame)ProjectTree.this.frame,
	            			ProjectTree.this.frame.getDiagramManager(),	            			
	            			(RefOntoUML.Comment)element,
	            			true
	            		);	            			
            		} else if (element instanceof RefOntoUML.Constraintx)
            		{
            			DialogCaller.callConstraintxDialog(
	            			(JFrame)ProjectTree.this.frame,
	            			ProjectTree.this.frame.getDiagramManager(),	            			
	            			(RefOntoUML.Constraintx)element,
	            			true
	            		);                  			
            		}else if (element instanceof RefOntoUML.Generalization)
            		{
            			DialogCaller.callGeneralizationDialog(
            				(JFrame)ProjectTree.this.frame,
            				ProjectTree.this.frame.getDiagramManager(),            				
            				(RefOntoUML.Generalization)element,
            				true
            			);
            		} else if (element instanceof RefOntoUML.GeneralizationSet)
            		{            			
            			DialogCaller.callGeneralizationSetDialog(
            				(JFrame)ProjectTree.this.frame,
            				ProjectTree.this.frame.getDiagramManager(),
            				(GeneralizationSet)element,            				
            				true
            			);
            		}
            	}
        	}
        }
	}
}
