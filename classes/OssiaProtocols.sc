/*
 * This project is a fork of Pierre Cohard's ossia-supercollider
 * https://github.com/OSSIA/ossia-supercollider.git
 * Form his sclang files, the aim is to provide the same message structure
 * specific to the OSSIA library (https://github.com/OSSIA/libossia.git)
 * and the interactive sequencer OSSIA/score (https://github.com/OSSIA/score.git)
 */

	//-------------------------------------------//
	//                   OSC                     //
	//-------------------------------------------//

OSSIA_OSCProtocol
{
	var remoteAddr;
	var remotePort;
	var localPort;
	var device;
	var netAddr;

	*new
	{ | remoteAddr, remotePort, localPort, device |

		^this.newCopyArgs(remoteAddr, remotePort, localPort, device).oscProtocolCtor;
	}

	oscProtocolCtor
	{
		netAddr = NetAddr(remoteAddr, remotePort);
		device.explore(parameters_only: true).do(this.instantiateParameter(_));
	}

	push
	{ | anOssiaParameter |

		anOssiaParameter.type.ossiaSendMsg(anOssiaParameter, netAddr);
	}

	instantiateNode { | anOssiaNode | }

	instantiateParameter
	{ | anOssiaParameter |

		var path = anOssiaParameter.path;

		OSCdef(path,
			{ | msg |
				if (msg.size == 2)
				{
					anOssiaParameter.valueQuiet(msg[1]);
				} {
					msg.removeAt(0);
					anOssiaParameter.valueQuiet(msg);
				};
			},
			path, recvPort: localPort);

		this.push(anOssiaParameter);
	}

	freeNode { | nodePath | }

	freeParameter
	{ | anOssiaParameter |

		OSCdef(anOssiaParameter.path).free;
	}

	free
	{
		device.explore(parameters_only: true).do(this.freeParameter(_));
		^super.free;
	}
}

	//-------------------------------------------//
	//              OSCQUERY SERVER              //
	//-------------------------------------------//

