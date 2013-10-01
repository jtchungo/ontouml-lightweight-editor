package br.ufes.inf.nemo.ocl2swrl.factory.ocl.uml.impl;

import java.util.Set;

import org.eclipse.ocl.uml.impl.IteratorExpImpl;
import org.eclipse.ocl.uml.impl.OCLExpressionImpl;
import org.eclipse.uml2.uml.internal.impl.NamedElementImpl;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import br.ufes.inf.nemo.ocl2swrl.factory.Factory;



/**
 * @author fredd_000
 * @version 1.0
 * @created 24-set-2013 09:16:12
 */
public class IteratorExpImplFactory extends LoopExpImplFactory {

	public IteratorExpImplFactory(NamedElementImpl m_NamedElementImpl){
		super(m_NamedElementImpl);
	}
	
	public void finalize() throws Throwable {
		super.finalize();
	}

	@Override
	public SWRLDArgument solve(String nameSpace, OWLOntologyManager manager, OWLDataFactory factory, OWLOntology ontology, Set<SWRLAtom> antecedent, Set<SWRLAtom> consequent, SWRLDArgument referredArgument, Boolean oclConsequentShouldBeNegated, Boolean expressionIsNegated, int repeatNumber) {
		IteratorExpImpl iteratorExpImpl = (IteratorExpImpl) this.m_NamedElementImpl; 
		OCLExpressionImpl source = (OCLExpressionImpl) iteratorExpImpl.getSource();
		OCLExpressionImpl body = (OCLExpressionImpl) iteratorExpImpl.getBody();
		
		this.sourceFactory = (OCLExpressionImplFactory) Factory.constructor(source);
		SWRLDArgument varX = this.sourceFactory.solve(nameSpace, manager, factory, ontology, antecedent, consequent, null, false, expressionIsNegated, repeatNumber);
		
		this.bodyFactory = (OCLExpressionImplFactory) Factory.constructor(body);
		SWRLDArgument varY = this.bodyFactory.solve(nameSpace, manager, factory, ontology, antecedent, consequent, varX, oclConsequentShouldBeNegated, expressionIsNegated, repeatNumber); 
		
		SWRLDArgument varZ = null;
		if(this.isUnique()){
			varZ = solveIsUnique(nameSpace, manager, factory, ontology, antecedent, consequent, varX, varY, false, false);
		}
		
		return varY;
	}
	
	public SWRLDArgument solveIsUnique(String nameSpace, OWLOntologyManager manager, OWLDataFactory factory, OWLOntology ontology, Set<SWRLAtom> antecedent, Set<SWRLAtom> consequent, SWRLDArgument referredArgX, SWRLDArgument referredArgY, Boolean oclConsequentShouldBeNegated, Boolean expressionIsNegated) {
		IteratorExpImpl iteratorExpImpl = (IteratorExpImpl) this.m_NamedElementImpl; 
		OCLExpressionImpl source = (OCLExpressionImpl) iteratorExpImpl.getSource();
		OCLExpressionImpl body = (OCLExpressionImpl) iteratorExpImpl.getBody();
		
		int repeatNumber = 2;
		
		this.sourceFactory = (OCLExpressionImplFactory) Factory.constructor(source);
		SWRLDArgument varX = this.sourceFactory.solve(nameSpace, manager, factory, ontology, antecedent, consequent, null, false, expressionIsNegated, repeatNumber);
		
		this.bodyFactory = (OCLExpressionImplFactory) Factory.constructor(body);
		SWRLDArgument varY = this.bodyFactory.solve(nameSpace, manager, factory, ontology, antecedent, consequent, varX, oclConsequentShouldBeNegated, expressionIsNegated, repeatNumber); 
		
		SWRLDifferentIndividualsAtom diff = factory.getSWRLDifferentIndividualsAtom((SWRLVariable)referredArgX, (SWRLVariable)varX);
		antecedent.add(diff);
		
		SWRLSameIndividualAtom same = factory.getSWRLSameIndividualAtom((SWRLVariable)referredArgY, (SWRLVariable)varY);
		antecedent.add(same);
		
		return null;
	}
	
	@Override
	public Boolean isUnique(){
		IteratorExpImpl iteratorExpImpl = (IteratorExpImpl) this.m_NamedElementImpl; 
		String name = iteratorExpImpl.getName();
		
		if(name.equals("isUnique")){
			return true;
		}
		
		return false;
	}
}