package com.ly.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * http 表单模式req
 * @author luoyoujun
 *
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class HttpRequestParams {
	private String url;
	private Map<String, String> params;
	private Map<String, String> headers;
}