OSSIA_OSCQSProtocol
{
	var name;
	var osc_port;
	var ws_port;
	var device;
	var <netAddr;
	var ws_server;
	var zeroconf_service;
	var json_tree;
	var <dictionary;

	classvar host_info;

	*initClass
	{
		host_info = ",\"OSC_TRANSPORT\":\"UDP\""
		++",\"EXTENSIONS\":"
		++"{"
		++"\"TYPE\":true"
		++",\"ACCESS\":true"
		++",\"VALUE\":true"
		++",\"RANGE\":true"
		++",\"TAGS\":true"
		++",\"CLIPMODE\":true"
		++",\"UNIT\":true"
		++",\"CRITICAL\":true"
		++",\"DESCRIPTION\":true"
		++",\"HTML\":true"
		++",\"OSC_STREAMING\":true"
		++",\"LISTEN\":true"
		++",\"ECHO\":true"
		++",\"PATH_CHANGED\":false"
		++",\"PATH_RENAMED\":false"
		++",\"PATH_ADDED\":true"
		++",\"PATH_REMOVED\":true"
		++"}"
		++"}";
	}

	*new
	{ | name, osc_port, ws_port, device |

		^this.newCopyArgs(name, osc_port, ws_port, device).oscQuerryProtocolCtor();
	}

	oscQuerryProtocolCtor
	{
		netAddr = [];
		ws_server = WebSocketServer(ws_port).granularity_(25);
		zeroconf_service = ZeroconfService(name, "_oscjson._tcp", ws_port);
		dictionary = IdentityDictionary.new;

		ws_server.onNewConnection = { | con |
			postln(format("[websocket-server] new connection from %:%", con.address, con.port));

			con.onTextMessageReceived = { | msg |
				var command = msg.parseYAML;

				postln(format("[websocket-server] new message from: %:%", con.address, con.port));
				postln(msg);

				switch (command["COMMAND"],
					"START_OSC_STREAMING",
					{
						netAddr = netAddr.add(
							NetAddr(con.address,
								command["DATA"]["LOCAL_SERVER_PORT"].asInteger)
						)
					},
					"LISTEN",
					{ dictionary.at(command["DATA"].asSymbol).listening_(true) },
					"IGNORE",
					{ dictionary.at(command["DATA"].asSymbol).listening_(false) }
				);
			};

			con.onOscMessageReceived = { | array |

				postln(format("[websocket-server] new osc message from: %:%", con.address, con.port));
				postln(array);

				dictionary.at(array[0].asSymbol).valueQuiet(array[1]);
			};
		};

		ws_server.onHttpRequestReceived = { | req |

			postln("[http-server] request received");
			postln(format("[http-server] uri: %", req.uri));
			postln(req.query);

			if (req.query == "VALUE")
			{
				var param = dictionary.at(req.uri.asSymbol);

				if ((param.type == String) || (param.type == Char))
					{
						req.replyJson("{\"VALUE\": \"" ++ param.value ++"\"}")
					} {
						req.replyJson("{\"VALUE\": " ++ param.value ++"}")
					};

				this.push(dictionary.at(req.uri.asSymbol));
				postln(format("[http-server] reply sent"));
			};

			if (req.uri == "/")
			{
				if (req.query == "HOST_INFO")
				{
					req.replyJson("{\"NAME\":\""++ name ++"\",\"OSC_PORT\":"++ osc_port ++ host_info);
				} {
					json_tree = "{\"FULL_PATH\":\"/\",\"CONTENTS\":"++ OSSIA.stringify(device.children) ++"}";
					req.replyJson(json_tree);
				}
			}
		};

		ws_server.onDisconnection = { | con |
			postln(format("[websocket-server] client %:% disconnected", con.address, con.port));
		};

		device.explore().flat.do(_.instantiate());
	}

	push
	{ | anOssiaParameter |

		ws_server.numConnections.do({ | iter |

			if (anOssiaParameter.critical)
			{
				ws_server[iter].port.postln;
				anOssiaParameter.type.ossiaWsWrite(anOssiaParameter, ws_server[iter]);
			} {
				anOssiaParameter.type.ossiaSendMsg(anOssiaParameter, netAddr[iter]);
			}
		});
	}

	instantiateNode
	{ | nodePath |

		ws_server.numConnections.do({ | iter |

			this.prAddPath(nodePath, iter);
		});
	}

	instantiateParameter
	{ | anOssiaParameter |

		dictionary.put(anOssiaParameter.path.asSymbol, anOssiaParameter);

		ws_server.numConnections.do({ | iter |

			this.prAddPath(anOssiaParameter.path, iter);
			this.updateAtributes(anOssiaParameter, iter);
		});

		if (anOssiaParameter.critical.not)
		{
			var path = anOssiaParameter.path;

			OSCdef(path,
				{ | msg |
					if (msg.size == 2)
					{
						anOssiaParameter.valueQuiet(msg[1]);
					} {
						msg.removeAt(0);
						anOssiaParameter.valueQuiet(msg);
					};
				},
				path, recvPort: osc_port);
		};
	}

	freeNode
	{ | nodePath |

		ws_server.numConnections.do({ | iter |

			this.prRemovePath(nodePath, iter);
		});
	}

	freeParameter
	{ | anOssiaParameter |

		ws_server.numConnections.do({ | iter |

			this.prRemovePath(anOssiaParameter.path, iter);
		});

		if (anOssiaParameter.critical.not) { OSCdef(anOssiaParameter.path).free }
	}

	updateAtributes
	{ | anOssiaParameter, index |

		ws_server[index].writeText("{\"COMMAND\":\"ATTRIBUTES_CHANGED\",\"DATA\":{\"FULL_PATH\":\""
			++ anOssiaParameter.path ++ "\""
			++ anOssiaParameter.jsonParams()
			++ "}}"
		);
	}

	prAddPath
	{ | path, index |

		ws_server[index].writeText("{\"COMMAND\":\"PATH_ADDED\",\"DATA\":\"" ++ path ++"\"}");
	}

	prRemovePath
	{ | path, index |

		ws_server[index].writeText("{\"COMMAND\":\"PATH_REMOVED\",\"DATA\":\"" ++ path ++"\"}");
	}
}

	//-------------------------------------------//
	//              OSCQUERY MIRROR              //
	//-------------------------------------------//

