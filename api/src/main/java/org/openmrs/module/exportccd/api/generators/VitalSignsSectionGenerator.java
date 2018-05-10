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
	
	private static final int VITAL_SIGNS_CONCEPT_ID = 1114;
	
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
		section.setTitle(utils.buildST("Vital Signs"));
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\" width=\"100%\">");
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Date</th>");
		List<Concept> vitalSignsList = new ArrayList();
		vitalSignsList.add(Context.getConceptService().getConcept(VITAL_SIGNS_CONCEPT_ID));
		List<Obs> listOfObservations = new ArrayList();
		Map<String, String> vitalSignData = new HashMap();
		Set<Concept> observedConceptList = new HashSet();
		Set<Date> dateSet = new HashSet();
		
		Iterator i$ = vitalSignsList.iterator();
		while (i$.hasNext()) {
			Concept concept = (Concept) i$.next();
			if (concept.isSet()) {
				List<Concept> conceptSet = concept.getSetMembers();
				System.out.println(conceptSet);
				i$ = conceptSet.iterator();
				
				while (i$.hasNext()) {
					Concept conceptSet2 = (Concept) i$.next();
					listOfObservations.addAll(Context.getObsService()
					        .getObservationsByPersonAndConcept(patient, conceptSet2));
					observedConceptList.add(conceptSet2);
				}
			} else {
				listOfObservations.addAll(Context.getObsService().getObservationsByPersonAndConcept(patient, concept));
				observedConceptList.add(concept);
			}
		}
		
		i$ = listOfObservations.iterator();
		
		Date date;
		while (i$.hasNext()) {
			Obs obs = (Obs) i$.next();
			date = obs.getObsDatetime();
			dateSet.add(date);
			if (vitalSignData.containsKey(date)) {
				String data = vitalSignData.get(date);
				data = data + "," + obs.getId();
				vitalSignData.put(date + obs.getConcept().getId().toString(), data);
			} else {
				vitalSignData.put(date + obs.getConcept().getId().toString(), obs.getId().toString());
			}
		}
		
		System.out.println(vitalSignData);
		SortedSet<Date> sortedSet = new TreeSet(Collections.reverseOrder());
		sortedSet.addAll(dateSet);
		i$ = sortedSet.iterator();
		
		while (i$.hasNext()) {
			date = (Date) i$.next();
			buffer.append("<th>" + utils.format(date) + "</th>");
		}
		
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		i$ = observedConceptList.iterator();
		
		while (i$.hasNext()) {
			Concept concept = (Concept) i$.next();
			buffer.append("<tr>");
			buffer.append("<td><content Id= \"" + concept.getDisplayString() + "\">" + concept.getDisplayString()
			        + "</content></td>");
			i$ = sortedSet.iterator();
			
			while (i$.hasNext()) {
				date = (Date) i$.next();
				if (vitalSignData.containsKey(date + "" + concept)) {
					ConceptNumeric c = Context.getConceptService().getConceptNumeric(concept.getId());
					String obsId = vitalSignData.get(date + "" + concept);
					Obs obs = Context.getObsService().getObs(Integer.parseInt(obsId));
					buffer.append("<td>" + obs.getValueNumeric() + c.getUnits() + "</td>");
					Entry vitalSignEntry = CDAFactory.eINSTANCE.createEntry();
					vitalSignEntry.setTypeCode(x_ActRelationshipEntry.DRIV);
					Organizer organizer = CDAFactory.eINSTANCE.createOrganizer();
					organizer.setClassCode(x_ActClassDocumentEntryOrganizer.CLUSTER);
					organizer.setMoodCode(ActMood.EVN);
					organizer.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.13.1", "", "IHE PCC"));
					organizer.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.32", "", "CCD"));
					organizer.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.35", "", "CCD"));
					organizer.getIds().add(utils.buildID(obs.getUuid(), ""));
					organizer.setCode(utils.buildCode("46680005", "2.16.840.1.113883.6.96", "Vital signs", "SNOMED-CT"));
					CS statusCode = DatatypesFactory.eINSTANCE.createCS();
					statusCode.setCode("completed");
					organizer.setStatusCode(statusCode);
					organizer.setEffectiveTime(utils.buildEffectiveTimeinIVL(date, null));
					Component4 component = CDAFactory.eINSTANCE.createComponent4();
					Observation observation = CDAFactory.eINSTANCE.createObservation();
					observation.setClassCode(ActClassObservation.OBS);
					observation.setMoodCode(x_ActMoodDocumentObservation.EVN);
					observation.getTemplateIds().add(
					    utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.14", "", "HITSP C83"));
					observation.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.31", "", "CCD"));
					observation.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.13", "", "IHE PCC"));
					observation.getIds().add(utils.buildID(obs.getUuid(), ""));
					observation.setCode(utils.buildConceptCode(concept, "SNOMED", "LOINC"));
					observation.setText(utils.buildEDText("#" + concept.getDisplayString()));
					CS statusCode1 = DatatypesFactory.eINSTANCE.createCS();
					statusCode1.setCode("completed");
					observation.setStatusCode(statusCode1);
					observation.setEffectiveTime(utils.buildEffectiveTimeinIVL(obs.getObsDatetime(), null));
					PQ unit = DatatypesFactory.eINSTANCE.createPQ();
					unit.setUnit(c.getUnits());
					unit.setValue(obs.getValueNumeric());
					observation.getValues().add(unit);
					ReferenceRange conceptRange = CDAFactory.eINSTANCE.createReferenceRange();
					ObservationRange observationRange = CDAFactory.eINSTANCE.createObservationRange();
					observationRange.setNullFlavor(NullFlavor.UNK);
					conceptRange.setObservationRange(observationRange);
					observation.getReferenceRanges().add(conceptRange);
					component.setObservation(observation);
					organizer.getComponents().add(component);
					vitalSignEntry.setOrganizer(organizer);
					section.getEntries().add(vitalSignEntry);
				} else {
					buffer.append("<td></td>");
				}
			}
			
			buffer.append("</tr>");
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		details.addText(buffer.toString());
		section.setText(details);
		return ccd;
	}
}
