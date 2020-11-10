/*
* This project is a fork of Pierre Cohard's ossia-supercollider
* https://github.com/OSSIA/ossia-supercollider.git
* Form his sclang files, the aim is to provide the same message structure
* specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
* and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
*/

//-------------------------------------------//
//                    NODE                   //
//-------------------------------------------//

+ OSSIA_Node
{
	gui
	{ | parent_window, childrenDepth = 1, layout = \full |

		var win = this.prWindowIfNeeded(parent_window);

		this.prChildGui(childrenDepth, win, layout);

		^win;
	}

	closeGui
	{ | parent_window, childrenDepth = 1 |

		this.prCloseChildGui(childrenDepth, parent_window);
	}

	//-------------------------------------------//
	//              PRIVATE METHODS              //
	//-------------------------------------------//

	prWindowIfNeeded
	{ | win |

		if (win.isNil)
		{
			var window = Window(name).front; // resize later to the flow layout size
			window.asView.palette_(OSSIA.palette);
			window.asView.background_(OSSIA.palette.base);
			window.addFlowLayout;
			^window;
		} {
			^win;
		};
	}

	prChildGui
	{ | childrenDepth, win, layout |

		if (childrenDepth > 0)
		{
			children.do({ | item |
				item.gui(win, childrenDepth - 1, layout);
			});
		};
	}

	prCloseChildGui
	{ | childrenDepth, win |

		if (childrenDepth > 0)
		{
			children.do({ | item |
				item.closeGui(win, childrenDepth - 1);
			});
		};
	}
}

+ OSSIA_Parameter
{
	gui
	{ | parent_window, childrenDepth = 0, layout = \full |

		var win = this.prWindowIfNeeded(parent_window);

		widgets = [];

		type.ossiaWidget(this, win, layout);
		this.prChildGui(childrenDepth, win, layout);

		this.resizeLayout(win);

		^win;
	}

	closeGui
	{ | parent_window, childrenDepth = 0 |

		if (parent_window.notNil)
		{
			parent_window.asView.children.reverseDo({ | item |

				widgets.reverseDo({ | widget |

					if (item === widget) { item.remove }
				})
			});

			this.prCheckFlow(parent_window);
		} {
			widgets.do({ | item |

				var parent = item.parent;

				item.remove;

				this.prCheckFlow(parent);
			})
		};

		this.prCloseChildGui(childrenDepth, parent_window);
	}

	removeClosed
	{
		var closed = [];

		widgets.do({ | item, count |

			if (item.isClosed) { closed = closed.add(count) };
		});

		closed.reverseDo({ | i | widgets.removeAt(i) });
	}

	//-------------------------------------------//
	//              PRIVATE METHODS              //
	//-------------------------------------------//

	prCheckFlow
	{ | parent |

		parent.asView.decorator.reFlow(parent.asView);
		this.resizeLayout(parent);

		if (parent.asView.children == [])
		{ parent.close }
	}

	resizeLayout
	{ | aWindow |

		var deco = aWindow.asView.decorator;

		if ((deco.used.height - aWindow.bounds.height) != 2)
		{ //resize to flow layout
			aWindow.bounds_(aWindow.bounds.height_(deco.used.height + 2));
		};
	}
}