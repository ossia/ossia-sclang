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
	{ | anOssiaParameter, win, layout |

		var widget, event;

		anOssiaParameter.addDependant(event);

		if (layout == \minimal)
		{
			event = { | param |
				{
					if (param.value != widget.value)
					{ widget.value_(param.value) };
				}.defer;
			};

			widget = CheckBox(win, 20@20);

			widget.action_({ | check | anOssiaParameter.value_(check.value) });

			{ widget.value_(anOssiaParameter.value) }.defer;

			widget.onClose_({ anOssiaParameter.removeDependant(event);
				anOssiaParameter.removeClosed();
			});
		} {
			event = { | param |
				{
					if (param.value != widget[1].value)
					{ widget[1].value_(param.value) };
				}.defer;
			};

			widget = OSSIA.makeButtonGui(anOssiaParameter, win);

			// Boolean specific states, actions and initial value
			widget[1].states_(
				[
					[
						"false",
						win.asView.palette.color('light', 'active'),
						win.asView.palette.color('middark', 'active')
					],
					[
						"true",
						win.asView.palette.color('middark', 'active'),
						win.asView.palette.color('light', 'active')
					]
				]
			).action_({ | val | anOssiaParameter.value_(val.value) });

			{ widget[1].value_(anOssiaParameter.value) }.defer;

			widget[1].onClose_({ anOssiaParameter.removeDependant(event);
				widget[0].remove;
				anOssiaParameter.removeClosed();
			});
		};

		anOssiaParameter.widgets = anOssiaParameter.widgets ++ widget;
	}
}