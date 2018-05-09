package org.openmrs.module.exportccd.api.generators;

import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.StrucDocText;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.CS;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVXB_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassManufacturedMaterial;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.SetOperator;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentSubstanceMood;
import org.openmrs.CareSetting;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.utils.ExportCcdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class MedicationSectionGenerator {
	
	@Autowired
	private ExportCcdUtils utils;
	
	public ContinuityOfCareDocument buildMedication(ContinuityOfCareDocument ccd, Patient patient) {
		MedicationsSection medicationSection = CCDFactory.eINSTANCE.createMedicationsSection();
		ccd.addSection(medicationSection);
		medicationSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.112", "", "HITSP/C83"));
		medicationSection.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.3.19", "", "IHE PCC"));
		medicationSection.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.8", "", "HL7 CCD"));
		medicationSection.setCode(utils
		        .buildCodeCE("10160-0", "2.16.840.1.113883.6.1", "History of medication use", "LOINC"));
		medicationSection.setTitle(utils.buildST("Medication"));
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\" width=\"100%\">");
		buffer.append("<thead>");
		buffer.append("<tr>");
		buffer.append("<th>Medication</th>");
		buffer.append("<th>Effective Date</th>");
		buffer.append("<th>Dose</th>");
		buffer.append("<th>Days</th>");
		buffer.append("</tr>");
		buffer.append("</thead>");
		buffer.append("<tbody>");
		
		OrderService orderService = Context.getOrderService();
		OrderType orderType = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
		Set<Order> drugOrders = new HashSet<Order>();
		for (CareSetting careSetting : orderService.getCareSettings(false)) {
			drugOrders.addAll(orderService.getOrders(patient, careSetting, orderType, false));
		}
		List<Entry> drugOrderEntryList = new ArrayList();
		int i = 0;
		
		for (Iterator i$ = drugOrders.iterator(); i$.hasNext(); ++i) {
			DrugOrder drugOrder = (DrugOrder) i$.next();
			buffer.append("<tr>");
			buffer.append("<td><content ID=\"drug" + i + "\">" + drugOrder.getDrug().getName() + "</content></td>");
			Date date = drugOrder.getEffectiveStartDate();
			buffer.append("<td>" + utils.format(date) + "</td>");
			buffer.append("<td>" + drugOrder.getDose() + "</td>");
			buffer.append("<td>" + drugOrder.getFrequency() + "</td>");
			buffer.append("</tr>");
			Entry medicationEntry = CDAFactory.eINSTANCE.createEntry();
			medicationEntry.setTypeCode(x_ActRelationshipEntry.DRIV);
			SubstanceAdministration medicationSubstance = CDAFactory.eINSTANCE.createSubstanceAdministration();
			medicationSubstance.setClassCode(ActClass.SBADM);
			medicationSubstance.setMoodCode(x_DocumentSubstanceMood.EVN);
			medicationSubstance.getTemplateIds()
			        .add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.7.1", "", "IHE PCC"));
			medicationSubstance.getTemplateIds().add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.7", "", "IHE PCC"));
			medicationSubstance.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.24", "", "CCD"));
			medicationSubstance.getIds().add(utils.buildID(drugOrder.getDrug().getUuid(), ""));
			CS statusCode = DatatypesFactory.eINSTANCE.createCS();
			statusCode.setCode("completed");
			medicationSubstance.setStatusCode(statusCode);
			
			Double medicationFrequency = drugOrder.getFrequency().getFrequencyPerDay();
			IVL_TS e = DatatypesFactory.eINSTANCE.createIVL_TS();
			IVXB_TS startDate = DatatypesFactory.eINSTANCE.createIVXB_TS();
			SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss");
			String creationDate = s.format(drugOrder.getEffectiveStartDate());
			startDate.setValue(creationDate);
			e.setLow(startDate);
			medicationSubstance.getEffectiveTimes().add(e);
			PIVL_TS e1 = DatatypesFactory.eINSTANCE.createPIVL_TS();
			e1.setOperator(SetOperator.A);
			PQ period = DatatypesFactory.eINSTANCE.createPQ();
			period.setUnit("h");
			period.setValue(24.0D / medicationFrequency);
			e1.setPeriod(period);
			medicationSubstance.getEffectiveTimes().add(e1);
			medicationSubstance.setText(utils.buildEDText("#drug" + i));
			IVL_PQ dose = DatatypesFactory.eINSTANCE.createIVL_PQ();
			
			//TODO:
			//dose.setUnit(drugOrder.getUnits());
			dose.setUnit("");
			
			dose.setValue(drugOrder.getDose());
			medicationSubstance.setDoseQuantity(dose);
			++i;
			Consumable consumable = CDAFactory.eINSTANCE.createConsumable();
			consumable.setTypeCode(ParticipationType.CSM);
			ManufacturedProduct manufacturedProduct = CDAFactory.eINSTANCE.createManufacturedProduct();
			manufacturedProduct.getTemplateIds().add(
			    utils.buildTemplateID("2.16.840.1.113883.3.88.11.83.8.2", "", "HITSP C83"));
			manufacturedProduct.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.53", "", "CCD"));
			manufacturedProduct.getTemplateIds()
			        .add(utils.buildTemplateID("1.3.6.1.4.1.19376.1.5.3.1.4.7.2", "", "IHE PCC"));
			Material manufacturedMaterial = CDAFactory.eINSTANCE.createMaterial();
			manufacturedMaterial.setClassCode(EntityClassManufacturedMaterial.MMAT);
			CE materialCode = utils.buildConceptCode(drugOrder.getConcept(), "RxNorm");
			materialCode.setOriginalText(utils.buildEDText("#drug" + i));
			manufacturedMaterial.setCode(materialCode);
			manufacturedProduct.setManufacturedMaterial(manufacturedMaterial);
			consumable.setManufacturedProduct(manufacturedProduct);
			medicationSubstance.setConsumable(consumable);
			EntryRelationship medicationStatus = CDAFactory.eINSTANCE.createEntryRelationship();
			medicationStatus.setTypeCode(x_ActRelationshipEntryRelationship.REFR);
			Observation medicationStatusObs = CDAFactory.eINSTANCE.createObservation();
			medicationStatus.setObservation(medicationStatusObs);
			medicationStatusObs.setMoodCode(x_ActMoodDocumentObservation.EVN);
			medicationStatusObs.setClassCode(ActClassObservation.OBS);
			medicationStatusObs.getTemplateIds().add(utils.buildTemplateID("2.16.840.1.113883.10.20.1.47", "", ""));
			medicationStatusObs.setCode(utils.buildCode("33999-4", "2.16.840.1.113883.6.1", "Status", "LOINC"));
			medicationStatusObs.getValues().add(
			    utils.buildCodeCE("55561003", "2.16.840.1.113883.6.96", "Active", "SNOMED-CT"));
			medicationSubstance.getEntryRelationships().add(medicationStatus);
			medicationEntry.setSubstanceAdministration(medicationSubstance);
			drugOrderEntryList.add(medicationEntry);
		}
		
		buffer.append("</tbody>");
		buffer.append("</table>");
		StrucDocText medicationDetails = CDAFactory.eINSTANCE.createStrucDocText();
		medicationDetails.addText(buffer.toString());
		medicationSection.setText(medicationDetails);
		medicationSection.getEntries().addAll(drugOrderEntryList);
		return ccd;
	}
}
