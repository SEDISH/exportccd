//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openmrs.module.exportccd.web.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.exportccd.api.PatientSummaryExportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping({ "/module/exportccd/ccdConfiguration*" })
public class ExportCCDManageController {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public ExportCCDManageController() {
	}
	
	@RequestMapping(method = { RequestMethod.GET })
	public void manage(ModelMap model, HttpServletRequest request) {
		PatientSummaryExportService patientSummaryService = (PatientSummaryExportService) Context
		        .getService(PatientSummaryExportService.class);
		model.addAttribute("VitalSigns", patientSummaryService.getConceptByCategory("VitalSigns"));
		model.addAttribute("SocialHistory", patientSummaryService.getConceptByCategory("SocialHistory"));
		model.addAttribute("LabResults", patientSummaryService.getConceptByCategory("LabResults"));
		model.addAttribute("PlanOfCare", patientSummaryService.getConceptByCategory("PlanOfCare"));
		model.addAttribute("FamilyHistory", patientSummaryService.getConceptByCategory("FamilyHistory"));
	}
	
	@RequestMapping(method = { RequestMethod.POST })
	public void getParameters(HttpServletRequest request, ModelMap model) {
		this.manageSections(request, "VitalSigns");
		PatientSummaryExportService patientSummaryService = (PatientSummaryExportService) Context
		        .getService(PatientSummaryExportService.class);
		model.addAttribute("VitalSigns", patientSummaryService.getConceptByCategory("VitalSigns"));
		this.manageSections(request, "SocialHistory");
		model.addAttribute("SocialHistory", patientSummaryService.getConceptByCategory("SocialHistory"));
		this.manageSections(request, "LabResults");
		model.addAttribute("LabResults", patientSummaryService.getConceptByCategory("LabResults"));
		this.manageSections(request, "PlanOfCare");
		model.addAttribute("PlanOfCare", patientSummaryService.getConceptByCategory("PlanOfCare"));
		this.manageSections(request, "FamilyHistory");
		model.addAttribute("FamilyHistory", patientSummaryService.getConceptByCategory("FamilyHistory"));
	}
	
	private void manageSections(HttpServletRequest request, String section) {
		PatientSummaryExportService patientSummaryService = (PatientSummaryExportService) Context
		        .getService(PatientSummaryExportService.class);
		request.getParameter(section + "Counter");
		List<Concept> conceptList = patientSummaryService.getConceptByCategory(section);
		int j = Integer.parseInt(request.getParameter(section + "Counter"));
		List conceptIds = new ArrayList();
		int ii;
		if (j > 0) {
			for (ii = 0; ii < j; ++ii) {
				try {
					Integer conceptId = Integer.parseInt(request.getParameter(section + ii + "_span_hid"));
					conceptIds.add(conceptId);
				}
				catch (NumberFormatException var14) {
					var14.printStackTrace();
				}
			}
		}
		
		ii = conceptList.size();
		int i = 0;
		
		while (i < ii) {
			Integer dbConcept = ((Concept) conceptList.get(i)).getConceptId();
			if (conceptIds.contains(dbConcept)) {
				conceptIds.remove(dbConcept);
				conceptList.remove(Context.getConceptService().getConcept(dbConcept));
				--i;
			} else {
				++i;
			}
		}
		
		try {
			if (conceptIds.size() > 0) {
				patientSummaryService.saveConceptAsCCDSections(conceptIds, section);
			}
			
			if (conceptList.size() > 0) {
				patientSummaryService.deleteConceptsByCategory(conceptList, section);
			}
			
			request.setAttribute("openmrs_error", "");
		}
		catch (DAOException var11) {
			request.setAttribute("openmrs_error", Context.getMessageSourceService().getMessage("ExportCCD.could.not.save"));
		}
		catch (APIException var12) {
			request.setAttribute("openmrs_error", var12.getMessage());
		}
		catch (Exception var13) {
			request.setAttribute("openmrs_error", var13.getMessage());
		}
		
	}
}
