package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;

import org.openhealthtools.mdht.uml.cda.StrucDocText;

import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.FamilyHistorySection;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FamilyHistorySectionGenerator {
	
	public static final String BIRTHPLACE = "Birthplace";
	
	public static final String TELEPHONE_NUMBER = "Telephone Number";
	
	public static final String CIVIL_STATUS = "Civil Status";
	
	public static final String FIRST_NAME_OF_MOTHER = "First Name of Mother";
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildFamilyHistory(ContinuityOfCareDocument ccd, Patient patient) {
		FamilyHistorySection section = CCDFactory.eINSTANCE.createFamilyHistorySection();
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.125", "", "HITSP/C83"));
		section.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.4", "", "CCD"));
		section.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.14", "", "IHE PCC"));
		section.setCode(utils.buildCodeCE("10157-6", "2.16.840.1.113883.6.1", "History of family member diseases", "LOINC"));
		section.setTitle(utils.buildST("Informations Démographiques"));
		StrucDocText details = CDAFactory.eINSTANCE.createStrucDocText();
		
		StringBuilder builder = utils.buildSectionHeader();
		
		Person person = patient.getPerson();
		
		StringBuilder nameRow = new StringBuilder();
		nameRow.append(person.getGivenName());
		if (person.getMiddleName() != null) {
			nameRow.append(" ");
			nameRow.append(patient.getPerson().getMiddleName());
		}
		nameRow.append(", ");
		nameRow.append(person.getFamilyName());
		builder.append(utils.buildSectionContent("Nom:", nameRow.toString()));
		
		builder.append(utils.buildSectionContent("Addresse, Commune:", person.getPersonAddress().getAddress1() == null ? "-"
		        : person.getPersonAddress().getAddress1()));
		builder.append(utils.buildSectionContent("Section, communale:",
		    person.getPersonAddress().getCountyDistrict() == null ? "" : person.getPersonAddress().getCountyDistrict()));
		builder.append(utils.buildSectionContent("Localité", person.getPersonAddress().getCityVillage() == null ? "-"
		        : person.getPersonAddress().getCityVillage()));
		builder.append(utils.buildSectionContent("Lieu de naissance:", person.getAttribute(BIRTHPLACE) == null ? "-"
		        : person.getAttribute(BIRTHPLACE).getValue()));
		builder.append(utils.buildSectionContent("Téléphone:", person.getAttribute(TELEPHONE_NUMBER) == null ? "-" : person
		        .getAttribute(TELEPHONE_NUMBER).getValue()));
		builder.append(utils.buildSectionContent("Sexe:", person.getGender() == null ? "-" : person.getGender()));
		builder.append(utils.buildSectionContent("Statut martial:", person.getAttribute(CIVIL_STATUS) == null ? "-" : person
		        .getAttribute(CIVIL_STATUS).getValue()));
		String birthdate = person.getBirthdateEstimated() == true ? "~" : "";
		birthdate += person.getBirthdate().toString() == null ? "-" : person.getBirthdate().toString();
		builder.append(utils.buildSectionContent("Date de naissance:", birthdate));
		builder.append(utils.buildSectionContent("Prénom de la mère:",
		    person.getAttribute(FIRST_NAME_OF_MOTHER) == null ? "-" : person.getAttribute(FIRST_NAME_OF_MOTHER).getValue()));
		
		builder.append(utils.buildSectionFooter());
		
		details.addText(builder.toString());
		section.setText(details);
		ccd.addSection(section);
		return ccd;
	}
}
