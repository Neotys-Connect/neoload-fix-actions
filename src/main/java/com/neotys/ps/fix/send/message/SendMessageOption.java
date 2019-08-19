package com.neotys.ps.fix.send.message;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter.Type;

import static com.neotys.action.argument.DefaultArgumentValidator.NON_EMPTY;
import static com.neotys.action.argument.Option.AppearsByDefault.False;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

enum SendMessageOption implements Option {
    SessionName("SessionName",
                Required,
                True,
                TEXT,
                "myFixSession",
                "Name of the FIX session.\n\t",
                NON_EMPTY),
    MessagePath("MessagePath",
            Optional,
            True,
            TEXT,
            "${NL-CustomResources}/messages/messages.log",
            "Path of the message file.\n\t",
            NON_EMPTY),
    Message("Message",
            Optional,
            False,
            TEXT,
            "",
            "Message to send.\n\t",
            NON_EMPTY);

    private final String name;
    private final OptionalRequired optionalRequired;
    private final AppearsByDefault appearsByDefault;
    private final Type type;
    private final String defaultValue;
    private final String description;
    private final ArgumentValidator argumentValidator;

    SendMessageOption(final String name,
                      final OptionalRequired optionalRequired,
                      final AppearsByDefault appearsByDefault,
                      final Type type,
                      final String defaultValue,
                      final String description,
                      final ArgumentValidator argumentValidator) {
        this.name = name;
        this.optionalRequired = optionalRequired;
        this.appearsByDefault = appearsByDefault;
        this.type = type;
        this.defaultValue = defaultValue;
        this.description = description;
        this.argumentValidator = argumentValidator;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public OptionalRequired getOptionalRequired() {
        return optionalRequired;
    }

    @Override
    public AppearsByDefault getAppearsByDefault() {
        return appearsByDefault;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ArgumentValidator getArgumentValidator() {
        return argumentValidator;
    }
}
