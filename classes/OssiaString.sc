/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ String {

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendMsg(anOssiaParameter.path, anOssiaParameter.value);
	}

	*ossiaBounds { |mode|
		if (mode == 'values') {
			^{ |value, domain| domain[2].detect({ |item|
				item == value.asString });
			};
		} {
			^{ |value, domain| value.asString };
		};
	}

	*ossiaDefaultValue { ^""; }

	*ossiaNaNFilter { |newVal, oldval|
		^newVal;
	}

	*ossiaJson { ^"\"s\""; }

	*ossiaWidget { |anOssiaParameter|
		var widgets, enventName = (anOssiaParameter.device.name ++ anOssiaParameter.path).asSymbol;
		// apend the device Name to diferentiate between multiple devices with identical parameters
		// ei. server and mirror

		widgets = EZText(anOssiaParameter.window, 392@20, anOssiaParameter.name,
			action:{ | val | anOssiaParameter.value_(val.value); },
			initVal: anOssiaParameter.value, labelWidth:100, gap:4@0).onClose_({
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