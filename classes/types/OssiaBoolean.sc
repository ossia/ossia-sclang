/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Boolean
{
	*ossiaWsWrite
	{ | anOssiaParameter, ws |

		ws.writeOsc(anOssiaParameter.path, anOssiaParameter.value);
	}

	*ossiaSendMsg
	{ | anOssiaParameter, addr |

		addr.sendMsg(
			anOssiaParameter.path,
			if (anOssiaParameter.value) { $T } { $F }
		);
	}

	*ossiaBounds { | mode | ^{ | value, domain | value.asBoolean } }

	*ossiaNaNFilter { | newVal, oldval | ^newVal }

	*ossiaJson { ^"\"F\"" }

	*ossiaDefaultValue { ^false }

	*ossiaWidget
	{ | anOssiaParameter |

		OSSIA.makeButtonGui(anOssiaParameter);

		// Boolean specific states, actions and initial value
		anOssiaParameter.widgets[1].states_(
			[
				[
					"false",
					anOssiaParameter.window.asView.palette.color('light', 'active'),
					anOssiaParameter.window.asView.palette.color('middark', 'active')
					],
				[
					"true",
					anOssiaParameter.window.asView.palette.color('middark', 'active'),
					anOssiaParameter.window.asView.palette.color('light', 'active')
				]
			]
		).action_({ | val | anOssiaParameter.value_(val.value) });

		{ anOssiaParameter.widgets[1].value_(anOssiaParameter.value); }.defer;
	}
}