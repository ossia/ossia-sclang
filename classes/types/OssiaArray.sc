/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

// As can be red at the end of the the OSC comunication helpfile
// Arrays are not suported by supercollider. comenting out for now


+ Array
{
	*ossiaWsWrite
	{ | anOssiaParameter, ws |

		var msg = [anOssiaParameter.path] ++ anOssiaParameter.value;
		ws.writeOsc(*msg);
	}

	*ossiaSendMsg
	{ | anOssiaParameter, addr |

		var msg = [anOssiaParameter.path] ++ anOssiaParameter.value;
		addr.sendMsg(*msg);
	}

	*ossiaBounds
	{ | mode |

		switch (mode,
			'free',
			{ ^{ | value, domain | value.asArray } },
			'clip',
			{
				^{ | value, domain |

					value.collect(
						{ | item, i | item.clip(domain.min[i], domain.max[i]) }
					).asArray;
				};
			},
			'low',
			{
				^{ | value, domain |

					value.collect(
						{ | item, i | item.max(domain.min[i]) }
					).asArray;
				};
			},
			'high',
			{
				^{ | value, domain |

					value.collect(
						{ | item, i | item.min(domain.max[i]) }
					).asArray;
				};
			},
			'wrap',
			{
				^{ | value, domain |

					value.collect(
						{ | item, i | item.wrap(domain.min[i], domain.max[i]) }
					).asArray ;
				};
			},
			'fold',
			{
				^{ | value, domain |

					value.collect(
						{ | item, i | item.fold(domain.min[i], domain.max[i]) }
					).asArray;
				};
			},
			{
				^{ | value, domain |

					domain[2].detect(
						{ | item | item == value.asArray }
					);
				};
			}
		);
	}

	*ossiaDefaultValue { ^[0, 0] }

	*ossiaNaNFilter { | newVal, oldval | ^newVal }

	*ossiaJson { ^"\"l\"" }
}