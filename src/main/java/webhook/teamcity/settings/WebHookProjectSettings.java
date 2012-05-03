package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.jdom.Element;

import webhook.teamcity.BuildState;
import webhook.teamcity.Loggers;


public class WebHookProjectSettings implements ProjectSettings {
	private static final String NAME = WebHookProjectSettings.class.getName();
	ProjectSettingsManager psm;
	ProjectSettings ps;
	private Boolean webHooksEnabled = true;
	private Boolean updateSucess = false;
	private String updateMessage = "";
	private CopyOnWriteArrayList<WebHookConfig> webHooksConfigs;
	
	public WebHookProjectSettings(){
		webHooksConfigs = new CopyOnWriteArrayList<WebHookConfig>();
	}

    @SuppressWarnings("unchecked")
	public void readFrom(Element rootElement)
    /* Is passed an Element by TC, and is expected to persist it to the settings object.
     * Old settings should be overwritten.
     */
    {
    	Loggers.SERVER.debug("readFrom :: " + rootElement.toString());
    	CopyOnWriteArrayList<WebHookConfig> configs = new CopyOnWriteArrayList<WebHookConfig>();
    	
    	if (rootElement.getAttribute("enabled") != null){
    		this.webHooksEnabled = Boolean.parseBoolean(rootElement.getAttributeValue("enabled"));
    	}
    	
		List<Element> namedChildren = rootElement.getChildren("webhook");
        if(namedChildren.size() == 0)
        {
            this.webHooksConfigs = null;
        } else {
			for(Iterator<Element> i = namedChildren.iterator(); i.hasNext();)
	        {
				Element e = i.next();
				WebHookConfig whConfig = new WebHookConfig(e);
				Loggers.SERVER.debug(e.toString());
				configs.add(whConfig);
				Loggers.SERVER.debug(NAME + ":readFrom :: url " + whConfig.getUrl());
				Loggers.SERVER.debug(NAME + ":readFrom :: enabled " + String.valueOf(whConfig.getEnabled()));
				//Loggers.SERVER.debug(NAME + ":readFrom :: statemask " + String.valueOf(whConfig.getStatemask()));
				Loggers.SERVER.debug(NAME + ":readFrom :: payloadFormat " + String.valueOf(whConfig.getPayloadFormat()));
	        }
			this.webHooksConfigs = configs;
    	}
    }

    public void writeTo(Element parentElement)
    /* Is passed an (probably empty) Element by TC, which is expected to be populated from the settings
     * in memory. 
     */
    {
    	Loggers.SERVER.debug(NAME + ":writeTo :: " + parentElement.toString());
    	parentElement.setAttribute("enabled", String.valueOf(this.webHooksEnabled));
        if(webHooksConfigs != null)
        {
            for(Iterator<WebHookConfig> whConf = webHooksConfigs.iterator(); whConf.hasNext();)
            {
                WebHookConfig whc = whConf.next();
            	Element el = whc.getAsElement();
            	Loggers.SERVER.debug(el.toString());
                parentElement.addContent(el);
				Loggers.SERVER.debug(NAME + ":writeTo :: url " + whc.getUrl());
				Loggers.SERVER.debug(NAME + ":writeTo :: enabled " + String.valueOf(whc.getEnabled()));
				//Loggers.SERVER.debug(NAME + ":writeTo :: statemask " + String.valueOf(whc.getStatemask()));
				Loggers.SERVER.debug(NAME + ":writeTo :: payloadFormat " + String.valueOf(whc.getPayloadFormat()));
            }

        }
    }
    
    public List<WebHookConfig> getWebHooksAsList(){
    	return this.webHooksConfigs;
    }    
	
    public String getWebHooksAsString(){
    	String tmpString = "";
    	for(Iterator<WebHookConfig> whConf = webHooksConfigs.iterator(); whConf.hasNext();)
    	{
    		tmpString = tmpString + whConf.next().getUrl() + "<br/>";
    	}
    	return tmpString;
    }

    public void deleteWebHook(String webHookId, String ProjectId){
        if(this.webHooksConfigs != null)
        {
        	updateSucess = false;
        	updateMessage = "";
        	List<WebHookConfig> tempWebHookList = new ArrayList<WebHookConfig>();
            for(Iterator<WebHookConfig> whConf = this.webHooksConfigs.iterator(); whConf.hasNext();)
            {
                WebHookConfig whc = whConf.next();
                if (whc.getUniqueKey().equals(webHookId)){
                	Loggers.SERVER.debug(NAME + ":deleteWebHook :: Deleting webhook from " + ProjectId + " with URL " + whc.getUrl());
                	tempWebHookList.add(whc);
                }
            }
            if (tempWebHookList.size() > 0){
            	this.updateSucess = true;
            	this.webHooksConfigs.removeAll(tempWebHookList);	
            }
        }    	
    }

	public void updateWebHook(String ProjectId, String webHookId, String URL, Boolean enabled, BuildState buildState, String format) {
        if(this.webHooksConfigs != null)
        {
        	updateSucess = false;
        	updateMessage = "";
            for(Iterator<WebHookConfig> whConf = this.webHooksConfigs.iterator(); whConf.hasNext();)
            {
                WebHookConfig whc = whConf.next();
                if (whc.getUniqueKey().equals(webHookId)){
                	whc.setEnabled(enabled);
                	whc.setUrl(URL);
                	whc.setBuildStates(buildState);
                	whc.setPayloadFormat(format);
                	Loggers.SERVER.debug(NAME + ":updateWebHook :: Updating webhook from " + ProjectId + " with URL " + whc.getUrl());
                   	this.updateSucess = true;
                }
            }
        }    			
	}

	public void addNewWebHook(String ProjectId, String URL, Boolean enabled, BuildState buildState, String format) {
		this.webHooksConfigs.add(new WebHookConfig(URL, enabled, buildState, format));
		Loggers.SERVER.debug(NAME + ":addNewWebHook :: Adding webhook to " + ProjectId + " with URL " + URL);
		this.updateSucess = true;
	}

/*	public void addNewWebHook(String ProjectId, String uniqueKey, String URL, Boolean enabled,Integer buildState) {
		this.webHooksConfigs.add(new WebHookConfig(uniqueKey,URL,enabled,buildState));
		Loggers.SERVER.debug(NAME + ":addNewWebHook :: Adding webhook to " + ProjectId + " with URL " + URL);
		this.updateSucess = true;
	}	
*/	
    public Boolean updateSuccessful(){
    	return this.updateSucess;
    }
    
	public void dispose() {
		Loggers.SERVER.debug(NAME + ":dispose() called");
	}

	public Integer getWebHooksCount(){
		return this.webHooksConfigs.size();
	}
	
	public Boolean isEnabled() {
		return webHooksEnabled;
	}

	public String isEnabledAsChecked() {
		if (this.webHooksEnabled){
			return "checked ";
		}
		return "";
	}
	
	public List<WebHookConfig> getWebHooksConfigs() {
		return webHooksConfigs;
	}

	public String getUpdateMessage() {
		return updateMessage;
	}

}
