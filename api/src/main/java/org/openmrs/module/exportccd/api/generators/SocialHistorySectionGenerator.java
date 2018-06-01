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
	
	private static final int OBSTETRIC_HISTORY_ID = 160076;

	private static final int ARV_ID = 5356;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildSocialHistory(ContinuityOfCareDocument ccd, Patient patient) {
		SocialHistorySection section = CCDFactory.eINSTANCE.createSocialHistorySection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.15", "", "HITSP/C83"));
		section.setCode(utils.buildCodeCE("29762-2", "2.16.840.1.113883.6.1", "Social History", "LOINC"));
		section.setTitle(utils.buildST("Historique"));
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		
		StringBuilder builder = new StringBuilder();
		builder.append(utils.buildSectionHeader());
		
		Concept pointOfHiv = Context.getConceptService().getConcept(POINT_OF_HIV_TESTING_CONCEPT_ID);
		List<Obs> listOfObservations = extractObservations(patient, pointOfHiv);
		if (!listOfObservations.isEmpty()) {
			String element = pointOfHiv.getDisplayString();
			builder.append(utils.buildSectionContent("<b>" + element + "</b>"));
			for (Obs obs : listOfObservations) {
				buildRow(builder, obs);
			}
		}

		buildSubsection(patient, builder, METHOD_OD_HIV_EXPOSURE_CONCEPT_ID, "Mode probable de transmission");
		buildSubsection(patient, builder, OBSTETRIC_HISTORY_ID, "Antécédents Obstétriques et Grossesse");
		buildSubsection(patient, builder, METHOD_OD_FAMILY_PLANNING_CONCEPT_ID, "Planning familial");
		buildSubsection(patient, builder, TUBERCULOSIS_DISEASE_STATUS_CONCEPT_ID, "Statut de TB");
		buildSubsection(patient, builder, ARV_ID, "Eligibilité Médical aux ARV");

		builder.append(utils.buildSectionFooter());
		
		details.addText(builder.toString());
		section.setText(details);
		ccd.addSection(section);
		return ccd;
	}

	private void buildSubsection(Patient patient, StringBuilder builder, int conceptId, String sectionHeader) {

		Concept concept = Context.getConceptService().getConcept(conceptId);
		List<Obs> listOfObservations = extractObservations(patient, concept);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSectionHeader(sectionHeader));
			for (Obs obs : listOfObservations) {
				buildRow(builder, obs);
			}
		}
	}

	private void buildRow(StringBuilder builder, Obs obs) {
		if (obs.getValueNumeric() != null) {
			String conceptName = obs.getConcept().getDisplayString();
			String value = obs.getValueNumeric().toString();
			builder.append(utils.buildSectionContent(conceptName, value));
		} else if (obs.getValueDatetime() != null) {
			String conceptName = obs.getConcept().getDisplayString();
			String value = obs.getValueDatetime().toString();
			builder.append(utils.buildSectionContent(conceptName, value));
		} else if (obs.getValueCoded() != null) {
			builder.append(utils.buildSectionContent(obs.getValueCoded().getDisplayString()));
		}
	}

	private List<Obs> extractObservations(Patient patient, Concept concept) {
		List<Obs> listOfObservations = new ArrayList<Obs>();
		if (concept.isSet()) {
			for (Concept conceptSet : concept.getSetMembers()) {
				listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, conceptSet));
			}
		} else {
			listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
		}
		return listOfObservations;
	}
}
