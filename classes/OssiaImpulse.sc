/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Impulse {

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendMsg(anOssiaParameter.path, $I);
	}

	*ossiaDefaultValue { ^nil; }

	*ossiaWidget { |anOssiaParameter|
		var widgets;

		StaticText(anOssiaParameter.window, 100@20).string_(anOssiaParameter.name).align_(\right);

		widgets = Button(anOssiaParameter.window, 288@20).states_([
			["Pulse"]]).action_({ | val | anOssiaParameter.value_(val); }).onClose_({
			anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); });

		if(anOssiaParameter.domain.min.notNil) {
			widgets.controlSpec.minval_(anOssiaParameter.domain.min);
			widgets.controlSpec.maxval_(anOssiaParameter.domain.max);
		};
	}
}