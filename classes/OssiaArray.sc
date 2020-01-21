/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Array {

	*ossiaSendMsg {	|anOssiaParameter, addr|
		addr.sendRaw(([anOssiaParameter.path] ++ anOssiaParameter.value.asSymbol).asRawOSC);
	}

	*ossiaBounds { |mode|
		switch(mode,
			'free', {
				^{ |value, domain| value.asArray };
			},
			'clip', {
				^{ |value, domain| value.collect({ |item, i|
				item.clip(domain.min[i], domain.max[i]) }).asArray };
			},
			'low', {
				^{ |value, domain| value.collect({ |item, i|
				item.max(domain.min[i]) }).asArray };
			},
			'high', {
				^{ |value, domain| value.collect({ |item, i|
				item.min(domain.max[i]) }).asArray };
			},
			'wrap', {
				^{ |value, domain| value.collect({ |item, i|
				item.wrap(domain.min[i], domain.max[i]) }).asArray };
			},
			'fold', {
				^{ |value, domain| value.collect({ |item, i|
				item.fold(domain.min[i], domain.max[i]) }).asArray };
			}, {
				^{ |value, domain| domain[2].detect({ |item|
					item == value.asArray });
				};
		});
	}

	*ossiaDefaultValue { ^[]; }

	*ossiaNaNFilter { |newVal, oldval|
		^newVal;
	}

	*ossiaWidget { |anOssiaParameter|
		var widgets;

		widgets = EZText(anOssiaParameter.window, 392@20, anOssiaParameter.name,
			action:{ | val | anOssiaParameter.value_(val.value); },
			initVal: anOssiaParameter.value, labelWidth:100, gap:0@0).onClose_({
			anOssiaParameter.removeFromEvenGui_(anOssiaParameter.name.asSymbol); });

		anOssiaParameter.addToEvenGui_(
			name.asSymbol,
			{
				if (anOssiaParameter.value != widgets.value) {
					widgets.value_(anOssiaParameter.value);
				};
			};
		);
	}
}