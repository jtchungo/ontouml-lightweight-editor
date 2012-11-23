package br.ufes.inf.nemo.ocl2alloy.transformer;

import org.eclipse.uml2.uml.Constraint;

import br.ufes.inf.nemo.common.ontoumlparser.OntoUMLParser;

import br.ufes.inf.nemo.ocl2alloy.exception.IteratorException;
import br.ufes.inf.nemo.ocl2alloy.exception.LiteralException;
import br.ufes.inf.nemo.ocl2alloy.exception.OperationException;
import br.ufes.inf.nemo.ocl2alloy.exception.StereotypeException;
import br.ufes.inf.nemo.ocl2alloy.exception.TypeException;

import br.ufes.inf.nemo.ocl2alloy.parser.OCLParser;

import br.ufes.inf.nemo.ocl2alloy.visitor.ToAlloyVisitor;

public class Transformer {	
	
	public static String log;
		
	public static Boolean succeeds;
	
	public static String TransformConstraint (Constraint ct, String stereo, OCLParser oclparser, OntoUMLParser refparser)
	{
		String result = new String();		
		
		log = new String();
		
		succeeds = false;
				
		ToAlloyVisitor myVisitor = new ToAlloyVisitor(oclparser,refparser);
						
		if(stereo.equals("FACT")) myVisitor.stereo_invariant="FACT";
		
		if(stereo.equals("SIMULATION")) myVisitor.stereo_invariant="SIMULATION";
		
		if(stereo.equals("ASSERTION")) myVisitor.stereo_invariant="ASSERTION";
		
		/*
		 * Tests:
		 * org.eclipse.ocl.util.ToStringVisitor visitor = org.eclipse.ocl.util.ToStringVisitor.getInstance(oclparser.getUMLEnvironment());
		 * System.out.prinln("");
		 * System.out.println(visitor.visitConstraint(ct));
		 */	
		
		try{			
			
			result += myVisitor.visitConstraint(ct);
						
			succeeds = true;			
			
		}catch(IteratorException e){
			log += "Constraint: "+ct.getName()+"\n"+e.getMessage()+"\n"+"This Constraint was not transformed into Alloy.\n";
			succeeds=false; 
			
		}catch(LiteralException e){
			log += "Constraint: "+ct.getName()+"\n"+e.getMessage()+"\n"+"This Constraint was not transformed into Alloy.\n";;
			succeeds=false; 
			
		}catch(OperationException e){
			log += "Constraint: "+ct.getName()+"\n"+e.getMessage()+"\n"+"This Constraint was not transformed into Alloy.\n";;
			succeeds=false; 
			
		}catch(StereotypeException e){
			log += "Constraint: "+ct.getName()+"\n"+e.getMessage()+"\n"+"This Constraint was not transformed into Alloy.\n";;
			succeeds=false; 
			
		}catch(TypeException e){
			log += "Constraint: "+ct.getName()+"\n"+e.getMessage()+"\n"+"This Constraint was not transformed into Alloy.\n";;
			succeeds=false; 
		}					
				
		return result;
	}
}
