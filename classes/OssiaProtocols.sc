/*
 *
 *
 *
 */

OSSIA_OSCProtocol
{
	var remoteAddr;
	var remotePort;
	var localPort;
	var device;
	var netAddr;
	var recivrers;

	*new { |remoteAddr, remotePort, localPort, device|
		^this.newCopyArgs(remoteAddr, remotePort, localPort, device).oscProtocolCtor;
	}

	oscProtocolCtor {
		netAddr = NetAddr(remoteAddr, remotePort);
		recivrers = Array.with(
			device.tree(
				parameters_only: true).do(this.instantiateParameter(_))
		);
	}

	push { |anOssiaParameter|
		netAddr.sendMsg(anOssiaParameter.path, anOssiaParameter.value);
	}

	instantiateParameter { |anOssiaParameter|
		var path = anOssiaParameter.path;

		recivrers.add(OSCdef(path.asSymbol,
			{ |msg|
				if (msg.size == 2) {
					anOssiaParameter.valueQuiet(msg[1]);
				} { anOssiaParameter.valueQuiet(msg.removeAt(0));
				};
			},
			path, recvPort: localPort));

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