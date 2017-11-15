package io.sugo.http.resource.overlord.condition;

import java.text.MessageFormat;
import java.util.Map;

public class SupervisorSearchCondition {
	private String supervisorId;
	private String supervisorType;
	private Map<String,String> supervisorSortItem;
	private Map<String,Integer> supervisorPageItem;

	public SupervisorSearchCondition() {

	}

	public SupervisorSearchCondition(String supervisorId, String supervisorType) {
		this.supervisorId = supervisorId;
		this.supervisorType = supervisorType;
	}

	public String getSupervisorId() {
		return supervisorId;
	}

	public String getSupervisorType() {
		return supervisorType;
	}

	public Map<String,String> getSupervisorSortItem() {
		return supervisorSortItem;
	}

	public Map<String,Integer> getSupervisorPageItem() {
		return supervisorPageItem;
	}

	public void setSupervisorId(String supervisorId) {
		this.supervisorId = supervisorId;
	}

	public void setSupervisorType(String supervisorType) {
		this.supervisorType = supervisorType;
	}

	public void setSupervisorSortItem(Map<String, String> supervisorSortItem) {
		this.supervisorSortItem = supervisorSortItem;
	}

	public void setSupervisorPageItem(Map<String,Integer> supervisorPageItem) {
		this.supervisorPageItem = supervisorPageItem;
	}

}
