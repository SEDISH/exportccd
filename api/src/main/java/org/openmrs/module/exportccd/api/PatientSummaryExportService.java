//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openmrs.module.exportccd.api;

import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PatientSummaryExportService extends OpenmrsService {
	
	ContinuityOfCareDocument produceCCD(int var1);
}
