//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openmrs.module.exportccd.web.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletResponse;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.exportccd.api.PatientSummaryExportService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping({ "/module/exportccd/exportPatient*" })
public class ExportPatientSummaryController {
	
	public ExportPatientSummaryController() {
	}
	
	@RequestMapping(method = { RequestMethod.POST })
	public void manage(@RequestParam(value = "patientId", required = true) Patient patient, HttpServletResponse response) {
		if (patient != null) {
			System.out.println(patient.getId());
			PatientSummaryExportService yservice = (PatientSummaryExportService) Context
			        .getService(PatientSummaryExportService.class);
			ContinuityOfCareDocument ccd = yservice.produceCCD(patient.getId().intValue());
			response.setHeader("Content-Disposition", "attachment;filename=" + patient.getGivenName() + ".xml");
			
			try {
				StringWriter r = new StringWriter();
				CDAUtil.save(ccd, r);
				String ccdDoc = r.toString();
				ccdDoc = ccdDoc.replaceAll("&lt;", "<");
				ccdDoc = ccdDoc.replaceAll("&quot;", "\"");
				byte[] res = ccdDoc.getBytes(Charset.forName("UTF-8"));
				response.setCharacterEncoding("UTF-8");
				response.getOutputStream().write(res);
				response.flushBuffer();
			}
			catch (IOException var8) {
				var8.printStackTrace();
			}
			catch (Exception var9) {
				var9.printStackTrace();
			}
		}
		
	}
	
	@RequestMapping(method = { RequestMethod.GET })
	public void manage(@RequestParam(value = "patientId", required = false) Patient patient) {
		System.out.println("Hellow World");
	}
}
