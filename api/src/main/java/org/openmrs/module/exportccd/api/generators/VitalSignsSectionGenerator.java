package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.ObservationRange;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.VitalSignsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryOrganizer;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.db.PatientSummaryExportDAO;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Component
public class VitalSignsSectionGenerator {
	
	private static final Integer WEIGHT_CONCEPT_ID = 5089;
	
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

		ConceptNumeric weight = Context.getConceptService().getConceptNumeric(WEIGHT_CONCEPT_ID);
		List<Obs> listOfObservations = utils.extractObservations(patient, weight);
		if (!listOfObservations.isEmpty()) {
			String element = weight.getDisplayString();
			builder.append(utils.buildSectionHeader("Résultat", "Unité", "Date"));
			for (Obs obs : listOfObservations) {
				String value = obs.getValueNumeric().toString();
				String date = obs.getDateCreated().toString();
				builder.append(utils.buildSectionContent(value, weight.getUnits(), date));
			}
		}
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		details.addText(builder.toString());
		section.setText(details);
		return ccd;
	}
}
