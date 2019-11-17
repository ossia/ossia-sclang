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