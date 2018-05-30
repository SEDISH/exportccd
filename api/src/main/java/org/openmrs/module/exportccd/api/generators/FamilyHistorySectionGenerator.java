package org.openmrs.module.exportccd.api.generators;

import org.apache.commons.lang3.StringUtils;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.RelatedSubject;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.Subject;
import org.openhealthtools.mdht.uml.cda.SubjectPerson;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.FamilyHistorySection;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryOrganizer;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubject;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class FamilyHistorySectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildFamilyHistory(ContinuityOfCareDocument ccd, Patient patient) {
		FamilyHistorySection section = CCDFactory.eINSTANCE.createFamilyHistorySection();
		ccd.addSection(section);
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.125", "", "HITSP/C83"));
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.4", "", "CCD"));
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.14", "", "IHE PCC"));
		section.setCode(utils.buildCodeCE("10157-6", "2.16.840.1.113883.6.1", "History of family member diseases", "LOINC"));
		section.setTitle(utils.buildST("Family History"));
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		StringBuffer buffer = new StringBuffer();
		ConceptService cs = Context.getConceptService();
		List<Obs> l = Context.getObsService().getObservationsByPersonAndConcept(patient, cs.getConcept(160593));
		Iterator i$ = l.iterator();
		
		while (i$.hasNext()) {
			Obs obs = (Obs) i$.next();
			List<Obs> familyHistory = Context.getObsService().findObsByGroupId(obs.getId());
			System.out.println(familyHistory);
			String relation = "";
			String diagnosis = "";
			String age = "";
			
			for (Obs obs2 : familyHistory) {
				switch (obs2.getConcept().getId()) {
					case 1560:
						relation = obs2.getValueCoded().getDisplayString();
						break;
					case 160592:
						diagnosis = obs2.getValueCoded().getDisplayString();
						break;
					case 160617:
						age = obs2.getValueNumeric().toString();
				}
			}
			
			buffer.append("<paragraph>" + relation + "</paragraph>");
			buffer.append(utils.getBorderStart());
			buffer.append("<thead>");
			buffer.append("<tr>");
			buffer.append("<th style=\"text-align: left;\">Age</th>");
			buffer.append("<th style=\"text-align: left;\">Diagnosis</th>");
			buffer.append("</tr>");
			buffer.append("</thead>");
			buffer.append("<tbody>");
			buffer.append("<tr>");
			buffer.append("<td>" + age + "</td>");
			buffer.append("<td> <content id=\"" + diagnosis + "\">" + diagnosis + "</content></td>");
			buffer.append("</tr>");
			buffer.append("</tbody>");
			buffer.append("</table>");
			Entry entry = CDAFactory.eINSTANCE.createEntry();
			entry.setTypeCode(x_ActRelationshipEntry.DRIV);
			Organizer organizer = CDAFactory.eINSTANCE.createOrganizer();
			organizer.setMoodCode(ActMood.EVN);
			organizer.setClassCode(x_ActClassDocumentEntryOrganizer.CLUSTER);
			organizer.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.18", "", "HITSP C83"));
			organizer.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.23", "", "CCD"));
			organizer.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.15", "", "IHE PCC"));
			CS statusCode = DatatypesFactory.eINSTANCE.createCS();
			statusCode.setCode("completed");
			organizer.setStatusCode(statusCode);
			Subject subject = CDAFactory.eINSTANCE.createSubject();
			RelatedSubject relatedSubject = CDAFactory.eINSTANCE.createRelatedSubject();
			relatedSubject.setClassCode(x_DocumentSubject.PRS);
			relatedSubject.setCode(utils.buildConceptCode(cs.getConcept(relation), "Snomed"));
			AD address = DatatypesFactory.eINSTANCE.createAD();
			relatedSubject.getAddrs().add(address);
			TEL tel = DatatypesFactory.eINSTANCE.createTEL();
			tel.setNullFlavor(NullFlavor.UNK);
			SubjectPerson subjectPerson = CDAFactory.eINSTANCE.createSubjectPerson();
			PN name = DatatypesFactory.eINSTANCE.createPN();
			subjectPerson.getNames().add(name);
			CE gender = DatatypesFactory.eINSTANCE.createCE();
			gender.setNullFlavor(NullFlavor.UNK);
			subjectPerson.setAdministrativeGenderCode(gender);
			TS birthTime = DatatypesFactory.eINSTANCE.createTS();
			birthTime.setNullFlavor(NullFlavor.UNK);
			subjectPerson.setBirthTime(birthTime);
			relatedSubject.setSubject(subjectPerson);
			relatedSubject.getTelecoms().add(tel);
			subject.setRelatedSubject(relatedSubject);
			organizer.setSubject(subject);
			Component4 obsComp = CDAFactory.eINSTANCE.createComponent4();
			Observation co = CDAFactory.eINSTANCE.createObservation();
			co.setClassCode(ActClassObservation.OBS);
			co.setMoodCode(x_ActMoodDocumentObservation.EVN);
			co.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.22", "", "CCD"));
			co.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.13", "", "IHE PCC"));
			co.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.13.3", "", "IHE PCC"));
			co.getIds().add(utils.buildID(obs.getUuid(), ""));
			co.setCode(utils.buildConceptCode(cs.getConcept(diagnosis), "SNOMED"));
			co.setText(utils.buildEDText("#" + diagnosis));
			CS statusCode1 = DatatypesFactory.eINSTANCE.createCS();
			statusCode1.setCode("completed");
			co.setStatusCode(statusCode1);
			IVL_TS et = DatatypesFactory.eINSTANCE.createIVL_TS();
			et.setNullFlavor(NullFlavor.UNK);
			co.setEffectiveTime(et);
			obsComp.setObservation(co);
			EntryRelationship e = CDAFactory.eINSTANCE.createEntryRelationship();
			e.setTypeCode(x_ActRelationshipEntryRelationship.SUBJ);
			e.setInversionInd(true);
			Observation ob = CDAFactory.eINSTANCE.createObservation();
			ob.setClassCode(ActClassObservation.OBS);
			ob.setMoodCode(x_ActMoodDocumentObservation.EVN);
			ob.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.38", "", ""));
			ob.setCode(utils.buildCode("397659008", "2.16.840.1.113883.6.96", "Age", "SNOMED-CT"));
			CS statusCode2 = DatatypesFactory.eINSTANCE.createCS();
			statusCode2.setCode("completed");
			ob.setStatusCode(statusCode2);
			INT age1 = DatatypesFactory.eINSTANCE.createINT();
			if (StringUtils.isNotEmpty(age)) {
				age1.setValue((int) Float.parseFloat(age));
			}
			ob.getValues().add(age1);
			e.setObservation(ob);
			co.getEntryRelationships().add(e);
			organizer.getComponents().add(obsComp);
			entry.setOrganizer(organizer);
			section.getEntries().add(entry);
		}
		
		details.addText(buffer.toString());
		section.setText(details);
		return ccd;
	}
}
