package com.neotys.ps.fix.acceptor.logon;

import com.google.common.base.Optional;
import com.neotys.action.argument.Arguments;
import com.neotys.action.argument.Option.AppearsByDefault;
import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class AcceptorLogonAction implements Action{
	private static final String BUNDLE_NAME = "com.neotys.ps.fix.acceptor.logon.bundle";
	private static final String DISPLAY_NAME = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	private static final String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");
	private static final ImageIcon DISPLAY_ICON = new ImageIcon (AcceptorLogonAction.class.getResource(ResourceBundle.getBundle(BUNDLE_NAME,Locale.getDefault()).getString("iconFile")));


	@Override
	public String getType() {
		return "FIXAcceptorLogon";
	}

	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final List<ActionParameter> parameters = new ArrayList<>();

		//Build the list of default parameters
		for (final AcceptorLogonOption option : AcceptorLogonOption.values()){
			if (AppearsByDefault.True.equals(option.getAppearsByDefault())){
				parameters.add(new ActionParameter(option.getName(),option.getDefaultValue(),option.getType()));
			}
		}

		return parameters;
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return AcceptorLogonActionEngine.class;
	}

	@Override
	public Icon getIcon() {
		return DISPLAY_ICON;
	}

	@Override
	public boolean getDefaultIsHit(){
		return true;
	}

	@Override
	public String getDescription() {
		return "Creates a FIX acceptor.\n\n"+ Arguments.getArgumentDescriptions(AcceptorLogonOption.values());
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getDisplayPath() {
		return DISPLAY_PATH;
	}

	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
		return Optional.absent();
	}

	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
		return Optional.absent();
	}
}
