/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

+ Float
{
	*ossiaWsWrite
	{ | anOssiaParameter, ws |

		ws.writeOsc(anOssiaParameter.path, anOssiaParameter.value);
	}

	*ossiaSendMsg
	{ | anOssiaParameter, addr |

		addr.sendRaw(([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	*ossiaBounds
	{ | mode |

		switch (mode,
			'free',
			{ ^{ | value, domain | value.asFloat } },
			'clip',
			{ ^{ | value, domain | value.clip(domain.min, domain.max).asFloat } },
			'low',
			{ ^{ | value, domain | value.max(domain.min).asFloat } },
			'high',
			{ ^{ | value, domain | value.min(domain.max).asFloat } },
			'wrap',
			{ ^{ | value, domain | value.wrap(domain.min, domain.max).asFloat } },
			'fold',
			{ ^{ | value, domain | value.fold(domain.min, domain.max).asFloat } },
			'values',
			{
				^{ | value, domain |

					domain[2].detect(
						{ | item | item == value.asFloat };
					);
				};
			},
			{ ^Error("bounding mode not recognized").throw };
		);
	}

	*ossiaDefaultValue { ^0.0 }

	*ossiaNaNFilter
	{ | newVal, oldval |

		if (newVal.isNil) { ^newVal }
		{ if (newVal.isNaN) { ^oldval } { ^newVal } };
	}

	*ossiaJson { ^"\"f\"" }
}
