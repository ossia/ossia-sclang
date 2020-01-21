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

	*ossiaDefaultValue { ^false; }

	*ossiaWidget { |anOssiaParameter|
		var widgets;

		StaticText(anOssiaParameter.window, 100@20).string_(anOssiaParameter.name).align_(\right);

		widgets = Button(anOssiaParameter.window, 288@20).states_([
			["true", Color.black, Color.green()],
			["false", Color.white, Color.red()]
		]).action_({ | val | anOssiaParameter.value_(val.value); }).onClose_({
			anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); });

		anOssiaParameter.addToEvenGui_(
			anOssiaParameter.name.asSymbol,
			{
				if (anOssiaParameter.value != widgets.value) {
					widgets.value_(anOssiaParameter.value);
				};
			};
		);
	}
}