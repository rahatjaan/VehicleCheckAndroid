package com.matt.vehiclecheck.activity;

public class CheckListData {

	private String name;
	private boolean selected;
	private Integer questionId;

	/**
	 * @return the questionId
	 */
	public Integer getQuestionId() {
		return questionId;
	}

	/**
	 * @param questionId the questionId to set
	 */
	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	public CheckListData(String name, boolean selected, Integer questionId) {
		super();
		this.name = name;
		this.selected = selected;
		this.questionId = questionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
