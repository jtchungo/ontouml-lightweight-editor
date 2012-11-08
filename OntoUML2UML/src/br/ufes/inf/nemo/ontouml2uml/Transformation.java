package br.ufes.inf.nemo.ontouml2uml;

import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;

/**
 * @author John Guerson
 */

public class Transformation {

	public Dealer mydealer;	 
	public HashMap <RefOntoUML.Package,org.eclipse.uml2.uml.Package> packagesMap;			
	public RefOntoUML.Package refmodel;
	
	/** 
	 * Constructor 
	 */
	public Transformation(RefOntoUML.Package model)  
    { 
    	mydealer = new Dealer(model);  
    	packagesMap = new HashMap<RefOntoUML.Package,org.eclipse.uml2.uml.Package>(); 
    	refmodel = model;
    }
	
	/**
	 * Transforming...
	 */
	public org.eclipse.uml2.uml.Package Transform ()
    {           
		Dealer.outln("Transforming OntoUML Model to UML...");
		
        org.eclipse.uml2.uml.Package umlmodel = org.eclipse.uml2.uml.UMLFactory.eINSTANCE.createModel();
                
        mydealer.DealNamedElement((RefOntoUML.NamedElement) refmodel, (org.eclipse.uml2.uml.NamedElement) umlmodel);
        mydealer.RelateElements((RefOntoUML.Element) refmodel, (org.eclipse.uml2.uml.Element) umlmodel);
        
        packagesMap.put(refmodel, umlmodel);
                
        TransformingPackages(refmodel,umlmodel);
        
        VerifyElements();
                    
        TransformingPrimitiveTypes();        
        TransformingEnumerations();        
        TransformingDataTypes();
        
        TransformingClasses();        
        TransformingAssociations();        
        TransformingGeneralizations();        
        TransformingGeneralizationSets();
            
        Dealer.outln("Executed Succesfully.");
        
        return umlmodel;
    }
		
    /** 
     * Transforming Packages. 
     */  
    public void TransformingPackages (RefOntoUML.Package refmodel, org.eclipse.uml2.uml.Package umlmodel)
    {               
        for (EObject obj : refmodel.eContents())
        { 
            if (obj instanceof RefOntoUML.Package)
            {
            	org.eclipse.uml2.uml.Package umlpackage = org.eclipse.uml2.uml.UMLFactory.eINSTANCE.createPackage();
        		mydealer.DealPackage( (RefOntoUML.Package)obj,umlpackage);            	            		
        		umlmodel.getPackagedElements().add(umlpackage); 
        		packagesMap.put((RefOntoUML.Package)obj, umlpackage);
        		TransformingPackages((RefOntoUML.Package)obj,umlpackage);        						            	
            }
        }
	}	
	
    /** 
     * Verifying Non Permitted Elements: Non Classifiers, Non GeneralizationSet and Non Dependency 
     */
	public void VerifyElements ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;						
	        	
