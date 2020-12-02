/*
* This project is a fork of Pierre Cohard's ossia-supercollider
* https://github.com/OSSIA/ossia-supercollider.git
* Form his sclang files, the aim is to provide the same message structure
* specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
* and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
*/

+ OSSIA_FVector
{
	*ez2View
	{ | widget |

		// add EZ widgets as compositeViews since this is how they are stored in the parent view's children
		^widget.collect({ | item |

			if (item.class.superclass == EZGui)
			{
				item.view;
			} {
				item;
			}
		})
	}
}

+ OSSIA_vec2f
{
	*ossiaWidget
	{ | anOssiaParameter, win, layout |

		var event, widget;

		if (layout == \minimal)
		{
			event = { | param |
				{
					if (param.value != [widget.value[0],
						widget.value[1]])
					{
						widget[0].value_(param.value[0]);
						widget[1].value_(param.value[1])
					};
				}.defer;
			};

			widget = [
				NumberBox(win, 45@20)
				.action_({ | num | anOssiaParameter.value_(
					[
						num.value,
						anOssiaParameter.value[1]
					]
				) })
				.maxDecimals_(3)
				.step_(0.001).scroll_step_(0.001)
				.background_(win.asView.palette.color('base', 'active'))
				.normalColor_(win.asView.palette.color('windowText', 'active')),
				NumberBox(win, 45@20)
				.action_({ | num | anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						num.value
					]
				) })
				.maxDecimals_(3)
				.step_(0.001).scroll_step_(0.001)
				.background_(win.asView.palette.color('base', 'active'))
				.normalColor_(win.asView.palette.color('windowText', 'active'))
				.onClose_({ anOssiaParameter.removeDependant(event);
					widget[0].remove;
					anOssiaParameter.removeClosed(win);
				})
			];

			if (anOssiaParameter.domain.min.notNil)
			{
				widget[0].clipLo_(anOssiaParameter.domain.min);
				widget[1].clipHi_(anOssiaParameter.domain.max);
				widget[0].clipLo_(anOssiaParameter.domain.min);
				widget[1].clipHi_(anOssiaParameter.domain.max);
			};
		} {
			var width = win.bounds.width, isCartesian = false;

			if (anOssiaParameter.unit.notNil)
			{
				if (anOssiaParameter.unit.string == "position.cart2D") { isCartesian = true };
			};

			if (isCartesian)
			{
				var specs = [ControlSpec(), ControlSpec()];

				event = { | param |
					{
						if (param.value != [widget[0].value,
							widget[1].value])
						{
							widget[0].value_(param.value[0]);
							widget[1].value_(param.value[1]);
						};

						if (param.value != [specs[0].map(widget[2].x),
							specs[1].map(widget[2].y)])
						{
							widget[2].x_(specs[0].unmap(param.value[0]));
							widget[2].y_(specs[1].unmap(param.value[1]));
						};
					}.defer;
				};

				widget = [
					EZNumber(
						parent: win,
						bounds: (width - 57)@20,
						numberWidth: 45,
						label: anOssiaParameter.name,
						gap: 4@0),
					EZNumber(
						parent: win,
						bounds: 45@20,
						gap:0@0)
				];

				widget[0].labelView.align_(\left);

				widget.do({ | item, i |

						item.setColors(
							stringColor: win.asView.palette.color('baseText', 'active'),
							numNormalColor: win.asView.palette.color('windowText', 'active')
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
					widget[0].action_({ | val |
						anOssiaParameter.value_(
							[
								val.value,
								anOssiaParameter.value[1]
							]
						)
					}).value_(anOssiaParameter.value[0]);

					widget[1].action_({ | val |
						anOssiaParameter.value_(
							[
								anOssiaParameter.value[0],
								val.value
							]
						)
					}).value_(anOssiaParameter.value[1]);

				}.defer;

				widget = widget ++
				Slider2D(
					parent: win,
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
				})
				.onClose_({ anOssiaParameter.removeDependant(event);
					widget[0].remove;
					widget[1].remove;
					anOssiaParameter.removeClosed(win);
				});

				widget[2].focusColor_(
					win.asView.palette.color('midlight', 'active'))
				.background_(
					win.asView.palette.color('middark', 'active'));
			} {
				event = { | param |
					{
						if (param.value != widget.value)
						{ widget.value_(param.value) }
					}.defer;
				};

				widget = EZRanger(
					parent: win,
					bounds: (width - 6)@40,
					label: anOssiaParameter.name,
					layout: 'line2',
					gap:4@0)
				.onClose_({ anOssiaParameter.removeDependant(event);
					anOssiaParameter.removeClosed(win);
				})
				.setColors(
					stringColor: win.asView.palette.color('baseText', 'active'),
					sliderColor: win.asView.palette.color('middark', 'active'),
					numNormalColor: win.asView.palette.color('windowText', 'active')
				);

				widget.rangeSlider.focusColor_(
					win.asView.palette.color('midlight', 'active'));

				widget.hiBox.maxDecimals_(3)
				.step_(0.001).scroll_step_(0.001);

				widget.loBox.maxDecimals_(3)
				.step_(0.001).scroll_step_(0.001);

				if (anOssiaParameter.domain.min.notNil)
				{
					widget.controlSpec.minval_(anOssiaParameter.domain.min[0]);
					widget.controlSpec.maxval_(anOssiaParameter.domain.max[1]);
				};

				// set GUI action & value after min and max are set
				{
					widget.action_({ | val | anOssiaParameter.value_(val.value) });
					widget.value_(anOssiaParameter.value);
				}.defer;
			};

		};

		anOssiaParameter.addDependant(event);

		anOssiaParameter.widgets = anOssiaParameter.widgets ++ this.ez2View([widget]);
	}
}

