/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Boolean {

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendMsg(anOssiaParameter.path,
			if (anOssiaParameter.value) { $T } { $F });
	}

	*ossiaBounds { |mode|
		^{ |value, domain| value.asBoolean };
	}

	*ossiaNaNFilter { |newVal, oldval|
		^newVal;
	}

	*ossiaJson { ^"\"F\""; }

	*ossiaDefaultValue { ^false; }

	*ossiaWidget { |anOssiaParameter|
		var widgets, enventName = (anOssiaParameter.device.name ++ anOssiaParameter.path).asSymbol;
		// apend the device Name to diferentiate between multiple devices with identical parameters
		// ei. server and mirror

		StaticText(anOssiaParameter.window, 100@20).string_(anOssiaParameter.name).align_(\right);

		widgets = Button(anOssiaParameter.window, 288@20).states_([
			["false", Color.white, Color.red()],
			["true", Color.black, Color.green()]
		]).action_({ | val | anOssiaParameter.value_(val.value); }).onClose_({
			anOssiaParameter.removeFromEvenGui_(enventName); });

		anOssiaParameter.addToEvenGui_(
			enventName,
			{
				if (anOssiaParameter.value != widgets.value) {
					widgets.value_(anOssiaParameter.value);
				};
			};
		);
	}
}