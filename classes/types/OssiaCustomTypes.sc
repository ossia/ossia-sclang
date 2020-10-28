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

	*ossiaWidget
	{ | anOssiaParameter |

		var isCartesian = false, event;
		var width = anOssiaParameter.window.bounds.width;

		if (anOssiaParameter.unit.notNil)
		{
			if (anOssiaParameter.unit.string == "position.cart2D") { isCartesian = true };
		};

		if (isCartesian)
		{
			var specs = [ControlSpec(), ControlSpec()];

			event = { | param |
				{
					if (param.value != [param.widgets[0].value,
						param.widgets[1].value])
					{
						param.widgets[0].value_(param.value[0]);
						param.widgets[1].value_(param.value[1]);
					};

					if (param.value != [specs[0].map(param.widgets[2].x),
						specs[1].map(param.widgets[2].y)])
					{
						param.widgets[2].x_(specs[0].unmap(param.value[0]));
						param.widgets[2].y_(specs[1].unmap(param.value[1]));
					};
				}.defer;
			};

			anOssiaParameter.widgets = [
				EZNumber(
					parent: anOssiaParameter.window,
					bounds: (width - 57)@20,
					numberWidth: 45,
					label: anOssiaParameter.name,
					gap: 4@0),
				EZNumber(
					parent: anOssiaParameter.window,
					bounds: 45@20,
					gap:0@0)
			];

			anOssiaParameter.widgets[0].labelView.align_(\left);

			anOssiaParameter.widgets.do(
				{ | item, i |

					item.setColors(
						stringColor:anOssiaParameter.window.asView.palette.color('baseText', 'active'),
						numNormalColor:anOssiaParameter.window.asView.palette.color('windowText', 'active')
					);

					// set numberBoxes scroll step and colors
					item.numberView.maxDecimals_(3)
					.step_(0.001).scroll_step_(0.001);

					if (anOssiaParameter.domain.min.notNil)
					{
						item.controlSpec.minval_(anOssiaParameter.domain.min[i]);
						item.controlSpec.maxval_(anOssiaParameter.domain.max[i]);
						specs[i].minval_(anOssiaParameter.domain.min[i]);
						specs[i].maxval_(anOssiaParameter.domain.max[i]);
					};
				}
			);

			// set GUI action and valued after min and max are set
			{
				anOssiaParameter.widgets[0].action_({ | val |
					anOssiaParameter.value_(
						[
							val.value,
							anOssiaParameter.value[1]
						]
					)
				}).value_(anOssiaParameter.value[0]);

				anOssiaParameter.widgets[1].action_({ | val |
					anOssiaParameter.value_(
						[
							anOssiaParameter.value[0],
							val.value
						]
					)
				}).value_(anOssiaParameter.value[1]);

			}.defer;

			anOssiaParameter.widgets = anOssiaParameter.widgets ++
			Slider2D(
				parent: anOssiaParameter.window,
				bounds: (width - 6)@(width - 6)
			).x_(specs[0].unmap(anOssiaParameter.value[0])) // initial location of x
			.y_(specs[1].unmap(anOssiaParameter.value[1])) // initial location of y
			.action_({ | val |

				anOssiaParameter.value_(
					[
						specs[0].map(val.x),
						specs[1].map(val.y)
					]
				)
			}).onClose_({
				anOssiaParameter.removeDependant(event);
				anOssiaParameter.widgets = nil;
			});

			anOssiaParameter.widgets[2].focusColor_(
				anOssiaParameter.window.asView.palette.color('midlight', 'active'))
			.background_(
				anOssiaParameter.window.asView.palette.color('middark', 'active'));
		} {
			event = { | param |
				{
					if (param.value != param.widgets.value)
					{
						param.widgets.value_(param.value);
					};
				}.defer;
			};

			anOssiaParameter.widgets = EZRanger(
				parent: anOssiaParameter.window,
				bounds: (width - 6)@40,
				label: anOssiaParameter.name,
				layout: 'line2',
				gap:4@0).onClose_({
				anOssiaParameter.removeDependant(event);
				anOssiaParameter.widgets = nil;
			}).setColors(
				stringColor:anOssiaParameter.window.asView.palette.color('baseText', 'active'),
				sliderColor:anOssiaParameter.window.asView.palette.color('middark', 'active'),
				numNormalColor:anOssiaParameter.window.asView.palette.color('windowText', 'active')
			);

			anOssiaParameter.widgets.rangeSlider.focusColor_(
				anOssiaParameter.window.asView.palette.color('midlight', 'active'));

			anOssiaParameter.widgets.hiBox.maxDecimals_(3)
			.step_(0.001).scroll_step_(0.001);

			anOssiaParameter.widgets.loBox.maxDecimals_(3)
			.step_(0.001).scroll_step_(0.001);

			if (anOssiaParameter.domain.min.notNil)
			{
				anOssiaParameter.widgets.controlSpec.minval_(anOssiaParameter.domain.min[0]);
				anOssiaParameter.widgets.controlSpec.maxval_(anOssiaParameter.domain.max[1]);
			};

			// set GUI action & value after min and max are set
			{
				anOssiaParameter.widgets.action_({ | val | anOssiaParameter.value_(val.value) });
				anOssiaParameter.widgets.value_(anOssiaParameter.value);
			}.defer
		};

		anOssiaParameter.addDependant(event);
	}
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

	*ossiaWidget
	{ | anOssiaParameter |

		var event, width = anOssiaParameter.window.bounds.width;

		anOssiaParameter.widgets = [
			EZNumber(
				parent: anOssiaParameter.window,
				bounds: (width - 106)@20,
				numberWidth: 45,
				label: anOssiaParameter.name,
				labelWidth:100,
				gap:4@0),
			EZNumber(
				parent: anOssiaParameter.window,
				bounds: 45@20,
				gap:0@0),
			EZNumber(
				parent: anOssiaParameter.window,
				bounds: 45@20,
				gap:0@0);
		];

		anOssiaParameter.widgets[0].labelView.align_(\left);

		anOssiaParameter.widgets.do(
			{ | item, i |

				item.setColors(stringColor:OSSIA.palette.color('baseText', 'active'),
					numNormalColor:OSSIA.palette.color('windowText', 'active'));

				// set numberBoxes scroll step and colors
				item.numberView.maxDecimals_(3)
				.step_(0.001).scroll_step_(0.001);

				if(anOssiaParameter.domain.min.notNil)
				{
					item.controlSpec.minval_(anOssiaParameter.domain.min[i]);
					item.controlSpec.maxval_(anOssiaParameter.domain.max[i]);
				}
			}
		);

		// set GUI action and valued after min and max are set
		{
			anOssiaParameter.widgets[0].action_({ | val |
				anOssiaParameter.value_(
					[
						val.value,
						anOssiaParameter.value[1],
						anOssiaParameter.value[2]
					]
				)
			}).value_(anOssiaParameter.value[0]);

			anOssiaParameter.widgets[1].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						val.value,
						anOssiaParameter.value[2]
					]
				)
			}).value_(anOssiaParameter.value[1]);

			anOssiaParameter.widgets[2].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						anOssiaParameter.value[1],
						val.value
					]
				)
			}).value_(anOssiaParameter.value[2]);

		}.defer;

		if (anOssiaParameter.unit.notNil)
		{
			switch (anOssiaParameter.unit.string,
				"position.cart3D",
				{
					var specs = [ControlSpec(), ControlSpec(), ControlSpec()], sliders;

					if(anOssiaParameter.domain.min.notNil)
					{
						specs.do(
							{ | item, i |
								item.minval_(anOssiaParameter.domain.min[i]);
								item.maxval_(anOssiaParameter.domain.max[i]);
							}
						);
					};

					sliders = [
						Slider2D(
							parent: anOssiaParameter.window,
							bounds: (width - 32)@(width - 32)
						).x_(specs[0].unmap(anOssiaParameter.value[0])) // initial value of x
						.y_(specs[1].unmap(anOssiaParameter.value[1])) // initial value of y
						.action_(
							{ | val |

								anOssiaParameter.value_(
									[
										specs[0].map(val.x),
										specs[1].map(val.y),
										anOssiaParameter.value[2]
									]
								)
							}
						),
						Slider(
							parent: anOssiaParameter.window,
							bounds: 20@(width - 32))
						.orientation_(\vertical)
						.value_(specs[2].unmap(anOssiaParameter.value[2])) // initial value of z
						.action_(
							{ | val |

								anOssiaParameter.value_(
									[
										anOssiaParameter.value[0],
										anOssiaParameter.value[1],
										specs[2].map(val.value)
									]
								)
							}
						)
					];

					sliders.do(
						{ | item |

							item.focusColor_(
								OSSIA.palette.color('midlight', 'active'))
							.background_(
								OSSIA.palette.color('middark', 'active'));
						}
					);

					anOssiaParameter.widgets = anOssiaParameter.widgets ++ sliders;

					event = { | param |
						{
							if (param.value != [param.widgets[0].value,
								param.widgets[1].value,
								param.widgets[2].value])
							{
								param.widgets[0].value_(param.value[0]);
								param.widgets[1].value_(param.value[1]);
								param.widgets[2].value_(param.value[2]);
							};

							if (param.value != [specs[0].map(param.widgets[3].x),
								specs[1].map(param.widgets[3].y),
								specs[2].map(param.widgets[4].value)])
							{
								param.widgets[3].x_(specs[0].unmap(param.value[0]));
								param.widgets[3].y_(specs[1].unmap(param.value[1]));
								param.widgets[4].value_(specs[2].unmap(param.value[2]));
							};
						}.defer;
					};
				},
				"orientation.euler",
				{
					var specs = [ControlSpec(), ControlSpec(), ControlSpec()], widgets;

					if(anOssiaParameter.domain.min.notNil)
					{
						specs.do(
							{ | item, i |
								item.minval_(anOssiaParameter.domain.min[i]);
								item.maxval_(anOssiaParameter.domain.max[i]);
							}
						);
					};

					widgets = [
						Knob(
							parent: anOssiaParameter.window,
							bounds: (width - 32)@(width - 32)
						).value_(specs[0].unmap(anOssiaParameter.value[0])) // initial value of roll
						.centered_(true)
						.action_(
							{ | val |

								anOssiaParameter.value_(
									[
										specs[0].map(1 - val.value),
										anOssiaParameter.value[1],
										anOssiaParameter.value[2]
									]
								)
							}
						),
						Slider(
							parent: anOssiaParameter.window,
							bounds: 20@(width - 32))
						.orientation_(\vertical)
						.value_(specs[2].unmap(anOssiaParameter.value[2])) // initial value of pitch
						.action_(
							{ | val |

								anOssiaParameter.value_(
									[
										anOssiaParameter.value[0],
										specs[1].map(1 - val.value),
										anOssiaParameter.value[2]
									]
								)
							}
						),
						Slider(
							parent: anOssiaParameter.window,
							bounds: (width -6)@20)
						.orientation_(\horizontal)
						.value_(specs[2].unmap(anOssiaParameter.value[2])) // initial value of yaw
						.action_(
							{ | val |

								anOssiaParameter.value_(
									[
										anOssiaParameter.value[0],
										anOssiaParameter.value[1],
										specs[2].map(1 - val.value)
									]
								)
							}
						)
					];

					widgets.do(
						{ | item |

							item.focusColor_(
								OSSIA.palette.color('midlight', 'active'))
							.background_(
								OSSIA.palette.color('middark', 'active'));
						}
					);

					widgets[0].color_(
						[
							widgets[0].color[0],
							OSSIA.palette.color('light', 'active'),
							widgets[0].color[2],
							widgets[0].color[3]
						]
					);

					anOssiaParameter.widgets = anOssiaParameter.widgets ++ widgets;

					event = { | param |
						{
							if (param.value != [param.widgets[0].value,
								param.widgets[1].value,
								param.widgets[2].value])
							{
								param.widgets[0].value_(param.value[0]);
								param.widgets[1].value_(param.value[1]);
								param.widgets[2].value_(param.value[2]);
							};

							if (param.value != [specs[0].map(1 - param.widgets[3].value),
								specs[1].map(1 - param.widgets[4].value),
								specs[2].map(1 - param.widgets[5].value)])
							{
								param.widgets[3].value_(1 - specs[0].unmap(param.value[0]));
								param.widgets[4].value_(1 - specs[1].unmap(param.value[1]));
								param.widgets[5].value_(1 - specs[2].unmap(param.value[2]));
							};
						}.defer;
					};
				},
				{
					event = { | param |
						{
							if (param.value != [param.widgets[0].value,
								param.widgets[1].value,
								param.widgets[2].value])
							{
								param.widgets[0].value_(param.value[0]);
								param.widgets[1].value_(param.value[1]);
								param.widgets[2].value_(param.value[2]);
							}
						}.defer;
					};
				}
			);
		};

		anOssiaParameter.addDependant(event);

		// always add the onClose function to the last ellement
		// reserving widgets = nil at the ed of the loop
		anOssiaParameter.widgets[anOssiaParameter.widgets.size - 1]
		.onClose_({
			anOssiaParameter.removeDependant(event);
			anOssiaParameter.widgets = nil;
		})
	}
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

	*ossiaWidget
	{ | anOssiaParameter |

		var event, width = anOssiaParameter.window.bounds.width;

		event = { | param |
			{
				if (param.value != [param.widgets[0].value,
					param.widgets[1].value,
					param.widgets[2].value,
					param.widgets[3].value])
				{
					param.widgets[0].value_(param.value[0]);
					param.widgets[1].value_(param.value[1]);
					param.widgets[2].value_(param.value[2]);
					param.widgets[3].value_(param.value[3]);
				}
			}.defer;
		};

		anOssiaParameter.widgets = [
			EZNumber(
				parent: anOssiaParameter.window,
				bounds: (width - 155)@20,
				numberWidth: 45,
				label: anOssiaParameter.name,
				labelWidth:100,
				gap:4@0),
			EZNumber(
				parent: anOssiaParameter.window,
				bounds: 45@20,
				gap:0@0),
			EZNumber(
				parent: anOssiaParameter.window,
				bounds: 45@20,
				gap:0@0),
			EZNumber(
				parent: anOssiaParameter.window,
				bounds: 45@20,
				gap:0@0).onClose_({
				anOssiaParameter.removeDependant(event);
				anOssiaParameter.widgets = nil;
			});
		];

		anOssiaParameter.widgets[0].labelView.align_(\left);

		anOssiaParameter.widgets.do(
			{ | item, i |

				item.setColors(stringColor:OSSIA.palette.color('baseText', 'active'),
					numNormalColor:OSSIA.palette.color('windowText', 'active'));

				// set numberBoxes scroll step and colors
				item.numberView.maxDecimals_(3)
				.step_(0.001).scroll_step_(0.001);

				if(anOssiaParameter.domain.min.notNil)
				{
					item.controlSpec.minval_(anOssiaParameter.domain.min[i]);
					item.controlSpec.maxval_(anOssiaParameter.domain.max[i]);
				}
			}
		);

		// set GUI action and valued after min and max are set
		{
			anOssiaParameter.widgets[0].action_({ | val |
				anOssiaParameter.value_(
					[
						val.value,
						anOssiaParameter.value[1],
						anOssiaParameter.value[2],
						anOssiaParameter.value[3]
					]
				)
			}).value_(anOssiaParameter.value[0]);

			anOssiaParameter.widgets[1].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						val.value,
						anOssiaParameter.value[2],
						anOssiaParameter.value[3]
					]
				)
			}).value_(anOssiaParameter.value[1]);

			anOssiaParameter.widgets[2].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						anOssiaParameter.value[1],
						val.value,
						anOssiaParameter.value[3]
					]
				)
			}).value_(anOssiaParameter.value[2]);

			anOssiaParameter.widgets[3].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						anOssiaParameter.value[1],
						anOssiaParameter.value[2],
						val.value
					]
				)
			}).value_(anOssiaParameter.value[3]);

		}.defer;
	}
}