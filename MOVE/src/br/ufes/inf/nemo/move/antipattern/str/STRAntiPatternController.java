package br.ufes.inf.nemo.move.antipattern.str;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import br.ufes.inf.nemo.common.file.FileUtil;
import br.ufes.inf.nemo.move.output.OutputModel;
import br.ufes.inf.nemo.move.util.AlloyJARExtractor;
import br.ufes.inf.nemo.ontouml2alloy.transformer.OntoUML2Alloy;
import br.ufes.inf.nemo.ontouml2alloy.util.Options;
import edu.mit.csail.sdg.alloy4whole.SimpleGUICustom;

/**
 * @author John Guerson
 */

public class STRAntiPatternController {

	private STRAntiPatternView strView;
	private STRAntiPatternModel strModel;
	
	/**
	 * Constructor.
	 * 
	 * @param strView
	 * @param strModel
	 */
	public STRAntiPatternController(STRAntiPatternView strView, STRAntiPatternModel strModel)
	{
		this.strView = strView;
		this.strModel = strModel;		
		
		strView.addExecuteWithAnalzyerListener(new ExecuteWithAnalzyerListener());
	}
	
	/**
	 * Execute With Analyzer Action Listener
	 * 
	 * @author John
	 */
	class ExecuteWithAnalzyerListener implements ActionListener 
	{
		public void actionPerformed(ActionEvent e) 
	    {			
	    	try{
	    		
	    		String predicates = new String();
				Integer cardinality = strView.getScope();
				
	    		if(strView.isSelectedAntiSymmetric()) 
	    		{
	    			predicates += "\n\n"+strModel.getSTRAntiPattern().generateAntisymmetricPredicate(
	    				cardinality,strView.getTheFrame().getOntoUMLModel().getOntoUMLParser()
	    			);
	    		}
			
	    		if(strView.isSelectedIntransitive()) 
	    		{
	    			predicates += "\n\n"+strModel.getSTRAntiPattern().generateIntransitivePredicate(
	    				cardinality,strView.getTheFrame().getOntoUMLModel().getOntoUMLParser()
	    			);
	    		}	    		
	    		
	    		if(strView.isSelectedIrreflexive()) 
	    		{
	    			predicates += "\n\n"+strModel.getSTRAntiPattern().generateIrreflexivePredicate(
	    				cardinality,strView.getTheFrame().getOntoUMLModel().getOntoUMLParser()
	    			);
	    		}
			
	    		if(strView.isSelectedReflexive()) 
	    		{
	    			predicates += "\n\n"+strModel.getSTRAntiPattern().generateReflexivePredicate(
	    				cardinality,strView.getTheFrame().getOntoUMLModel().getOntoUMLParser()
	    			);
	    		}
								
	    		if(strView.isSelectedSymmetric()) 
	    		{
	    			predicates += "\n\n"+strModel.getSTRAntiPattern().generateSymmetricPredicate(
	    				cardinality,strView.getTheFrame().getOntoUMLModel().getOntoUMLParser()
	    			);
	    		}
	    		
	    		if(strView.isSelectedTransitive()) 
	    		{
	    			predicates += "\n\n"+strModel.getSTRAntiPattern().generateTransitivePredicate(
	    				cardinality,strView.getTheFrame().getOntoUMLModel().getOntoUMLParser()
	    			);
	    		}
	    		
	    		String alsPath = OutputModel.alsOutDirectory+
	    				strView.getTheFrame().getOutputModel().alsmodelName+"$STR"+strModel.getId()+".als";		
						
	    		Options opt = strView.getTheFrame().getOptionModel().getOptions();
			
	    		RefOntoUML.Package refmodel = strView.getTheFrame().getOntoUMLModel().getOntoUMLModelInstance();		
			
	    		OntoUML2Alloy.Transformation(refmodel, alsPath, opt);
						
	    		FileUtil.writeToFile(predicates, alsPath);
			
	    		if (opt.openAnalyzer)
	    		{
	    			AlloyJARExtractor.extractAlloyJaRTo("alloy4.2.jar", OutputModel.alsOutDirectory);
				
	    			String[] argsAnalyzer = { "","" };
	    			argsAnalyzer[0] = alsPath;
	    			argsAnalyzer[1] = OutputModel.alsOutDirectory + "standart_theme.thm"	;	
	    			SimpleGUICustom.main(argsAnalyzer);
	    		}
	    		
	    	}catch(Exception exception){
	    		JOptionPane.showMessageDialog(strView.getTheFrame(),exception.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
	    	}
	    	
	    }
	}
}