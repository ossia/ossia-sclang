/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

OSSIA_Base // base classe for OSSIA_Device and OSSIA_Node
{
	var <name;
	var <path;
	var <children;

	// overwriten by OSSIA_Device and OSSIA_Node
	parent { }

	addChild { | anOssiaNode | children = children.add(anOssiaNode) }

	explore
	{ | with_attributes = false, parameters_only = false |

		if (parameters_only)
		{
			^this.prParamExplore;
		} {
			^this.prNodeExplore;
		}
	}

	tree
	{ |with_attributes = false, parameters_only = false|

		var exp = this.explore(with_attributes, parameters_only);

		exp.do({ | item |

			var str = "";

			item.do({ | subitem | str = str + subitem });
			str.postln;
		});

	}

	snapshot
	{ |... exclude|

		var exp = this.explore(false, true);
		var res = [];

		exp.do({ | item |

			var unique = item[0].split($/).last ++ "_" ++ item[1];

			res = res.add(unique.asSymbol);
			res = res.add(item[2]);
		});

		if(exclude.notEmpty)
		{
			exclude.do({ | item |

				var index = res.indexOf(item.sym);

				2.do({ res.removeAt(index) });
			});
		};

		^res
	}

	find
	{ | nodePath |

		var array, string = this.prCheckPath(nodePath);

		array = this.prPath2Array(string);

		^this.findFromArray(array);
	}

	findFromArray
	{ | namesArray |

		if (namesArray.size == 1)
		{
			children.do({ | item |

				if (item.name == namesArray[0])
				{ ^item; }
			})
		} {
			children.do({ | item |

				if (item.name == namesArray[0])
				{
					namesArray.removeAt(0);
					^item.findFromArray(namesArray);
				}
			})
		};

		^namesArray.addFirst(this);
	}

	//-------------------------------------------//
	//              PRIVATE METHODS              //
	//-------------------------------------------//

	prHandlePath
	{ | nm |

		var curent_path = this.parent.find(nm);

		if (curent_path.class == Array)
		{
			var lasName, previousNode = curent_path[0];

			curent_path.removeAt(0);

			// preserve the last name in the string and remove it from the path
			lasName = curent_path.removeAt(curent_path.size - 1);

			curent_path.do({ | item, i |
				previousNode = OSSIA_Node(previousNode, item);
			});

			this.prCreateOrIncrement(previousNode, lasName);
		} {
			this.prCreateOrIncrement(this.parent, curent_path);
		}
	}

	prCheckPath
	{ | roughPath |

		var string;

		switch (roughPath.class,
			Symbol,
			{ string = roughPath.asString },
			String,
			{ string = roughPath },
			{ ^Error("nodePath must be of type String").throw }
		);

		^string;
	}

	prPath2Array
	{ | pathString |

		var splitedPath = pathString.split();

		// clean up first and laast $/ as they are not needed
		if (splitedPath.first == "") { splitedPath.removeAt(0) };
		if (splitedPath.last == "")
		{ splitedPath.removeAt(splitedPath.size - 1) };

		^splitedPath;
	}

	// overwriten by OSSIA_Device and OSSIA_Node
	prNodeExplore { }
	prParamExplore { }
	prCreateOrIncrement { }
}
