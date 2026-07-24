package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.BOTH)
public class Constants {
	public static final String MOD_ID = "easyhopper";
	public static final String MOD_NAME = "EasyHopper";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
}