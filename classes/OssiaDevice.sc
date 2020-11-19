/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                  DEVICE                   //
	//-------------------------------------------//

OSSIA_Device : OSSIA_Base
{
	classvar <g_devices;

	var <protocol;
	var m_semaphore;

	parent { ^this } // compatibility with OSSIA_Node

	*initClass
	{
		g_devices = [];
		ShutDown.add({this.ossia_dtor});
	}

	deviceCtor
	{ | n |

		name = n;
		path = $/;
		children = [];
	}

	*new
	{ | name |

		g_devices.do({ |dev|
			if(name == dev.name) { dev.free() };
		});

		^super.new.deviceCtor(name).stack_up();
	}

	stack_up { g_devices = g_devices.add(this) }

	*ossia_dtor
	{
		"OSSIA: cleanup...".postln;

		g_devices.do({ | dev |
			if (dev.protocol.notNil) { dev.protocol.free };
			dev.free;
		});
	}

	instantiateNode
	{ | nodePath |

		if (protocol.notNil) { protocol.instantiateNode(nodePath) };
	}

	instantiateParameter
	{ | anOssiaParameter |

		if (protocol.notNil) { protocol.instantiateParameter(anOssiaParameter) };
	}

	freeNode
	{ | nodePath |

		if (protocol.notNil) { protocol.freeNode(nodePath) };
	}

	freeParameter
	{ | anOssiaParameter |

		if (protocol.notNil) { protocol.freeParameter(anOssiaParameter) };
	}

	updateParameter { | anOssiaParameter | protocol.push(anOssiaParameter) }

	get { | addr | ^OSSIA_MirrorParameter(this, addr) }

	//-------------------------------------------//
	//                NEW SHORTCUTS              //
	//-------------------------------------------//

	*newOSCQueryServer
	{ | name, osc_port = 1234, ws_port = 5678, callback |

		^this.new(name).exposeOSCQueryServer(osc_port, ws_port, callback);
	}

	*newOSCQueryMirror
	{ | name, host_addr, callback |

		^this.new(name).exposeOSCQueryMirror(host_addr, callback);
	}

	*newOSC
	{ | name, remote_ip = "127.0.0.1", remote_port = 9997, local_port = 9996, callback |

		^this.new(name).exposeOSC(remote_ip, remote_port, local_port, callback);
	}

	//-------------------------------------------//
	//                   EXPOSE                  //
	//-------------------------------------------//

	exposeOSCQueryServer
	{ | osc_port = 1234, ws_port = 5678, callback |

		this.prForkExpose('oscqs', [name, osc_port, ws_port], callback);
	}

	exposeOSCQueryMirror
	{ | host_addr, callback |

		this.prForkExpose('oscqm', [host_addr], callback);
	}

	exposeOSC
	{ | remote_ip = "127.0.0.1", remote_port = 9997, local_port = 9996, callback |

		this.prForkExpose('osc', [remote_ip, remote_port, local_port], callback);
	}

	//-------------------------------------------//
	//              PRIVATE METHODS              //
	//-------------------------------------------//

	prNodeExplore { ^[children.collect(_.prNodeExplore)] }

	prParamExplore { ^[children.collect(_.prParamExplore)].flat }

	prCreateFromPAth { }

	//-------------------------------------------//
	//               DEVICE CALLBACKS            //
	//-------------------------------------------//

	prForkExpose
	{ | method, vargs, callback |

		callback !? {
			m_semaphore = Semaphore(1);

			fork {
				m_semaphore.wait();
				this.prExposeRedirect(method, vargs);
			};

			fork {
				if (callback.isKindOf(Function))
				{ callback.value() }
			};
		};

		callback ?? { this.prExposeRedirect(method, vargs) };
	}

	prExposeRedirect
	{ | method, vargs |

		switch (method,
			'oscqs',
			{
				if (protocol.notNil) { protocol.free; };
				protocol = OSSIA_OSCQSProtocol(vargs[0], vargs[1], vargs[2], this)
			},
			'oscqm',
			{
				if (protocol.notNil) { protocol.free; };
				protocol = OSSIA_OSCQMProtocol(vargs[0], this)
			},
			'osc',
			{
				if (protocol.notNil) { protocol.free; };
				protocol = OSSIA_OSCProtocol(vargs[0], vargs[1], vargs[2], this)
			}
		);
	}

	//-------------------------------------------//
	//     PRIMITIVE CALLS & METHODS (TOREDO)    //
	//-------------------------------------------//
	//
	// *format_ws { |zconf_array|
	// 	^format("ws://%:%", zconf_array[1], zconf_array[2]);
	// }
	//
	// *newFromZeroConf { |name, zconf_target_array, callback|
	// 	^OSSIA_Device(name).exposeOSCQueryMirror(
	// 	OSSIA_Device.format_ws(zconf_target_array), callback);
	// }
	// *net_explore {
	//  _OSSIA_ZeroConfExplore
	//  ^this.primitiveFailed
	// }
}