+ OSSIA_vec3f
{
	*ossiaWidget
	{ | anOssiaParameter, win, layout |

		var widget, event, width = win.bounds.width;

		if (layout == \minimal)
		{
			widget = EZNumber(
				parent: win,
				bounds: 45@20,
				gap:0@0)
		} {
			widget = EZNumber(
				parent: win,
				bounds: (width - 106)@20,
				numberWidth: 45,
				label: anOssiaParameter.name,
				labelWidth:100,
				gap:4@0);

			widget.labelView.align_(\left);
		};

		widget = [
			widget,
			EZNumber(
				parent: win,
				bounds: 45@20,
				gap:0@0),
			EZNumber(
				parent: win,
				bounds: 45@20,
				gap:0@0);
		];

		widget.do({ | item, i |

				item.setColors(stringColor: win.asView.palette.color('baseText', 'active'),
					numNormalColor: win.asView.palette.color('windowText', 'active'));

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
			widget[0].action_({ | val |
				anOssiaParameter.value_(
					[
						val.value,
						anOssiaParameter.value[1],
						anOssiaParameter.value[2]
					]
				)
			}).value_(anOssiaParameter.value[0]);

			widget[1].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						val.value,
						anOssiaParameter.value[2]
					]
				)
			}).value_(anOssiaParameter.value[1]);

			widget[2].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						anOssiaParameter.value[1],
						val.value
					]
				)
			}).value_(anOssiaParameter.value[2]);

		}.defer;

		if (anOssiaParameter.unit.notNil && (layout == \full))
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
							parent: win,
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
							parent: win,
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
						).onClose_({ anOssiaParameter.removeDependant(event);
							widget[0].remove;
							widget[1].remove;
							widget[2].remove;
							widget[3].remove;
							anOssiaParameter.removeClosed(win);
						})
					];

					sliders.do(
						{ | item |

							item.focusColor_(
								OSSIA.palette.color('midlight', 'active'))
							.background_(
								OSSIA.palette.color('middark', 'active'));
						}
					);

					widget = widget ++ sliders;

					event = { | param |
						{
							if (param.value != [widget[0].value,
								widget[1].value,
								widget[2].value])
							{
								widget[0].value_(param.value[0]);
								widget[1].value_(param.value[1]);
								widget[2].value_(param.value[2]);
							};

							if (param.value != [specs[0].map(widget[3].x),
								specs[1].map(widget[3].y),
								specs[2].map(widget[4].value)])
							{
								widget[3].x_(specs[0].unmap(param.value[0]));
								widget[3].y_(specs[1].unmap(param.value[1]));
								widget[4].value_(specs[2].unmap(param.value[2]));
							};
						}.defer;
					};
				},
				"orientation.euler",
				{
					var specs = [ControlSpec(), ControlSpec(), ControlSpec()], controls;

					if(anOssiaParameter.domain.min.notNil)
					{
						specs.do(
							{ | item, i |
								item.minval_(anOssiaParameter.domain.min[i]);
								item.maxval_(anOssiaParameter.domain.max[i]);
							}
						);
					};

					controls = [
						Knob(
							parent: win,
							bounds: (width - 32)@(width - 32)
						).value_(1 - specs[0].unmap(anOssiaParameter.value[0]))
						// initial value of roll
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
							parent: win,
							bounds: 20@(width - 32))
						.orientation_(\vertical)
						.value_(1 - specs[1].unmap(anOssiaParameter.value[1]))
						// initial value of pitch
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
							parent: win,
							bounds: (width -6)@20)
						.orientation_(\horizontal)
						.value_(1 - specs[2].unmap(anOssiaParameter.value[2]))
						// initial value of yaw
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
						).onClose_({ anOssiaParameter.removeDependant(event);
							widget[0].remove;
							widget[1].remove;
							widget[2].remove;
							widget[3].remove;
							widget[4].remove;
							anOssiaParameter.removeClosed(win);
						})
					];

					controls.do(
						{ | item |

							item.focusColor_(
								win.asView.palette.color('midlight', 'active'))
							.background_(
								win.asView.palette.color('middark', 'active'));
						}
					);

					controls[0].color_(
						[
							controls[0].color[0],
							win.asView.palette.color('light', 'active'),
							controls[0].color[2],
							controls[0].color[3]
						]
					);

					widget = widget ++ controls;

					event = { | param |
						{
							if (param.value != [widget[0].value,
								widget[1].value,
								widget[2].value])
							{
								widget[0].value_(param.value[0]);
								widget[1].value_(param.value[1]);
								widget[2].value_(param.value[2]);
							};

							if (param.value != [specs[0].map(1 - widget[3].value),
								specs[1].map(1 - widget[4].value),
								specs[2].map(1 - widget[5].value)])
							{
								widget[3].value_(1 - specs[0].unmap(param.value[0]));
								widget[4].value_(1 - specs[1].unmap(param.value[1]));
								widget[5].value_(1 - specs[2].unmap(param.value[2]));
							};
						}.defer;
					}
				}
			)
		} {
			event = { | param |
				{
					if (param.value != [widget[0].value,
						widget[1].value,
						widget[2].value])
					{
						widget[0].value_(param.value[0]);
						widget[1].value_(param.value[1]);
						widget[2].value_(param.value[2]);
					}
				}.defer;
			};

			widget[2].onClose_({ anOssiaParameter.removeDependant(event);
				widget[0].remove;
				widget[1].remove;
				anOssiaParameter.removeClosed(win);
			})
		};

		anOssiaParameter.addDependant(event);

		anOssiaParameter.widgets = anOssiaParameter.widgets ++ this.ez2View(widget);
	}
}

