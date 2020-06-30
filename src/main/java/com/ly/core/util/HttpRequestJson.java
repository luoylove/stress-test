package com.ly.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class HttpRequestJson {
	private String url;
	private Map<String, String> headers;
	private String body;	
}
