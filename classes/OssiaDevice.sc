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

OSSIA_Device {

	classvar g_devices;
	var <protocol;
	var <name;
	var <path;
	var <children;
	var m_semaphore;

	*initClass {
		g_devices = [];
		ShutDown.add({this.ossia_dtor});
	}

	addChild { |anOssiaNode|
		children = children.add(anOssiaNode);
	}

	deviceCtor { |n|
		name = n;
		path = $/;
		children = [];
	}

	*new { |name|

		g_devices.do({ |dev|
			if(name == dev.name) { dev.free() };
		});

		^super.new.deviceCtor(name).stack_up();
	}

	stack_up { g_devices = g_devices.add(this); }

	*ossia_dtor {

		"OSSIA: cleanup...".postln;

		g_devices.do({ |dev|
			if (dev.protocol.notNil) { dev.protocol.free; };
			dev.free;
		});
	}

	instantiateParameter { |anOssiaParameter|
		if (protocol.notNil) { protocol.instantiateParameter(anOssiaParameter); };
	}

	freeParameter { |anOssiaParameter|
		if (protocol.notNil) { protocol.freeParameter(anOssiaParameter); };
	}

	updateParameter { |anOssiaParameter|
		protocol.push(anOssiaParameter);
	}

	tree { |with_attributes = false, parameters_only = false|
		if (parameters_only) {
			^this.paramExplore;
		} {
			^this.nodeExplore;
		}
	}

	nodeExplore {
		^[this, children.collect(_.nodeExplore)];
	}

	paramExplore {
		^[children.collect(_.paramExplore)].flat;
	}

	//-------------------------------------------//
	//               DEVICE CALLBACKS            //
	//-------------------------------------------//

	forkExpose { |method, vargs, callback|

		callback !? {
			m_semaphore = Semaphore(1);
			fork {
				m_semaphore.wait();
				this.exposeRedirect(method, vargs);
			};

			fork {
				if(callback.isKindOf(Function)) {
					callback.value();
				}
			};
		};

		callback ?? {
			this.exposeRedirect(method, vargs);
		};
	}

	exposeRedirect { |method, vargs|
		switch(method,
			'oscqs', { if (protocol.notNil) { protocol.free; };
				protocol = OSSIA_OSCQSProtocol(vargs[0], vargs[1], vargs[2], this)},
			// 'oscqm', { this.pyrOSCQM(vargs[0])},
			// 'minuit', { this.pyrMinuit(vargs[0], vargs[1], vargs[2])},
			'osc', { if (protocol.notNil) { protocol.free; };
				protocol = OSSIA_OSCProtocol(vargs[0], vargs[1], vargs[2], this)}
		);
	}

	//-------------------------------------------//
	//                NEW SHORTCUTS              //
	//-------------------------------------------//

	// only OSC is suported for now
	// *newOSCQueryServer { |name, osc_port = 1234, ws_port = 5678, callback|
	// 	^this.new(name).exposeOSCQueryServer(osc_port, ws_port, callback);
	// }
	//
	// *newOSCQueryMirror { |name, host_addr, callback|
	// 	^this.new(name).exposeOSCQueryMirror(host_addr, callback);
	// }
	//
	// *newMinuit { |name, remote_ip, remote_port, local_port, callback|
	// 	^this.new(name).exposeMinuit(remote_ip, remote_port, local_port, callback);
	// }

	newOSC { |name, remote_ip = "127.0.0.1",
		remote_port = 9997, local_port = 9996, callback|
		^this.exposeOSC(remote_ip, remote_port, local_port, callback);
	}

	//-------------------------------------------//
	//                   EXPOSE                  //
	//-------------------------------------------//

	// only OSC is suported for now
	// get { |addr|
	// 	^OSSIA_MirrorParameter(this, addr)
	// }
	//
	// exposeOSCQueryServer { |osc_port = 1234, ws_port = 5678, callback|
	// 	this.forkExpose('oscqs', [osc_port, ws_port], callback);
	// }
	//
	// exposeOSCQueryMirror { |host_addr, callback|
	// 	this.forkExpose('oscqm', [host_addr], callback);
	// }
	//
	// exposeMinuit { |remote_ip, remote_port, local_port, callback|
	// 	this.forkExpose('minuit', [remote_ip, remote_port, local_port], callback);
	// }

	exposeOSC { |remote_ip = "127.0.0.1", remote_port = 9997, local_port = 9996, callback|
		this.forkExpose('osc', [remote_ip, remote_port, local_port], callback);
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
	//
	// pyrOSCQS { |osc_port, ws_port|
	// 	_OSSIA_ExposeOSCQueryServer
	// 	^this.primitiveFailed
	// }
	//
	// pyrOSCQM { |host_addr|
	// 	_OSSIA_ExposeOSCQueryMirror
	// 	^this.primitiveFailed;
	// }
	//
	// pyrMinuit { |remote_ip, remote_port, local_port|
	// 	_OSSIA_ExposeMinuit
	// 	^this.primitiveFailed;
	// }
	//
	// pyrOSC { |remote_ip, remote_port, local_port|
	// 	_OSSIA_ExposeOSC
	// 	^this.primitiveFailed
	// }
	//
	// pyrDeviceCtor { |name|
	// 	_OSSIA_InstantiateDevice
	// 	^this.primitiveFailed
	// }
	//
	// pyrFree {
	// 	_OSSIA_FreeDevice
	// 	^this.primitiveFailed
	// }
	//
	// free {
	// 	g_devices.remove(this);
	// 	this.pyrFree();
	// }
	//
	// *tests {
	// 	_OSSIA_Tests
	// 	^this.primitiveFailed
	// }

}