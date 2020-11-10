/*
* This project is a fork of Pierre Cohard's ossia-supercollider
* https://github.com/OSSIA/ossia-supercollider.git
* Form his sclang files, the aim is to provide the same message structure
* specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
* and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
*/

//-------------------------------------------//
//               OSSIA_VECNF                 //
//-------------------------------------------//

OSSIA_FVector
{
	var <am_val, m_sz;

	*new { | sz ... values | ^super.new.init(sz, values) }

	init
	{
		| sz, v |

		v.do({ | item |

			if ((item.isFloat.not) && (item.isInteger.not))
			{ Error("OSSIA: Error! Arguments are not of Float type").throw };
		}
		);

		am_val = v;
		m_sz = sz;
	}

	at { | i | ^am_val[i] }
	put { | index, item | am_val[index] = item.asFloat }

	*ossiaSendMsg
	{
		| anOssiaParameter, addr |

		addr.sendRaw(([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	*ossiaWsWrite
	{
		| anOssiaParameter, ws |

		var msg = [anOssiaParameter.path] ++ anOssiaParameter.value;
		ws.writeOsc(*msg);
	}

	*ossiaBounds
	{
		| mode |

		switch (mode,
			'free',
			{ ^{ | value, domain | this.asOssiaVec(value) } },
			'clip',
			{
				^{ | value, domain |

					this.asOssiaVec(value.collect(
						{ | item, i |
							item.clip(domain.min[i], domain.max[i]);
						}
					))
				};
			},
			'low',
			{
				^{ | value, domain |

					this.asOssiaVec(value.collect(
						{ | item, i |
							item.max(domain.min[i])
						}
					))
				};
			},
			'high',
			{
				^{ | value, domain |

					this.asOssiaVec(value.collect(
						{ | item, i |
							item.min(domain.max[i])
						}
					))
				};
			},
			'wrap',
			{
				^{ | value, domain |

					this.asOssiaVec(value.collect(
						{ | item, i |
							item.wrap(domain.min[i], domain.max[i]);
						}
					))
				};
			},
			'fold',
			{
				^{ | value, domain |

					this.asOssiaVec(value.collect(
						{ | item, i |
							item.fold(domain.min[i], domain.max[i]);
						}
					))
				};
			},
			{
				^{ | value, domain |

					domain[2].detect(
						{ | item |
							item == this.asOssiaVec(value)
						}
					);
				};
			}
		);
	}
}

OSSIA_vec2f : OSSIA_FVector
{
	*new { | v1 = 0.0, v2 = 0.0 | ^super.new(2, v1.asFloat, v2.asFloat) }

	*asOssiaVec { | anArray | ^[anArray[0].asFloat, anArray[1].asFloat] }

	*ossiaDefaultValue { ^[0.0, 0.0] }

	*ossiaNaNFilter
	{ | newVal, oldval |

		^[
			if (newVal[0].isNil) { newVal[0] }
			{ if (newVal[0].isNaN) { oldval[0] } { newVal[0] } },
			if (newVal[1].isNil) { newVal[1] }
			{ if (newVal[1].isNaN) { oldval[1] } { newVal[1] } }
		];
	}

	*ossiaJson { ^"\"ff\"" }
}

OSSIA_vec3f : OSSIA_FVector
{
	*new
	{ | v1 = 0.0, v2 = 0.0, v3 = 0.0 |

		^super.new(3, v1.asFloat, v2.asFloat, v3.asFloat);
	}

	*asOssiaVec
	{ | anArray |

		^[anArray[0].asFloat, anArray[1].asFloat, anArray[2].asFloat];
	}

	*ossiaDefaultValue { ^[0.0, 0.0, 0.0] }

	*ossiaNaNFilter
	{ | newVal, oldval |

		^[
			if (newVal[0].isNil) { newVal[0] }
			{ if (newVal[0].isNaN) { oldval[0] } { newVal[0] } },
			if (newVal[1].isNil) { newVal[1] }
			{ if (newVal[1].isNaN) { oldval[1] } { newVal[1] } },
			if (newVal[2].isNil) { newVal[2] }
			{ if (newVal[2].isNaN) { oldval[2] } { newVal[2] } }
		];
	}

	*ossiaJson { ^"\"fff\"" }
}

OSSIA_vec4f : OSSIA_FVector
{
	*new
	{ | v1 = 0.0, v2 = 0.0, v3 = 0.0, v4 = 0.0 |

		^super.new(4, v1.asFloat, v2.asFloat, v3.asFloat, v4.asFloat);
	}

	*asOssiaVec
	{ | anArray |

		^[anArray[0].asFloat, anArray[1].asFloat,  anArray[2].asFloat,  anArray[3].asFloat];
	}

	*ossiaDefaultValue { ^[0.0, 0.0, 0.0, 0.0] }

	*ossiaNaNFilter
	{ | newVal, oldval |

		^[
			if (newVal[0].isNil) { newVal[0] }
			{ if (newVal[0].isNaN) { oldval[0] } { newVal[0] }},
			if (newVal[1].isNil) { newVal[1] }
			{ if (newVal[1].isNaN) { oldval[1] } { newVal[1] }},
			if (newVal[2].isNil) { newVal[2] }
			{ if (newVal[2].isNaN) { oldval[2] } { newVal[2] }},
			if (newVal[3].isNil) { newVal[3] }
			{ if (newVal[3].isNaN) { oldval[3] } { newVal[3] }}
		];
	}

	*ossiaJson { ^"\"ffff\"" }
}