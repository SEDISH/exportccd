package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.AssignedCustodian;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.AssociatedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.AuthoringDevice;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Custodian;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.InfrastructureRootTypeId;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Participant1;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.Performer2;
import org.openhealthtools.mdht.uml.cda.PlayingEntity;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.EncountersSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.SC;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassAssociative;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentEncounterMood;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class HeaderGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildHeader(ContinuityOfCareDocument ccd, Patient patient) {
		ccd.getRealmCodes().clear();
		CS realmCode = DatatypesFactory.eINSTANCE.createCS("US");
		ccd.getRealmCodes().add(realmCode);
		Date d = new Date();
		ccd.setEffectiveTime(utils.buildEffectiveTime(d));
		SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss");
		String creationDate = s.format(d);
		InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
		typeId.setExtension("POCD_HD000040");
		typeId.setRoot("2.16.840.1.113883.1.3");
		ccd.setTypeId(typeId);
		ccd.getTemplateIds().clear();
		ccd.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.27.1776", "", "CDA/R2"));
		ccd.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.3", "", "HL7/CDT Header"));
		ccd.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.32.1", "", "HITSP/C32"));
		ccd.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.1.1", "", "IHE/PCC"));
		ccd.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.150.2474.11.2.1", "", ""));
		ccd.setCode(utils.buildCodeCE("422735006", "2.16.840.1.113883.6.96", "Summary clinical document", "SNOMED CT"));
		ccd.setTitle(utils.buildST("Medical Summary of " + patient.getGivenName() + " " + patient.getFamilyName()
		        + "create on " + d));
		CS languageCode = DatatypesFactory.eINSTANCE.createCS();
		languageCode.setCode("en-US");
		ccd.setLanguageCode(languageCode);
		CE confidentialityCode = DatatypesFactory.eINSTANCE.createCE();
		confidentialityCode.setCode("N");
		ccd.setConfidentialityCode(confidentialityCode);
		PatientRole patientRole = CDAFactory.eINSTANCE.createPatientRole();
		Set<PersonAddress> addresses = patient.getAddresses();
		
		for (PersonAddress address : addresses) {
			if (address.isPreferred()) {
				AD patientAddress = DatatypesFactory.eINSTANCE.createAD();
				patientAddress.addStreetAddressLine(address.getAddress1() + address.getAddress2());
				patientAddress.addCity(address.getCityVillage());
				patientAddress.addState(address.getStateProvince());
				patientRole.getAddrs().add(patientAddress);
			}
		}
		
		TEL patientTelecom = DatatypesFactory.eINSTANCE.createTEL();
		patientTelecom.setNullFlavor(NullFlavor.UNK);
		patientRole.getTelecoms().add(patientTelecom);
		org.openhealthtools.mdht.uml.cda.Patient cdapatient = CDAFactory.eINSTANCE.createPatient();
		patientRole.setPatient(cdapatient);
		PN name = DatatypesFactory.eINSTANCE.createPN();
		name.addGiven(patient.getPersonName().getGivenName());
		name.addFamily(patient.getPersonName().getFamilyName());
		cdapatient.getNames().add(name);
		CE gender = DatatypesFactory.eINSTANCE.createCE();
		gender.setCode(patient.getGender());
		gender.setCodeSystem("2.16.840.1.113883.5.1");
		cdapatient.setAdministrativeGenderCode(gender);
		System.out.print(patient.getAttribute("Civil Status"));
		PersonAttribute civilStatus = patient.getAttribute("Civil Status");
		if (civilStatus != null) {
			Concept c = Context.getConceptService().getConceptByName(civilStatus.toString());
			Collection<ConceptMap> conceptmapp = c.getConceptMappings();
			
			for (ConceptMap n : conceptmapp) {
				if (n.getSource().getName().equalsIgnoreCase("Snomed ct")) {
					CE codes = DatatypesFactory.eINSTANCE.createCE();
					codes.setCode(n.getSourceCode());
					codes.setCodeSystem("2.16.840.1.113883.6.96");
					codes.setCodeSystemName(n.getSource().getName());
					codes.setDisplayName(n.getConcept().getDisplayString());
					cdapatient.setMaritalStatusCode(codes);
				}
			}
		}
		
		TS dateOfBirth = DatatypesFactory.eINSTANCE.createTS();
		SimpleDateFormat s1 = new SimpleDateFormat("yyyyMMdd");
		Date dobs = patient.getBirthdate();
		String dob = s1.format(dobs);
		dateOfBirth.setValue(dob);
		cdapatient.setBirthTime(dateOfBirth);
		Organization providerOrganization = CDAFactory.eINSTANCE.createOrganization();
		AD providerOrganizationAddress = DatatypesFactory.eINSTANCE.createAD();
		providerOrganizationAddress.addCounty("");
		providerOrganizationAddress.addState("");
		providerOrganization.getAddrs().add(providerOrganizationAddress);
		ON organizationName = DatatypesFactory.eINSTANCE.createON();
		providerOrganization.getNames().add(organizationName);
		TEL providerOrganizationTelecon = DatatypesFactory.eINSTANCE.createTEL();
		providerOrganizationTelecon.setNullFlavor(NullFlavor.UNK);
		providerOrganization.getTelecoms().add(providerOrganizationTelecon);
		patientRole.setProviderOrganization(providerOrganization);
		ccd.addPatientRole(patientRole);
		Author author = CDAFactory.eINSTANCE.createAuthor();
		author.setTime(utils.buildEffectiveTime(d));
		AssignedAuthor assignedAuthor = CDAFactory.eINSTANCE.createAssignedAuthor();
		II authorId = DatatypesFactory.eINSTANCE.createII();
		assignedAuthor.getIds().add(authorId);
		Organization representedOrganization = CDAFactory.eINSTANCE.createOrganization();
		AD representedOrganizationAddress = DatatypesFactory.eINSTANCE.createAD();
		representedOrganizationAddress.addCounty("");
		representedOrganizationAddress.addState("");
		ON implName = DatatypesFactory.eINSTANCE.createON();
		representedOrganization.getNames().add(implName);
		assignedAuthor.getAddrs().add(representedOrganizationAddress);
		assignedAuthor.getTelecoms().add(providerOrganizationTelecon);
		org.openhealthtools.mdht.uml.cda.Person assignedPerson = CDAFactory.eINSTANCE.createPerson();
		PN assignedPersonName = DatatypesFactory.eINSTANCE.createPN();
		assignedPersonName.addText("Auto-generated");
		assignedPerson.getNames().add(assignedPersonName);
		AuthoringDevice authoringDevice = CDAFactory.eINSTANCE.createAuthoringDevice();
		SC authoringDeviceName = DatatypesFactory.eINSTANCE.createSC();
		authoringDeviceName.addText(Context.getAdministrationService().getGlobalProperty("application.name"));
		authoringDevice.setSoftwareName(authoringDeviceName);
		assignedAuthor.setAssignedAuthoringDevice(authoringDevice);
		assignedAuthor.setAssignedPerson(assignedPerson);
		assignedAuthor.setRepresentedOrganization(representedOrganization);
		author.setAssignedAuthor(assignedAuthor);
		ccd.getAuthors().add(author);
		ccd = this.buildEncounters(ccd, patient);
		List<Relationship> relationShips = Context.getPersonService().getRelationshipsByPerson(patient);
		List<Participant1> participantList = new ArrayList(relationShips.size());
		
		II custodianId;
		for (Relationship relationship : relationShips) {
			Participant1 e = CDAFactory.eINSTANCE.createParticipant1();
			e.setTypeCode(ParticipationType.IND);
			II pid1 = DatatypesFactory.eINSTANCE.createII();
			pid1.setAssigningAuthorityName("HITSP/C83");
			pid1.setRoot("2.16.840.1.113883.3.88.11.83.3");
			custodianId = DatatypesFactory.eINSTANCE.createII();
			custodianId.setAssigningAuthorityName("IHE/PCC");
			custodianId.setRoot("1.3.6.1.4.1.19376.1.5.3.1.2.4");
			e.getTemplateIds().add(pid1);
			e.getTemplateIds().add(custodianId);
			IVL_TS time = DatatypesFactory.eINSTANCE.createIVL_TS();
			time.setNullFlavor(NullFlavor.UNK);
			e.setTime(time);
			AssociatedEntity patientRelationShip = CDAFactory.eINSTANCE.createAssociatedEntity();
			patientRelationShip.setClassCode(RoleClassAssociative.PRS);
			CE relationShipCode = DatatypesFactory.eINSTANCE.createCE();
			relationShipCode.setCodeSystemName("Snomed CT");
			relationShipCode.setCodeSystem("2.16.840.1.113883.6.96");
			org.openhealthtools.mdht.uml.cda.Person associatedPerson = CDAFactory.eINSTANCE.createPerson();
			PN associatedPersonName = DatatypesFactory.eINSTANCE.createPN();
			Iterator<PersonAddress> patientAddressIterator = null;
			switch (relationship.getRelationshipType().getId()) {
				case 1:
					relationShipCode.setCode("305450004");
					relationShipCode.setDisplayName("Doctor");
					associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
					associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
					patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					break;
				case 2:
					relationShipCode.setCode("375005");
					relationShipCode.setDisplayName("Sibling");
					associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
					associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
					patientAddressIterator = relationship.getPersonA().getAddresses().iterator();
					break;
				case 3:
					if (patient.getId().equals(relationship.getPersonA().getId())) {
						relationShipCode.setCode("67822003");
						relationShipCode.setDisplayName("Child");
						associatedPersonName.addFamily(relationship.getPersonB().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonB().getGivenName());
						patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					} else {
						relationShipCode.setCode("40683002");
						relationShipCode.setDisplayName("Parent");
						associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
						patientAddressIterator = relationship.getPersonA().getAddresses().iterator();
					}
					break;
				case 4:
					if (patient.getId().equals(relationship.getPersonA().getId())) {
						if (relationship.getPersonB().getGender().equalsIgnoreCase("M")) {
							relationShipCode.setCode("83559000");
						} else {
							relationShipCode.setCode("34581001");
						}
						
						relationShipCode.setDisplayName("Neice/Nephew");
						associatedPersonName.addFamily(relationship.getPersonB().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonB().getGivenName());
						patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					} else {
						if (relationship.getPersonA().getGender().equalsIgnoreCase("M")) {
							relationShipCode.setCode("38048003");
						} else {
							relationShipCode.setCode("25211005");
						}
						
						relationShipCode.setDisplayName("Aunt/Uncle");
						associatedPersonName.addFamily(relationship.getPersonA().getFamilyName());
						associatedPersonName.addGiven(relationship.getPersonA().getGivenName());
						patientAddressIterator = relationship.getPersonB().getAddresses().iterator();
					}
			}
			
			patientRelationShip.setCode(relationShipCode);
			AD associatedPersonAddress = DatatypesFactory.eINSTANCE.createAD();
			if (patientAddressIterator.hasNext()) {
				PersonAddress padd = patientAddressIterator.next();
				associatedPersonAddress.addStreetAddressLine(padd.getAddress1() + padd.getAddress2());
			}
			
			patientRelationShip.getAddrs().add(associatedPersonAddress);
			associatedPerson.getNames().add(associatedPersonName);
			patientRelationShip.setAssociatedPerson(associatedPerson);
			e.setAssociatedEntity(patientRelationShip);
			participantList.add(e);
		}
		
		ccd.getParticipants().addAll(participantList);
		Custodian custodian = CDAFactory.eINSTANCE.createCustodian();
		AssignedCustodian assignedCustodian = CDAFactory.eINSTANCE.createAssignedCustodian();
		CustodianOrganization custodianOrganization = CDAFactory.eINSTANCE.createCustodianOrganization();
		custodianId = DatatypesFactory.eINSTANCE.createII();
		custodianOrganization.getIds().add(custodianId);
		custodianOrganization.setAddr(providerOrganizationAddress);
		custodianOrganization.setName(organizationName);
		custodianOrganization.setTelecom(providerOrganizationTelecon);
		assignedCustodian.setRepresentedCustodianOrganization(custodianOrganization);
		custodian.setAssignedCustodian(assignedCustodian);
		ccd.setCustodian(custodian);
		return ccd;
	}
	
	private ContinuityOfCareDocument buildEncounters(ContinuityOfCareDocument ccd, Patient patient) {
		EncountersSection encounterSection = CCDFactory.eINSTANCE.createEncountersSection();
		II encounterSectionTemplateID = DatatypesFactory.eINSTANCE.createII();
		encounterSectionTemplateID.setRoot("2.16.840.1.113883.3.88.11.83.127");
		encounterSectionTemplateID.setAssigningAuthorityName("HITSP/C83");
		encounterSection.getTemplateIds().add(encounterSectionTemplateID);
		II encounterSectionTemplateID1 = DatatypesFactory.eINSTANCE.createII();
		encounterSectionTemplateID1.setRoot("1.3.6.1.4.1.19376.1.5.3.1.1.5.3.3");
		encounterSectionTemplateID1.setAssigningAuthorityName("IHE PCC");
		encounterSection.getTemplateIds().add(encounterSectionTemplateID1);
		II encounterSectionTemplateID2 = DatatypesFactory.eINSTANCE.createII();
		encounterSectionTemplateID2.setRoot("2.16.840.1.113883.10.20.1.3");
		encounterSectionTemplateID2.setAssigningAuthorityName("HL7 CCD");
		encounterSection.getTemplateIds().add(encounterSectionTemplateID2);
		CE encounterSectionCode = DatatypesFactory.eINSTANCE.createCE();
		encounterSectionCode.setCode("46240-8");
		encounterSectionCode.setCodeSystem("2.16.840.1.113883.6.1");
		encounterSectionCode.setCodeSystemName("LOINC");
		encounterSectionCode.setDisplayName("History of encounters");
		encounterSection.setCode(encounterSectionCode);
		ST encounterSectionTitle = DatatypesFactory.eINSTANCE.createST();
		encounterSectionTitle.addText("Encounters");
		encounterSection.setTitle(encounterSectionTitle);
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\" width=\"100%\">");
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Encounter Type</th>");
		buffer.append("<th>Clinicial Name</th>");
		buffer.append("<th>Location</th>");
		buffer.append("<th>Date</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		List<Encounter> encounterList = Context.getEncounterService().getEncountersByPatient(patient);
		List<Entry> encounterEntryList = new ArrayList();
		int i = 0;
		
		for (Iterator i$ = encounterList.iterator(); i$.hasNext(); ++i) {
			Encounter encounter = (Encounter) i$.next();
			buffer.append("<tr>");
			buffer.append("<td><content id=\"encounterType" + i + " \">" + encounter.getEncounterType().getName()
			        + "</content></td>");
			Map<EncounterRole, Set<Provider>> encounterProviderMapByRole = encounter.getProvidersByRoles();
			if (encounterProviderMapByRole.values().iterator().hasNext()) {
				Set<Provider> encounterProviders = encounterProviderMapByRole.values().iterator().next();
				Iterator<Provider> encounterProvideIterator = encounterProviders.iterator();
				if (encounterProvideIterator.hasNext()) {
					buffer.append("<td>" + encounterProvideIterator.next() + "</td>");
				} else {
					buffer.append("<td></td>");
				}
			}
			
			buffer.append("<td>" + encounter.getLocation() + "</td>");
			Date date = encounter.getEncounterDatetime();
			buffer.append("<td>" + utils.format(date) + "</td>");
			buffer.append("</tr>");
			Entry encounterEntry = CDAFactory.eINSTANCE.createEntry();
			encounterEntry.setTypeCode(x_ActRelationshipEntry.DRIV);
			org.openhealthtools.mdht.uml.cda.Encounter encounterCCD = CDAFactory.eINSTANCE.createEncounter();
			encounterCCD.setClassCode(ActClass.ENC);
			encounterCCD.setMoodCode(x_DocumentEncounterMood.EVN);
			encounterEntry.setEncounter(encounterCCD);
			II encounterTemplateID = DatatypesFactory.eINSTANCE.createII();
			encounterTemplateID.setRoot("2.16.840.1.113883.3.88.11.83.16");
			encounterTemplateID.setAssigningAuthorityName("HITSP C83");
			encounterCCD.getTemplateIds().add(encounterSectionTemplateID);
			II encounterTemplateID1 = DatatypesFactory.eINSTANCE.createII();
			encounterTemplateID1.setRoot("2.16.840.1.113883.10.20.1.21");
			encounterTemplateID1.setAssigningAuthorityName("CCD");
			encounterCCD.getTemplateIds().add(encounterTemplateID1);
			II encounterTemplateID2 = DatatypesFactory.eINSTANCE.createII();
			encounterTemplateID2.setRoot("1.3.6.1.4.1.19376.1.5.3.1.4.14");
			encounterTemplateID2.setAssigningAuthorityName("IHE PCC");
			encounterCCD.getTemplateIds().add(encounterTemplateID2);
			II encounterID = DatatypesFactory.eINSTANCE.createII();
			encounterID.setRoot(encounter.getUuid());
			encounterCCD.getIds().add(encounterID);
			CD encounterActivityCode = DatatypesFactory.eINSTANCE.createCD();
			encounterActivityCode.setNullFlavor(NullFlavor.UNK);
			ED originalText = DatatypesFactory.eINSTANCE.createED();
			originalText.addText("<reference value=\"#encounterType " + i + "\"/>");
			encounterActivityCode.setOriginalText(originalText);
			encounterCCD.setCode(encounterActivityCode);
			ED text = DatatypesFactory.eINSTANCE.createED();
			text.addText("<reference value=\"#encounterType" + i + "\"/>");
			encounterCCD.setText(text);
			IVL_TS encounterDate = DatatypesFactory.eINSTANCE.createIVL_TS();
			Date ed = encounter.getDateCreated();
			String edate = utils.format(ed);
			encounterDate.setValue(edate);
			encounterCCD.setEffectiveTime(encounterDate);
			Performer2 encounterPerformer = CDAFactory.eINSTANCE.createPerformer2();
			AssignedEntity encounterAssignedEntity = CDAFactory.eINSTANCE.createAssignedEntity();
			II eid = DatatypesFactory.eINSTANCE.createII();
			Map<EncounterRole, Set<Provider>> encounterProviderMapByRole1 = encounter.getProvidersByRoles();
			Set<Provider> encounterProviders1 = null;
			if (encounterProviderMapByRole1.values().iterator().hasNext()) {
				encounterProviders1 = encounterProviderMapByRole1.values().iterator().next();
			}
			
			PN providerName;
			if (encounterProviders1.size() > 0) {
				Provider ep = encounterProviders1.iterator().next();
				eid.setRoot(ep.getUuid());
				encounterAssignedEntity.getIds().add(eid);
				AD providerAddress = DatatypesFactory.eINSTANCE.createAD();
				Person p = ep.getPerson();
				if (p != null) {
					Set<PersonAddress> providerAddSet = p.getAddresses();
					if (!providerAddSet.isEmpty()) {
						PersonAddress personAddress = providerAddSet.iterator().next();
						providerAddress.addStreetAddressLine(personAddress.getAddress1() + personAddress.getAddress2());
						providerAddress.addCity(personAddress.getCityVillage());
						providerAddress.addCountry(personAddress.getCountry());
					}
				}
				
				encounterAssignedEntity.getAddrs().add(providerAddress);
				TEL patientTelecom = DatatypesFactory.eINSTANCE.createTEL();
				patientTelecom.setNullFlavor(NullFlavor.UNK);
				encounterAssignedEntity.getTelecoms().add(patientTelecom);
				org.openhealthtools.mdht.uml.cda.Person assignedProvider = CDAFactory.eINSTANCE.createPerson();
				providerName = DatatypesFactory.eINSTANCE.createPN();
				providerName.addText(ep.getName());
				assignedProvider.getNames().add(providerName);
				encounterAssignedEntity.setAssignedPerson(assignedProvider);
				encounterPerformer.setAssignedEntity(encounterAssignedEntity);
				encounterCCD.getPerformers().add(encounterPerformer);
			}
			
			Participant2 participant = CDAFactory.eINSTANCE.createParticipant2();
			participant.setTypeCode(ParticipationType.LOC);
			II participantTemplateId = DatatypesFactory.eINSTANCE.createII();
			participantTemplateId.setRoot("2.16.840.1.113883.10.20.1.45");
			participant.getTemplateIds().add(participantTemplateId);
			ParticipantRole participantRole = CDAFactory.eINSTANCE.createParticipantRole();
			participantRole.setClassCode(RoleClassRoot.SDLOC);
			II locationId = DatatypesFactory.eINSTANCE.createII();
			locationId.setRoot(encounter.getLocation().getUuid());
			locationId.setExtension(encounter.getLocation().getName());
			participantRole.getIds().add(locationId);
			PlayingEntity playingEntity = CDAFactory.eINSTANCE.createPlayingEntity();
			playingEntity.setClassCode(EntityClassRoot.PLC);
			providerName = DatatypesFactory.eINSTANCE.createPN();
			playingEntity.getNames().add(providerName);
			participantRole.setPlayingEntity(playingEntity);
			participant.setParticipantRole(participantRole);
			encounterCCD.getParticipants().add(participant);
			encounterEntryList.add(encounterEntry);
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		StrucDocText encounterDetails = CDAFactory.eINSTANCE.createStrucDocText();
		encounterDetails.addText(buffer.toString());
		encounterSection.setText(encounterDetails);
		encounterSection.getEntries().addAll(encounterEntryList);
		ccd.addSection(encounterSection);
		return ccd;
	}
}
