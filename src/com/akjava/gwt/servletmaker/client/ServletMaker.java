package com.akjava.gwt.servletmaker.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.akjava.gwt.lib.client.GWTHTMLUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.ValueUtils;
import com.akjava.gwt.servletmaker.client.resources.Bundles;
import com.akjava.lib.common.utils.ValuesUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ServletMaker implements EntryPoint {
	

	private TextArea output;
	private TextArea input;

	
	private TextToClassCsv textToClassCsv=new TextToClassCsv();
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		 VerticalPanel root=new VerticalPanel();
		 RootPanel.get().add(root);
		 
		 PasteValueReceiveArea test=new PasteValueReceiveArea();
		 test.setStylePrimaryName("readonly");
		 test.setText("Click(Focus) & Paste Here");
		 root.add(test);
		 test.setSize("600px", "60px");
		 test.setFocus(true);
		 test.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String line=textToClassCsv.toCsv(event.getValue());
				if(line==null || line.isEmpty()){
					return;
				}
				GWT.log(line);
				String[] className_package=line.split(",");
				String className="";
				String packageName="";
				className=className_package[0];
				if(className_package.length>1){
					packageName=className_package[1];
				}
				String csv=className+","+packageName+","+toServletName(className)+","+toPathValue(className);
				input.setText(input.getText()+csv+"\n");
				doConvert();
			}
			 
		});
		 
		 input = new TextArea();
		 GWTHTMLUtils.setPlaceHolder(input, "className,package,servletName,path");
		 input.setSize("600px","200px");
		 root.add(input);
		 Button convert=new Button("Convert",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doConvert();
			}
		});
		 
		 root.add(convert);
		 output = new TextArea();
		 output.setSize("600px","200px");
		 output.setReadOnly(true);
		 output.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				output.selectAll();
			}
		});
		 GWTHTMLUtils.setPlaceHolder(output,"output would be generated here.click and select");
		 root.add(output);
		 
		 
		 
	}
	private String toServletName(String className){
		return ValuesUtils.removeSuffix(className, "Servlet");
	}
	private String toPathValue(String className){
		return "/"+toServletName(className).toLowerCase();
	}
	//manually convert csv to servlets
	protected void doConvert() {
		List<String[]> csvs=ValueUtils.csvToArrayList(input.getText(), ',');
		List<List<String>> values=new ArrayList<List<String>>();
		for(String[] csv:csvs){
			//TODO should do function
			String[] name_full_path=new String[3];
			name_full_path[0]=csv[0];
			if(csv.length>1){
				name_full_path[1]=csv[1]+"."+csv[0];
				if(csv.length>2){
					name_full_path[0]=csv[2];
					if(csv.length>3){
						name_full_path[2]=csv[3];
						
					}else{
						name_full_path[2]=csv[0];
					}
				}else{
					name_full_path[2]=csv[0];
				}
				
			}else{
				name_full_path[1]=csv[0];
				name_full_path[2]=csv[0];
			}
			List<String> value=Lists.newArrayList(name_full_path);
			values.add(value);
		}
		
		CsvToMapFunction function=new CsvToMapFunction(Lists.newArrayList("name","classfull","path"));
		List<Map<String,String>> maps=Lists.transform(values, function);
		
		String out=Joiner.on("\n").join(Lists.transform(maps, new MapToTemplatedTextFunction(Bundles.INSTANCE.servlet().getText())));
		output.setText(out);
	}

	//TODO move to common
	/**
	 * only work on Chrome(web-kit)
	 * to catch new value add addValueChangeHandler
	 * 
	 * 	addValueChangeHandler(new ValueChangeHandler<String>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					GWT.log("changed:"+event.getValue());
				}
			});
	 * ref
	 * https://groups.google.com/forum/?fromgroups#!topic/google-web-toolkit/CaNSdwfSK-A
	 * @author aki
	 *
	 */
	public class PasteValueReceiveArea extends TextArea{
		public PasteValueReceiveArea(){
			super();
			sinkEvents(Event.ONPASTE);
			
			setReadOnly(true);
		}
		public void onBrowserEvent(Event event) {
		    super.onBrowserEvent(event);
		    switch (event.getTypeInt()) {
		        case Event.ONPASTE: {
		        	LogUtils.log(event);
		        	GWT.log("paste");
		        	ValueChangeEvent.fire(this, getPastedText(event));
		            break;
		        }
		    }
		}
	}
	
	public static native String getPastedText(Event event)
    /*-{

        var text = "";

        if (event.clipboardData) // WebKit/Chrome/Safari
        {
            try
            {
                text = event.clipboardData.getData("Text");
                return text;
            }
            catch (e)
            {
                // Hmm, that didn't work.
            }
        }
        return text;
        }-*/;
}
