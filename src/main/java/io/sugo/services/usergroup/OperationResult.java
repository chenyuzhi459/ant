package io.sugo.services.usergroup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.zookeeper.Op;
import sun.text.resources.cldr.ia.FormatData_ia;

import static io.sugo.services.usergroup.OperationResult.OperationStatus.FAILED;
import static io.sugo.services.usergroup.OperationResult.OperationStatus.SUCCESS;

/**
 * Created by chenyuzhi on 19-8-8.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
public class OperationResult {
	private final String id;
	private OperationStatus status;
	private Long updatedRows;
	private Long totalRows;
	private Long requestTime;
	private Long startTime;
	private Long endTime;

	public OperationResult(String id) {
		this.id = id;
	}

	@JsonCreator
	public OperationResult(
			@JsonProperty("id") String id,
			@JsonProperty("status") OperationStatus status,
			@JsonProperty("updatedRows") Long updatedRows,
			@JsonProperty("totalRows") Long totalRows,
			@JsonProperty("requestTime") Long requestTime,
			@JsonProperty("startTime") Long startTime,
			@JsonProperty("endTime") Long endTime) {
		this.id = id;
		this.status = status;
		this.updatedRows = updatedRows;
		this.totalRows = totalRows;
		this.requestTime = requestTime;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@JsonProperty
	public String getId() {
		return id;
	}

	@JsonProperty
	public OperationStatus getStatus() {
		return status;
	}

	@JsonProperty
	public Long getUpdatedRows() {
		return updatedRows;
	}

	@JsonProperty
	public Long getTotalRows() {
		return totalRows;
	}

	@JsonProperty
	public Long getRequestTime() {
		return requestTime;
	}

	@JsonProperty
	public Long getStartTime() {
		return startTime;
	}

	@JsonProperty
	public Long getEndTime() {
		return endTime;
	}

	public OperationResult setStatus(OperationStatus status) {
		this.status = status;
		return this;
	}

	public OperationResult setUpdatedRows(Long updatedRows) {
		this.updatedRows = updatedRows;
		return this;
	}

	public OperationResult setTotalRows(Long totalRows) {
		this.totalRows = totalRows;
		return this;
	}

	public OperationResult setRequestTime(Long requestTime) {
		this.requestTime = requestTime;
		return this;
	}

	public OperationResult setStartTime(Long startTime) {
		this.startTime = startTime;
		return this;
	}

	public OperationResult setEndTime(Long endTime) {
		this.endTime = endTime;
		return this;
	}



	public  enum OperationStatus  {
		ACCEPTED("accepted"),
		PREPARE("prepare"),
		RUNNING("running"),
		FAILED("failed"),
		SUCCESS("success");

		String status;
		OperationStatus(String status){
			this.status = status;
		}

		@Override
		public String toString() {
			return status.toUpperCase();
		}

		public static boolean isFinished(OperationStatus status){
			return status.equals(FAILED)||status.equals(SUCCESS);
		}
	}
}

