//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openmrs.module.exportccd.api.impl;

import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.exportccd.api.PatientSummaryExportService;
import org.openmrs.module.exportccd.api.generators.AllergySectionGenerator;
import org.openmrs.module.exportccd.api.generators.FamilyHistorySectionGenerator;
import org.openmrs.module.exportccd.api.generators.HeaderGenerator;
import org.openmrs.module.exportccd.api.generators.LabResultsSectionGenerator;
import org.openmrs.module.exportccd.api.generators.MedicationSectionGenerator;
import org.openmrs.module.exportccd.api.generators.PlanOfCareSectionGenerator;
import org.openmrs.module.exportccd.api.generators.ProblemsSectionGenerator;
import org.openmrs.module.exportccd.api.generators.SocialHistorySectionGenerator;
import org.openmrs.module.exportccd.api.generators.VitalSignsSectionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientSummaryExportServiceImpl extends BaseOpenmrsService implements PatientSummaryExportService {
	
	@Autowired
	private AllergySectionGenerator allergySectionGenerator;
	
	@Autowired
	private FamilyHistorySectionGenerator familyHistorySectionGenerator;
	
	@Autowired
	private LabResultsSectionGenerator labResultsSectionGenerator;
	
	@Autowired
	private MedicationSectionGenerator medicationSectionGenerator;
	
	@Autowired
	private PlanOfCareSectionGenerator planOfCareSectionGenerator;
	
	@Autowired
	private ProblemsSectionGenerator problemsSectionGenerator;
	
	@Autowired
	private SocialHistorySectionGenerator socialHistorySectionGenerator;
	
	@Autowired
	private VitalSignsSectionGenerator vitalSignsSectionGenerator;
	
	@Autowired
	private HeaderGenerator headerGenerator;
	
	public PatientSummaryExportServiceImpl() {
	}
	
	public ContinuityOfCareDocument produceCCD(int patientId) {
		ContinuityOfCareDocument ccd = CCDFactory.eINSTANCE.createContinuityOfCareDocument();
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(patientId);
		System.out.println(patient);
		ccd = headerGenerator.buildHeader(ccd, patient);
		ccd = allergySectionGenerator.buildAllergies(ccd, patient);
		ccd = problemsSectionGenerator.buildProblems(ccd, patient);
		ccd = medicationSectionGenerator.buildMedication(ccd, patient);
		ccd = vitalSignsSectionGenerator.buildVitalSigns(ccd, patient);
		ccd = socialHistorySectionGenerator.buildSocialHistory(ccd, patient);
		ccd = labResultsSectionGenerator.buildLabResults(ccd, patient);
		ccd = planOfCareSectionGenerator.buildPlanOfCare(ccd, patient);
		ccd = familyHistorySectionGenerator.buildFamilyHistory(ccd, patient);
		
		try {
			CDAUtil.save(ccd, System.out);
		}
		catch (Exception var6) {
			var6.printStackTrace();
		}
		
		return ccd;
	}
}