OSSIA_OSCQMProtocol
{
	var host_addr;
	var device;
	var netAddr;
	var ws_client;
	var zeroconf_service;
	var <dictionary;

	*new
	{ | host_addr, device |

		^this.newCopyArgs(host_addr, device).oscQuerryProtocolCtor;
	}

	oscQuerryProtocolCtor
	{
		ws_client = WebSocketClient();
		dictionary = IdentityDictionary.new;

		zeroconf_service = ZeroconfBrowser("_oscjson._tcp", host_addr,
			{ | target |

				postln(format("[zeroconf] target resolved: % (%) at address: %:%",
					target.name, target.domain, target.address, target.port));

				target.onDisconnected = {
					postln(format("[zeroconf] target % is now offline", target.name));
					this.free;
				};

				ws_client.connect(target.address, target.port);
				netAddr = NetAddr(target.address.asString);
			}
		);

		ws_client.onConnected = {
			// client connection callback
			postln("[websocket-client] connected!");

			// requests root and host_info (for oscquery)
			ws_client.request("/?HOST_INFO");
			ws_client.request("/");
		};

		ws_client.onTextMessageReceived = { | msg |
			var command = msg.parseYAML;
			postln(format("[websocket-client] new message from: %:%", msg));
			postln(msg);

			switch (command["COMMAND"],
				"LISTEN",
				{ dictionary.at(command["DATA"].asSymbol).listening_(true) },
				"IGNORE",
				{ dictionary.at(command["DATA"].asSymbol).listening_(false) }
			);
		};

		ws_client.onOscMessageReceived = { | array |
			//postln(format("[websocket-client] new osc message", array));

			var address = array[0].asSymbol;

			if (array.size == 2)
			{
				dictionary.at(address).do(_.valueQuiet(array[1]));
			} {
				array.removeAt(0);
				dictionary.at(address).do(_.valueQuiet(array));
			};
		};

		ws_client.onHttpReplyReceived = { | reply |
			var json = reply.body.parseYAML;
			postln(format("[http-client] reply from server for uri: %, %", reply.uri, reply.body));

			if (json["FULL_PATH"] == "/")
			{
				this.setupIdentityDict(json["CONTENTS"]);
				device.explore().flat.do(_.instantiate());
			};

			if (json["OSC_PORT"].notNil)
			{
				netAddr.port_(json["OSC_PORT"].asInteger);
			};
		};

		ws_client.onDisconnected = { | con |
			postln(format("[websocket-client] client %:% disconnected", con.address, con.port));
		};
	}

	push
	{ | anOssiaParameter |

		if (anOssiaParameter.critical)
		{
			ws_client.writeOsc(anOssiaParameter.path, anOssiaParameter.value);
		} {
			anOssiaParameter.type.ossiaSendMsg(anOssiaParameter, netAddr);
		};
	}

	setupIdentityDict
	{ | aDictionaryAray |

		aDictionaryAray.do({ | aDictionary |

			// only keep parameters
			if (aDictionary["VALUE"].notNil) { dictionary.put(aDictionary["FULL_PATH"].asSymbol, []); };
			if (aDictionary["CONTENTS"].notNil) { this.setupIdentityDict(aDictionary["CONTENTS"]); };
		});
	}

	instantiateNode { | anOssiaNode | }

	instantiateParameter
	{ | anOssiaParameter |

		var key = anOssiaParameter.path.asSymbol;

		if (dictionary.includesKey(key))
		{
			dictionary.put(key, dictionary.at(dictionary).add(anOssiaParameter));
		};
	}

	freeParameter
	{ | anOssiaNode |

		if (anOssiaNode.class == OSSIA_Parameter)
		{
			if (anOssiaNode.critical.not)
			{ OSCdef(anOssiaNode.path.asSymbol).free };
		};
	}

	freeNode { | nodePath | }

	free
	{
		device.explore().flat.do(this.freeParameter(_));
		^super.free;
	}
}