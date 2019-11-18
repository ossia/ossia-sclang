/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                 PROTOCOLS                 //
	//-------------------------------------------//

OSSIA_OSCProtocol
{
	var remoteAddr;
	var remotePort;
	var localPort;
	var device;
	var netAddr;

	*new { |remoteAddr, remotePort, localPort, device|
		^this.newCopyArgs(remoteAddr, remotePort, localPort, device).oscProtocolCtor;
	}

	oscProtocolCtor {
		netAddr = NetAddr(remoteAddr, remotePort);
		device.tree(parameters_only: true).do(this.instantiateParameter(_));
	}

	push { |anOssiaParameter|
		netAddr.sendRaw(
			([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	instantiateParameter { |anOssiaParameter|
		var path = anOssiaParameter.path;

		OSCdef(path.asSymbol,
			{ |msg|
				if (msg.size == 2) {
					anOssiaParameter.valueQuiet(msg[1]);
				} { msg.removeAt(0);
					anOssiaParameter.valueQuiet(msg);
				};
			},
			path, recvPort: localPort);

		this.push(anOssiaParameter);
	}

	freeParameter { |anOssiaParameter|
		OSCdef(anOssiaParameter.path.asSymbol).free;
	}

	free {
		device.tree(parameters_only: true).do(this.freeParameter(_));
		^super.free;
	}
}

OSSIA_OSCQSProtocol
{
	var name;
	var osc_port;
	var ws_port;
	var device;
	var netAddr;
	var remoteAddr;
	var ws_server;

	*new { |name, osc_port, ws_port, device|
		^this.newCopyArgs(name, osc_port, ws_port, device).oscQuerryProtocolCtor;
	}

	oscQuerryProtocolCtor {
		ws_server = WebSocketServer(6789, name, "_oscjson._tcp");
		netAddr = NetAddr(remoteAddr, 9999);
		device.tree(parameters_only: true).do(this.instantiateParameter(_));
	}

	push { |anOssiaParameter|
		netAddr.sendRaw(
			([anOssiaParameter.path] ++ anOssiaParameter.value).asRawOSC);
	}

	instantiateParameter { |anOssiaParameter|
		this.instantiateOSC(anOssiaParameter);
	}

	instantiateOSC { |anOssiaParameter|
		var path = anOssiaParameter.path;

		OSCdef(path.asSymbol,
			{ |msg|
				if (msg.size == 2) {
					anOssiaParameter.valueQuiet(msg[1]);
				} { msg.removeAt(0);
					anOssiaParameter.valueQuiet(msg);
				};
			},
			path, recvPort: osc_port);
	}

	freeParameter { |anOssiaParameter|
		this.freeOSC(anOssiaParameter);
	}

	freeOSC { |anOssiaParameter|
		OSCdef(anOssiaParameter.path.asSymbol).free;
	}

	free {
		device.tree(parameters_only: true).do(this.freeParameter(_));
		^super.free;
	}
}