+ OSSIA_vec4f
{
	*ossiaWidget
	{ | anOssiaParameter, win, layout |

		var event, width = win.bounds.width, widget;

		event = { | param |
			{
				if (param.value != [widget[0].value,
					widget[1].value,
					widget[2].value,
					widget[3].value])
				{
					widget[0].value_(param.value[0]);
					widget[1].value_(param.value[1]);
					widget[2].value_(param.value[2]);
					widget[3].value_(param.value[3]);
				}
			}.defer;
		};

		if (layout == \minimal)
		{
			widget = EZNumber(
				parent: win,
				bounds: 45@20,
				gap:0@0)
		} {
			widget = EZNumber(
				parent: win,
				bounds: (width - 155)@20,
				numberWidth: 45,
				label: anOssiaParameter.name,
				labelWidth:100,
				gap:4@0);

			widget.labelView.align_(\left);
		};

		widget = [
			widget,
			EZNumber(
				parent: win,
				bounds: 45@20,
				gap:0@0),
			EZNumber(
				parent: win,
				bounds: 45@20,
				gap:0@0),
			EZNumber(
				parent: win,
				bounds: 45@20,
				gap:0@0)
			.onClose_({ anOssiaParameter.removeDependant(event);
				widget[0].remove;
				widget[1].remove;
				widget[2].remove;
				anOssiaParameter.removeClosed(win);
			})
		];

		widget.do(
			{ | item, i |

				item.setColors(stringColor: win.asView.palette.color('baseText', 'active'),
					numNormalColor: win.asView.palette.color('windowText', 'active'));

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
			widget[0].action_({ | val |
				anOssiaParameter.value_(
					[
						val.value,
						anOssiaParameter.value[1],
						anOssiaParameter.value[2],
						anOssiaParameter.value[3]
					]
				)
			}).value_(anOssiaParameter.value[0]);

			widget[1].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						val.value,
						anOssiaParameter.value[2],
						anOssiaParameter.value[3]
					]
				)
			}).value_(anOssiaParameter.value[1]);

			widget[2].action_({ | val |
				anOssiaParameter.value_(
					[
						anOssiaParameter.value[0],
						anOssiaParameter.value[1],
						val.value,
						anOssiaParameter.value[3]
					]
				)
			}).value_(anOssiaParameter.value[2]);

			widget[3].action_({ | val |
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

		// add EZ widgets as compositeViews since this is how they are stored in the parent view's children
		anOssiaParameter.widgets = anOssiaParameter.widgets ++ this.ez2View(widget);
	}
}