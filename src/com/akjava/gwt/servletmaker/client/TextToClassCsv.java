package com.akjava.gwt.servletmaker.client;

import java.util.List;

import com.akjava.gwt.lib.client.Regexs;
import com.akjava.gwt.lib.client.ValueUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class TextToClassCsv {

	//className,package,servletName,path
	public String toCsv(String text){
		//TODO create method
		text=ValueUtils.toNLineSeparator(text);
		List<String> lines=Lists.newArrayList(Splitter.on("\n").split(text));
		
		//List<String> returnValues=new ArrayList<String>();
		if(lines.size()==1){
			//parse one line
			String line=lines.get(0);
			if(line.toLowerCase().endsWith(".java")){//care lower case
				String className=line.substring(0,line.length()-5);
				return className+",";
			}else{
				return line+",";
			}
		}else{
			String packageName="";
			String className="";
			RegExp classPattern=RegExp.compile(Regexs.GET_CLASS_NAME);
			RegExp packagePattern=RegExp.compile(Regexs.GET_PACKAGE_NAME);
			for(String line:lines){
				if(packageName.isEmpty()){
				MatchResult  pmatch=packagePattern.exec(line);
				if(pmatch!=null){
					packageName=pmatch.getGroup(1);
				}
				}
				if(className.isEmpty()){
					MatchResult  cmatch=classPattern.exec(line);
					if(cmatch!=null){
						className=cmatch.getGroup(1);
					}
				}
			}
			return className+","+packageName;
		}
		//return null;
	}
}
