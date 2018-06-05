package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.VitalSignsSection;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.db.PatientSummaryExportDAO;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class VitalSignsSectionGenerator {
	
	private static final int WEIGHT_CONCEPT_ID = 5089;
	
	private static final int CD4_VALUE_CONCEPT_ID = 159375;
	
	private static final int EXAM_CONCEPT_START_ID = 1120;
	
	private static final int EXAM_CONCEPT_END_ID = 1129;
	
	private static final int MOUTH_EXAM_CONCEPT_ID = 163308;
	
	private static final int EYE_EXAM_CONCEPT_ID = 163309;
	
	private static final int NOSE_EXAM_CONCEPT_ID = 163336;
	
	private static final int EAR_EXAM_CONCEPT_ID = 163337;
	
	public static final int ABNORMAL_CONCEPT_ID = 1116;
	
	public static final int AUTRES_CONCLUSION_CONCEPT_ID = 1284;
	
	@Autowired
	private ExportCcdUtils utils;
	
	@Autowired
	private PatientSummaryExportDAO dao;
	
	public ContinuityOfCareDocument buildVitalSigns(ContinuityOfCareDocument ccd, Patient patient) {
		VitalSignsSection section = CCDFactory.eINSTANCE.createVitalSignsSection();
		ccd.addSection(section);
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.119", "", "HITSP/C83"));
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.1.5.3.2", "", "IHE PCC"));
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.25", "", "IHE PCC"));
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.16", "", "HL7 CCD"));
		section.setCode(utils.buildCodeCE("8716-3", "2.16.840.1.113883.6.1", "Vital signs", "LOINC"));
		section.setTitle(utils.buildST("Examen Clinique"));
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(buildWeightSection(patient));
		
		builder.append(buildCd4Section(patient));
		
		builder.append(buildConclusionSection(patient));
		
		builder.append(buildOtherConclusionSection(patient));
		
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		details.addText(builder.toString());
		section.setText(details);
		return ccd;
	}
	
	private String buildOtherConclusionSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		SortedMap<String, List<String>> otherConclusions = new TreeMap<String, List<String>>(utils.descendingDateComparator);
		Concept concept = Context.getConceptService().getConcept(AUTRES_CONCLUSION_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, concept);
		
		for (Obs obs : listOfObservations) {
			String conclusionValue = obs.getValueCoded().getDisplayString();
			if (obs.getComment() != null) {
				conclusionValue += String.format(" (%s)", obs.getComment());
			}
			
			if (otherConclusions.get(utils.format(obs.getDateCreated())) == null) {
				List<String> concepts = new ArrayList<String>();
				
				concepts.add(conclusionValue);
				otherConclusions.put(utils.format(obs.getDateCreated()), concepts);
			} else {
				otherConclusions.get(utils.format(obs.getDateCreated())).add(conclusionValue);
			}
		}
		
		if (!otherConclusions.isEmpty()) {
			builder.append(utils.buildSubTitle("Autres conlusions:"));
			builder.append(utils.buildSectionHeader());
			for (Map.Entry<String, List<String>> conclusion : otherConclusions.entrySet()) {
				builder.append(utils.buildSectionContent(conclusion.getKey(),
				    Arrays.toString(new HashSet(conclusion.getValue()).toArray()).replace("[", "").replace("]", "")));
			}
			builder.append(utils.buildSectionFooter());
		}
		return builder.toString();
	}
	
	private String buildCd4Section(Patient patient) {
		StringBuilder builder = new StringBuilder();
		ConceptNumeric cd4 = Context.getConceptService().getConceptNumeric(CD4_VALUE_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, cd4);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSubTitle("Historique de poids"));
			builder.append(utils.buildSectionHeader("Résultat", "Unité", "Date"));
			for (Obs obs : listOfObservations) {
				String value = obs.getValueNumeric().toString();
				builder.append(utils.buildSectionContent(value, "/mm3", utils.format(obs.getDateCreated())));
			}
		}
		builder.append(utils.buildSectionFooter());
		return builder.toString();
	}
	
	private String buildConclusionSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		SortedMap<String, List<String>> conclusions = new TreeMap<String, List<String>>(utils.descendingDateComparator);
		for (int i = EXAM_CONCEPT_START_ID; i < EXAM_CONCEPT_END_ID; i++) {
			extractConclusions(patient, conclusions, i);
		}
		
		extractConclusions(patient, conclusions, MOUTH_EXAM_CONCEPT_ID);
		extractConclusions(patient, conclusions, EYE_EXAM_CONCEPT_ID);
		extractConclusions(patient, conclusions, NOSE_EXAM_CONCEPT_ID);
		extractConclusions(patient, conclusions, EAR_EXAM_CONCEPT_ID);
		
		if (!conclusions.isEmpty()) {
			builder.append(utils.buildSubTitle("Conclusions d'examen clinique"));
			builder.append(utils.buildSectionHeader("Date de visite", "Résultats anormals"));
			for (Map.Entry<String, List<String>> conclusion : conclusions.entrySet()) {
				builder.append(utils.buildSectionContent(conclusion.getKey(),
				    Arrays.toString(conclusion.getValue().toArray()).replace("[", "").replace("]", "")));
			}
			builder.append(utils.buildSectionFooter());
		}
		return builder.toString();
	}
	
	private String buildWeightSection(Patient patient) {
		StringBuilder builder = new StringBuilder();
		ConceptNumeric weight = Context.getConceptService().getConceptNumeric(WEIGHT_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, weight);
		if (!listOfObservations.isEmpty()) {
			builder.append(utils.buildSubTitle("Historique de CD4"));
			builder.append(utils.buildSectionHeader("Résultat", "Unité", "Date"));
			for (Obs obs : listOfObservations) {
				String value = obs.getValueNumeric().toString();
				builder.append(utils.buildSectionContent(value, weight.getUnits(), utils.format(obs.getDateCreated())));
			}
		}
		builder.append(utils.buildSectionFooter());
		return builder.toString();
	}
	
	private void extractConclusions(Patient patient, SortedMap<String, List<String>> conclusions, int i) {
		List<Obs> listOfObservations;
		Concept examConcept = Context.getConceptService().getConcept(i);
		listOfObservations = utils.extractObservations(patient, examConcept);
		for (Obs obs : listOfObservations) {
			if (obs.getValueCoded().getConceptId() == ABNORMAL_CONCEPT_ID) {
				if (conclusions.get(utils.format(obs.getDateCreated())) == null) {
					List<String> concepts = new ArrayList<String>();
					concepts.add(examConcept.getDisplayString());
					conclusions.put(utils.format(obs.getDateCreated()), concepts);
				} else {
					conclusions.get(utils.format(obs.getDateCreated())).add(examConcept.getDisplayString());
				}
			}
		}
	}
	
}
