package com.matchFit.participation.dto.request;

import lombok.Getter;

@Getter
public class ManageApplicant {
	
	private Long applicantId;
    private Decision decision;

    public enum Decision {
        ACCEPT, REJECT
    }
}
