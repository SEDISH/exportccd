package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemsSectionGenerator {
	
	private static final int PROBLEM_START_CONCEPT_ID = 6042;
	
	private static final int PROBLEM_END_CONCEPT_ID = 6097;
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildProblems(ContinuityOfCareDocument ccd, Patient patient) {
		ProblemSection problemSection = CCDFactory.eINSTANCE.createProblemSection();
		ccd.addSection(problemSection);
		problemSection.setTitle(utils.buildST("Liste des Problèmes"));
		
		StringBuilder builder = utils.buildSectionHeader("Diagnostic", "Date de début", "Date de dernier diagnostic",
		    "Actif", "Guéri");
		
		List<Obs> startedProblems = Context.getObsService().getObservationsByPersonAndConcept(patient,
		    Context.getConceptService().getConcept(PROBLEM_START_CONCEPT_ID));
		
		List<Obs> endedProblems = Context.getObsService().getObservationsByPersonAndConcept(patient,
		    Context.getConceptService().getConcept(PROBLEM_END_CONCEPT_ID));
		
		for (Obs obs : startedProblems) {
			String startDate = utils.format(obs.getObsDatetime());
			String problem = obs.getValueCoded().getDisplayString();
			String endDate = "-";
			String activeStr = "[X]";
			String inactiveStr = "[ ]";
			
			for (Obs endedProblem : endedProblems) {
				if (endedProblem.getValueCoded().getDisplayString().equals(problem)) {
					endDate = utils.format(endedProblem.getObsDatetime());
					inactiveStr = "[X]";
					activeStr = "[ ]";
					break;
				}
			}
			
			builder.append(utils.buildSectionContent(problem, startDate, endDate, activeStr, inactiveStr));
		}
		
		builder.append(utils.buildSectionFooter());
		
		StrucDocText problemDetails = CDAFactory.eINSTANCE.createStrucDocText();
		problemDetails.addText(builder.toString());
		problemSection.setText(problemDetails);
		return ccd;
	}
}
