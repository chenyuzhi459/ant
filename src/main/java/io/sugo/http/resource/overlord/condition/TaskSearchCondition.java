package io.sugo.http.resource.overlord.condition;



import javafx.util.Pair;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Map;


public class TaskSearchCondition {
	private String taskId;
	private String taskTopic;
	private String taskStatus;
	private Map<String,String> taskSortItem;
	private Map<String,Integer> taskPageItem;

	public TaskSearchCondition() {

	}

	public TaskSearchCondition(String taskId, String taskTopic, String taskStatus) {
		this.taskId = taskId;
		this.taskTopic = taskTopic;
		this.taskStatus = taskStatus;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getTaskTopic() {
		return taskTopic;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public Map<String,String> getTaskSortItem() {
		return taskSortItem;
	}

	public Map<String,Integer> getTaskPageItem() {
		return taskPageItem;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public void setTaskTopic(String taskTopic) {
		this.taskTopic = taskTopic;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public void setTaskSortItem(Map<String,String> taskSortItem) {
		this.taskSortItem = taskSortItem;
	}

	public void setTaskPageItem(Map<String,Integer> taskPageItem) {
		this.taskPageItem = taskPageItem;
	}
}
