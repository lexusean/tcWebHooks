package webhook.teamcity;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.WebHookProxyConfig;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookMainSettings;

public class WebHookFactoryImpl implements WebHookFactory {

	final WebHookMainSettings myMainSettings;
	final WebHookAuthenticatorProvider myAuthenticatorProvider;
	final WebHookHttpClientFactory myWebHookHttpClientFactory;
	
	public WebHookFactoryImpl(WebHookMainSettings mainSettings, WebHookAuthenticatorProvider authenticatorProvider, WebHookHttpClientFactory webHookHttpClientFactory) {
		this.myMainSettings = mainSettings;
		this.myAuthenticatorProvider = authenticatorProvider;
		this.myWebHookHttpClientFactory = webHookHttpClientFactory;
	}

	public WebHook getWebHook(WebHookConfig webHookConfig, WebHookProxyConfig proxyConfig) {
		WebHook webHook = new WebHookImpl(webHookConfig.getUrl(), proxyConfig, myWebHookHttpClientFactory.getHttpClient());
		webHook.setUrl(webHookConfig.getUrl());
		webHook.setEnabled(webHookConfig.getEnabled());
		if (!webHookConfig.getEnabled()) {
			webHook.getExecutionStats().setStatusReason(WebHookExecutionException.WEBHOOK_DISABLED_ERROR_MESSAGE);
			webHook.getExecutionStats().setStatusCode(WebHookExecutionException.WEBHOOK_DISABLED_ERROR_CODE);
		}
		webHook.setBuildStates(webHookConfig.getBuildStates());
		if (webHookConfig.getAuthenticationConfig() != null){
			WebHookAuthenticator auth = myAuthenticatorProvider.getAuthenticator(webHookConfig.getAuthenticationConfig().getType());
			if (auth != null){
				auth.setWebHookAuthConfig(webHookConfig.getAuthenticationConfig());
				webHook.setAuthentication(auth);
			} else {
				Loggers.SERVER.warn("Could not enable authentication type '" + webHookConfig.getAuthenticationConfig().getType() + "' for URL " + webHookConfig.getUrl() );
			}
		}
		
		if (webHookConfig.getTriggerFilters() != null && webHookConfig.getTriggerFilters().size() > 0){
			for (WebHookFilterConfig filter : webHookConfig.getTriggerFilters()){
				webHook.addFilter(WebHookFilterConfig.copy(filter));
			}
		}
		
		webHook.setProxy(myMainSettings.getProxyConfigForUrl(webHookConfig.getUrl()));
		webHook.setConnectionTimeOut(myMainSettings.getHttpConnectionTimeout());
		webHook.setResponseTimeOut(myMainSettings.getHttpResponseTimeout());

		return webHook;
	}

	@Override
	public WebHook getWebHook() {
		return new WebHookImpl();
	}
	
	
	
}
