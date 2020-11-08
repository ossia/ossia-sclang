/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ String
{
	*ossiaWsWrite
	{ | anOssiaParameter, ws |

		ws.writeOsc(anOssiaParameter.path, anOssiaParameter.value);
	}

	*ossiaSendMsg
	{ | anOssiaParameter, addr |

		addr.sendMsg(anOssiaParameter.path, anOssiaParameter.value);
	}

	*ossiaBounds
	{ | mode |

		if (mode == 'values')
		{
			^{ | value, domain |

				domain[2].detect({ | item | item == value.asString });
			};
		} {
			^{ | value, domain | value.asString };
		};
	}

	*ossiaDefaultValue { ^"" }

	*ossiaNaNFilter { | newVal, oldval | ^newVal }

	*ossiaJson { ^"\"s\"" }

	*ossiaWidget
	{ | anOssiaParameter, win |

		var widget;

		if (anOssiaParameter.domain.values == [])
		{
			widget = OSSIA.makeTxtGui(anOssiaParameter, win);
		} {
			widget = OSSIA.makeDropDownGui(anOssiaParameter, win);
		};

		anOssiaParameter.widgets = anOssiaParameter.widgets.add(widget);
	}
}