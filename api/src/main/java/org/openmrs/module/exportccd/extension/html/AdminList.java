//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.openmrs.module.exportccd.extension.html;

import java.util.LinkedHashMap;
import java.util.Map;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.module.web.extension.AdministrationSectionExt;

public class AdminList extends AdministrationSectionExt {
	
	public AdminList() {
	}
	
	public MEDIA_TYPE getMediaType() {
		return MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "exportccd.title";
	}
	
	public Map<String, String> getLinks() {
		LinkedHashMap<String, String> map = new LinkedHashMap();
		map.put("/module/exportccd/ccdConfiguration.form", "ExportCCD.manage.ccd.configuration");
		map.put("/module/exportccd/exportPatient.form", "ExportCCD.export");
		return map;
	}
}