			for (EObject elem : refmodel.eContents())
	        {            
				if (!(elem instanceof RefOntoUML.Classifier) &&
					!(elem instanceof RefOntoUML.GeneralizationSet) &&
					!(elem instanceof RefOntoUML.Dependency) &&
					!(elem instanceof RefOntoUML.Package) &&
					!(elem instanceof RefOntoUML.Comment))
				{
					System.err.print("# Unsupported Class: "+elem.getClass().getName());                
				}
	        }
		}
	}	
		
    /**
     * Transforming Classes. 
     */
	public void TransformingClasses ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;
			org.eclipse.uml2.uml.Package umlmodel = packagesMap.get((RefOntoUML.Package)obj);			
	        
			for (EObject elem : refmodel.eContents())
	        {        	
	            if (elem instanceof RefOntoUML.Class)
	            {
	          		org.eclipse.uml2.uml.Class umlclass = org.eclipse.uml2.uml.UMLFactory.eINSTANCE.createClass();  
	           		mydealer.DealClass( (RefOntoUML.Class) elem, umlclass);	           			           		
	                umlmodel.getPackagedElements().add(umlclass);
	            }
	        }
		}			        
	}
		
	/**
	 * Transforming PrimitiveTypes. 
	 */
	public void TransformingPrimitiveTypes ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;
			org.eclipse.uml2.uml.Package umlmodel = packagesMap.get((RefOntoUML.Package)obj);			
	        
			for (EObject elem : refmodel.eContents())
	        {        	
				if (elem instanceof RefOntoUML.PrimitiveType)
	            {
	                 org.eclipse.uml2.uml.PrimitiveType dt2 = mydealer.DealPrimitiveType ((RefOntoUML.PrimitiveType) elem);	                 	                 		           		
	                 umlmodel.getPackagedElements().add(dt2);                               
	            }
	        }
		}
	}		
	
	/**
	 * Transforming Enumerations. 
	 */
	public void TransformingEnumerations ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;
			org.eclipse.uml2.uml.Package umlmodel = packagesMap.get((RefOntoUML.Package)obj);			
	        
			for (EObject elem : refmodel.eContents())
	        {        	
				if (elem instanceof RefOntoUML.Enumeration) 
	            {
	                 org.eclipse.uml2.uml.Enumeration dt2 = mydealer.DealEnumeration ((RefOntoUML.Enumeration) elem);
	                 umlmodel.getPackagedElements().add(dt2);                               
	            }
	        }
		}
	}
		
	/** 
	 * Transforming DataTypes. 
	 */
	public void TransformingDataTypes ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;
			org.eclipse.uml2.uml.Package umlmodel = packagesMap.get((RefOntoUML.Package)obj);			
	        
			for (EObject elem : refmodel.eContents())
	        {        	
				if ((elem instanceof RefOntoUML.DataType) && !(elem instanceof RefOntoUML.PrimitiveType) && !(elem instanceof RefOntoUML.Enumeration))  
	            {
	                 org.eclipse.uml2.uml.DataType dt2 = mydealer.DealDataType ((RefOntoUML.DataType) elem);
	                 umlmodel.getPackagedElements().add(dt2);                               
	            }
	        }
		}
	}		
		
	/**
	 * Transforming Associations. 
	 */
	public void TransformingAssociations ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;
			org.eclipse.uml2.uml.Package umlmodel = packagesMap.get((RefOntoUML.Package)obj);			
	        
			for (EObject elem : refmodel.eContents())
	        {        	
				if(elem instanceof RefOntoUML.Derivation)
				{
					org.eclipse.uml2.uml.Association assoc = mydealer.DealAssociation ((RefOntoUML.Association) elem);
					umlmodel.getPackagedElements().add(assoc);
					
				} else if( elem instanceof RefOntoUML.Association)
				{        		
					org.eclipse.uml2.uml.Association assoc = mydealer.DealAssociation ((RefOntoUML.Association) elem);
					umlmodel.getPackagedElements().add(assoc);        		
				}
	        }
        }
	}	
		
	/**
	 * Transforming Generalizations. 
	 */
	public void TransformingGeneralizations ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;						
	        	
			for (EObject elem : refmodel.eContents())
	        {	
				if (elem instanceof RefOntoUML.Classifier)
				{                    	    
					mydealer.ProcessGeneralizations((RefOntoUML.Classifier)elem);
				}
	        }         	
		}
	}		
	
	/**
	 * Transforming Generalization Sets. 
	 */
	public void TransformingGeneralizationSets ()
	{
		for (EObject obj: packagesMap.keySet())
		{
			RefOntoUML.Package refmodel = (RefOntoUML.Package)obj;
			org.eclipse.uml2.uml.Package umlmodel = packagesMap.get((RefOntoUML.Package)obj);			
	        	
			for (EObject elem : refmodel.eContents())
	        {	            
				if (elem instanceof RefOntoUML.GeneralizationSet)
				{
					org.eclipse.uml2.uml.GeneralizationSet gs2 = mydealer.DealGeneralizationSet ((RefOntoUML.GeneralizationSet) elem);        
					umlmodel.getPackagedElements().add(gs2);
				}
	        }
		}
	}
}