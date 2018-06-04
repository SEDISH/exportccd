package org.openmrs.module.exportccd.api.generators;

import org.apache.commons.lang3.StringUtils;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.ResultsSection;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.db.PatientSummaryExportDAO;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LabResultsSectionGenerator {
	
	private static final int LAB_RESULTS_CONCEPT_ID = 1271;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildLabResults(ContinuityOfCareDocument ccd, Patient patient) {
		ResultsSection section = CCDFactory.eINSTANCE.createResultsSection();
		ccd.addSection(section);
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.122", "", "HITSP/C83"));
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.28", "", "IHE PCC"));
		section.setCode(utils.buildCodeCE("30954-2", "2.16.840.1.113883.6.1", "Relevant diagnostic tests/laboratory data",
		    "LOINC"));
		section.setTitle(utils.buildST("Analyses de laboratoire"));
		
		StringBuilder builder = new StringBuilder();
		Concept concept = Context.getConceptService().getConcept(LAB_RESULTS_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, concept);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSectionHeader("", "Date", "Résultat"));
			for (Obs obs : listOfObservations) {
				builder.append(utils.buildSectionContent(obs.getValueCoded().getDisplayString(),
				    utils.format(obs.getDateCreated()), getValueOfObs(patient, obs)));
			}
			builder.append(utils.buildSectionFooter());
		}
		
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		details.addText(builder.toString());
		section.setText(details);
		return ccd;
	}
	
	private String getValueOfObs(Patient patient, Obs obs) {
		//TODO: verify if no data is skipped
		Obs tmp = Context
		        .getObsService()
		        .getObservationsByPersonAndConcept(patient,
		            Context.getConceptService().getConcept(obs.getValueCoded().getConceptId())).get(0);
		if (tmp.getValueNumeric() != null) {
			return tmp.getValueNumeric().toString();
		} else if (tmp.getValueCoded() != null) {
			return tmp.getValueCoded().getDisplayString();
		} else if (tmp.getValueBoolean() != null) {
			return tmp.getValueBoolean() ? "Oui" : "Non";
		} else {
			return "-";
		}
	}
}
