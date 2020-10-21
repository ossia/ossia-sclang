/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Impulse {

	*ossiaWsWrite
	{
		| anOssiaParameter, ws |

		ws.writeOsc(anOssiaParameter.path);
	}

	*ossiaSendMsg
	{
		| anOssiaParameter, addr |

		addr.sendMsg(anOssiaParameter.path);
	}

	*ossiaBounds { | mode | ^"null" }

	*ossiaDefaultValue { ^"null" }

	*ossiaNaNFilter { | newVal, oldval | ^newVal }

	*ossiaJson { ^"\"I\"" }

	*ossiaWidget
	{
		| anOssiaParameter |

		if (anOssiaParameter.domain.values == [])
		{
			OSSIA.makeButtonGui(anOssiaParameter);

			// Impulse specific states and action
			anOssiaParameter.widgets.states_([
				["Pulse"]
			]).action_({ | val | anOssiaParameter.value_() });
		} {
			OSSIA.makeDropDownGui(anOssiaParameter);
		};
	}
}