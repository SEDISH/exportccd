package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.SocialHistorySection;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.db.PatientSummaryExportDAO;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SocialHistorySectionGenerator {
	
	private static final int METHOD_OD_FAMILY_PLANNING_CONCEPT_ID = 374;
	
	private static final int METHOD_OD_HIV_EXPOSURE_CONCEPT_ID = 1061;
	
	private static final int POINT_OF_HIV_TESTING_CONCEPT_ID = 159936;
	
	private static final int TUBERCULOSIS_DISEASE_STATUS_CONCEPT_ID = 1659;
	
	private static final int DURATION_CONCEPT_ID = 163340;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildSocialHistory(ContinuityOfCareDocument ccd, Patient patient) {
		SocialHistorySection section = CCDFactory.eINSTANCE.createSocialHistorySection();
		ccd.addSection(section);
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.15", "", "HITSP/C83"));
		section.setCode(utils.buildCodeCE("29762-2", "2.16.840.1.113883.6.1", "Social History", "LOINC"));
		section.setTitle(utils.buildST("Historique"));
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		
		StringBuilder builder = utils.buildSectionHeader("Test Anticorps VIH", "Values", "Date");
		
		List<Concept> historyList = new ArrayList<Concept>();
		historyList.add(Context.getConceptService().getConcept(POINT_OF_HIV_TESTING_CONCEPT_ID));
		historyList.add(Context.getConceptService().getConcept(METHOD_OD_FAMILY_PLANNING_CONCEPT_ID));
		historyList.add(Context.getConceptService().getConcept(METHOD_OD_HIV_EXPOSURE_CONCEPT_ID));

		historyList.add(Context.getConceptService().getConcept(TUBERCULOSIS_DISEASE_STATUS_CONCEPT_ID));
		historyList.add(Context.getConceptService().getConcept(DURATION_CONCEPT_ID));
		
		List<Obs> listOfObservations = new ArrayList();
		for (Concept concept : historyList) {
			if (concept.isSet()) {
				for (Concept conceptSet : concept.getSetMembers()) {
					listOfObservations
					        .addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, conceptSet));
				}
			} else {
				listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
			}
		}
		
		for (Obs obs : listOfObservations) {
			String element = obs.getConcept().getDisplayString();
			String value = obs.getValueCoded().getDisplayString();
			
			builder.append(utils.buildSectionContent(element, utils.format(obs.getObsDatetime()), value));
		}
		
		builder.append(utils.buildSectionFooter());
		
		details.addText(builder.toString());
		section.setText(details);
		return ccd;
	}
}
