/*
 * Messenger web client.
 *
 * Security notes:
 *  - Every value coming from the network is rendered with textContent / DOM APIs, never innerHTML.
 *  - The STOMP connection is authenticated (bearer token + customerId) and every chat subscription
 *    carries the chat security token, the server rejects anything else.
 */
var CreateProxy = function(config) {

	var DEVICE_TYPE_ID = "3C1F3EE3A445C1BA";
	var HOTEL_DEVICE_SCREEN = 767;
	var PAGE_SIZE = 100;
	var REQUEST_TIMEOUT = 50000;
	var MAX_RECONNECT_ATTEMPTS = 10;

	var elements = null;
	var websocket = null;
	var audio = null;

	var isGuest = null;            // "1" guest, "0" hotel
	var guestList = [];
	var activeGuest = null;        // guest room object of the open chat
	var unreadCounts = {};
	var settingMuteAllNotif = false;

	var authToken = null, customerId = null, customerName = null, hotelId = null, chatSecurityToken = null, session = null, receiver = null;
	var roomTypeCode = null, roomNumber = null, reservationNumber = null, hotelTimezone = null;

	var historySize = PAGE_SIZE, remainingMessagesCount = 0, loadingHistory = false;
	var connected = false, reconnectAttempts = 0, reconnectTimer = null, intentionalDisconnect = false;
	var outbox = [];               // messages typed while disconnected
	var sentMsgIds = {};

	/* ---------------------------------------------------------------- helpers */

	var isHotel = function() { return isGuest == 0; };

	var changeScreenState = function(labelTxt, isError, loadingVisible, panelVisible) {
		elements.msgLabel.textContent = labelTxt;
		elements.loadingDiv.classList.toggle('is-error', !!isError);
		elements.loadingSpinner.hidden = !!isError || !loadingVisible;
		elements.loadingDiv.hidden = !loadingVisible && !isError;
		elements.msgPanel.hidden = !panelVisible;
	};

	var showFatal = function(message) {
		changeScreenState(message || 'Error, please refresh the page.', true, true, false);
	};

	var toastTimer = null;
	var showToast = function(message) {
		elements.toast.textContent = message;
		elements.toast.hidden = false;
		clearTimeout(toastTimer);
		toastTimer = setTimeout(function() { elements.toast.hidden = true; }, 5000);
	};

	var setConnectionState = function(state, label) {
		elements.connectionStatus.dataset.state = state;
		elements.connectionStatus.textContent = label;
	};

	var el = function(tag, className, text) {
		var node = document.createElement(tag);
		if (className) node.className = className;
		if (text != null) node.textContent = text;
		return node;
	};

	var icon = function(name) {
		var i = el('i', 'fa ' + name);
		i.setAttribute('aria-hidden', 'true');
		return i;
	};

	var initials = function(name) {
		var parts = String(name || '?').trim().split(/[\s._@-]+/).filter(Boolean);
		var value = parts.length > 1 ? parts[0].charAt(0) + parts[1].charAt(0) : String(name || '?').substring(0, 2);
		return value.toUpperCase();
	};

	var avatarHue = function(name) {
		var hash = 0, str = String(name || '');
		for (var i = 0; i < str.length; i++) hash = (hash * 31 + str.charCodeAt(i)) | 0;
		return Math.abs(hash) % 360;
	};

	var fillAvatar = function(node, name) {
		node.textContent = initials(name);
		node.style.setProperty('--avatar-hue', avatarHue(name));
	};

	/* Characters outside latin1 (emojis...) are sent as numeric HTML entities because the
	 * messenger tables use a latin1 charset. They are decoded back as plain text for display. */
	var encodeContent = function(content) {
		var out = '';
		Array.from(content).forEach(function(ch) {
			var code = ch.codePointAt(0);
			out += code > 0xFF ? '&#' + code + ';' : ch;
		});
		return out;
	};

	var decodeContent = function(content) {
		return String(content == null ? '' : content).replace(/&#(x[0-9a-fA-F]{1,6}|[0-9]{1,7});/g, function(match, value) {
			var code = value.charAt(0) === 'x' ? parseInt(value.substring(1), 16) : parseInt(value, 10);
			return (code > 0 && code <= 0x10FFFF && (code < 0xD800 || code > 0xDFFF)) ? String.fromCodePoint(code) : match;
		});
	};

	var isSafeImageUrl = function(url) {
		try {
			var parsed = new URL(url, window.location.href);
			return parsed.protocol === 'https:' || parsed.protocol === 'http:';
		} catch (e) {
			return false;
		}
	};

	var formatLocalDate = function(date) {
		var pad = function(n) { return (n < 10 ? '0' : '') + n; };
		return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' +
			pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds());
	};

	var generateMessageUniqueId = function() {
		var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
		var values = new Uint32Array(10);
		window.crypto.getRandomValues(values);
		var id = '';
		for (var i = 0; i < values.length; i++) id += chars.charAt(values[i] % chars.length);
		return 'msgid.' + id;
	};

	var playSound = function() {
		if (settingMuteAllNotif) return;
		if (audio == null) audio = new Audio('content/sounds/beep.wav');
		var played = audio.play();
		if (played && played.catch) played.catch(function() {}); // autoplay may be blocked
	};

	/* ---------------------------------------------------------------- HTTP */

	var request = function(method, url, options) {
		options = options || {};
		var controller = window.AbortController ? new AbortController() : null;
		var timer = controller ? setTimeout(function() { controller.abort(); }, REQUEST_TIMEOUT) : null;
		return fetch(url, {
			method: method,
			headers: options.headers || {},
			body: options.body,
			credentials: 'same-origin',
			cache: 'no-store',
			referrerPolicy: 'no-referrer',
			signal: controller ? controller.signal : undefined
		}).then(function(response) {
			return response.text().then(function(text) {
				var json = null;
				try { json = text ? JSON.parse(text) : null; } catch (e) { json = null; }
				if (json == null) throw new Error('Invalid response');
				return json;
			});
		}).finally(function() {
			if (timer) clearTimeout(timer);
		});
	};

	var apiHeaders = function(extra) {
		var headers = {
			"customerId": customerId,
			"authorization": "Bearer " + authToken,
			"hotelId": hotelId
		};
		for (var key in extra) headers[key] = extra[key];
		return headers;
	};

	var api = function(method, path, params, extraHeaders, body) {
		var url = config.basePath + path;
		if (params) {
			var query = new URLSearchParams();
			Object.keys(params).forEach(function(key) {
				if (params[key] != null) query.append(key, params[key]);
			});
			var qs = query.toString();
			if (qs) url += '?' + qs;
		}
		return request(method, url, { headers: apiHeaders(extraHeaders), body: body });
	};

	/* Responses wrap their payload as a JSON string in "data" */
	var parseData = function(json) {
		if (typeof json.data === 'string') return JSON.parse(json.data);
		return json.data;
	};

	/* ---------------------------------------------------------------- startup */

	var fillRequestParms = function() {
		var urlParams = new URLSearchParams(window.location.search);
		hotelId = urlParams.get('h');
		roomTypeCode = urlParams.get('roomTypeCode');
		isGuest = urlParams.get('guest');
		session = urlParams.get('session');
		if (isGuest == 1)
			roomNumber = urlParams.get('roomNumber');
		reservationNumber = urlParams.get('reservationNumber');
		customerName = urlParams.get('cn');
	};

	var getCustomerByName = function() {
		var form = new FormData();
		form.append("customerName", customerName);
		form.append("deviceTypeId", DEVICE_TYPE_ID);

		request("POST", config.authUrl + "/getCustomerByName", { body: form })
			.then(function(json) {
				if (json.success == false || !json.data) {
					showFatal('Please refresh the page.');
					return;
				}
				customerId = json.data.customerId;
				generateToken({ customerId: customerId, customerKey: json.data.customerKey });
			})
			.catch(function() { showFatal(); });
	};

	var generateToken = function(jsonData) {
		request("POST", config.authUrl + "/token/generateToken", {
			headers: { "content-type": "application/json" },
			body: JSON.stringify(jsonData)
		}).then(function(json) {
			if (json.success == false || !json.data) {
				showFatal('Please refresh the page.');
				return;
			}
			authToken = json.data.token;

			if (isHotel()) {
				elements.app.classList.add('mode-hotel');
				elements.guestsListId.hidden = false;
				getHotelGuestList(true);
			}
			else if (isGuest == 1 && session != null) {
				elements.app.classList.add('mode-guest');
				guestLogin();
			}
			else {
				showFatal('Invalid chat link.');
			}
		}).catch(function() { showFatal(); });
	};

	/* ---------------------------------------------------------------- hotel side */

	var getHotelGuestList = function(firstLoad) {
		api("GET", "api/hotel/guests", { hotelUsername: session })
			.then(function(json) {
				if (json.success == false) {
					showFatal(json.message);
					return;
				}
				var data = parseData(json);
				guestList = data.guestList || [];
				hotelTimezone = data.hotelTimezone;

				/* the open chat may have been assigned to someone else */
				if (activeGuest != null) {
					var stillActive = findGuest(activeGuest.guestUsername);
					if (stillActive) {
						activeGuest = stillActive;
						chatSecurityToken = stillActive.securityToken;
					} else {
						closeActiveGuest();
					}
				}

				showGuestList();
				connectToSocket();
				if (firstLoad) changeScreenState('', false, false, true);
			})
			.catch(function() { showFatal(); });
	};

	var findGuest = function(guestUsername) {
		for (var i = 0; i < guestList.length; i++) {
			if (guestList[i].guestUsername === guestUsername) return guestList[i];
		}
		return null;
	};

	var showGuestList = function() {
		var list = elements.hotelGuestsSideBarId;
		list.textContent = '';
		elements.guestCount.textContent = guestList.length ? guestList.length + (guestList.length > 1 ? ' active chats' : ' active chat') : '';
		elements.guestListEmpty.hidden = guestList.length > 0;

		guestList.forEach(function(guest) {
			var item = el('li', 'guest-item');
			item.setAttribute('role', 'option');
			item.tabIndex = 0;
			item.dataset.guest = guest.guestUsername;
			item.dataset.search = (guest.guestUsername + ' ' + guest.roomNumber).toLowerCase();
			if (activeGuest && activeGuest.guestUsername === guest.guestUsername) {
				item.classList.add('active');
				item.setAttribute('aria-selected', 'true');
			}

			var avatar = el('div', 'avatar');
			fillAvatar(avatar, guest.guestUsername);
			item.appendChild(avatar);

			var main = el('div', 'guest-main');
			main.appendChild(el('span', 'guest-name', guest.guestUsername));
			main.appendChild(el('span', 'guest-room muted', 'Room ' + guest.roomNumber));
			item.appendChild(main);

			var meta = el('div', 'guest-meta');
			meta.appendChild(el('span', 'guest-time muted', guest.lastContentTime || ''));
			var badge = el('span', 'badge');
			var unread = unreadCounts[guest.guestUsername] || 0;
			badge.textContent = unread > 99 ? '99+' : String(unread);
			badge.hidden = unread === 0;
			meta.appendChild(badge);
			item.appendChild(meta);

			item.addEventListener('click', function() { selectGuest(guest.guestUsername); });
			item.addEventListener('keydown', function(e) {
				if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); selectGuest(guest.guestUsername); }
			});
			list.appendChild(item);
		});
		filterGuests();
	};

	var filterGuests = function() {
		var term = elements.searchText.value.trim().toLowerCase();
		Array.prototype.forEach.call(elements.hotelGuestsSideBarId.children, function(item) {
			item.hidden = term !== '' && item.dataset.search.indexOf(term) === -1;
		});
	};

	var selectGuest = function(guestUsername) {
		var guest = findGuest(guestUsername);
		if (!guest) return;

		activeGuest = guest;
		roomNumber = guest.roomNumber;
		chatSecurityToken = guest.securityToken;
		unreadCounts[guestUsername] = 0;
		historySize = PAGE_SIZE;

		Array.prototype.forEach.call(elements.hotelGuestsSideBarId.children, function(item) {
			var selected = item.dataset.guest === guestUsername;
			item.classList.toggle('active', selected);
			item.setAttribute('aria-selected', selected ? 'true' : 'false');
			if (selected) item.querySelector('.badge').hidden = true;
		});

		setConversationHeader(guestUsername, 'Room ' + guest.roomNumber);
		elements.textInputId.hidden = false;
		elements.assignGuestId.hidden = false;
		elements.app.classList.add('chat-open');

		retreiveOldChatMessages(guestUsername, roomNumber, false);
		if (window.innerWidth > HOTEL_DEVICE_SCREEN) elements.txtMsg.focus();
	};

	var closeActiveGuest = function() {
		activeGuest = null;
		chatSecurityToken = null;
		elements.app.classList.remove('chat-open');
		elements.textInputId.hidden = true;
		elements.assignGuestId.hidden = true;
		clearMessages();
		setConversationHeader('Hotel Guest Relation Chat Room', null);
		Array.prototype.forEach.call(elements.hotelGuestsSideBarId.children, function(item) {
			item.classList.remove('active');
			item.setAttribute('aria-selected', 'false');
		});
	};

	var markUnread = function(guestUsername) {
		unreadCounts[guestUsername] = (unreadCounts[guestUsername] || 0) + 1;
		var item = elements.hotelGuestsSideBarId.querySelector('[data-guest="' + cssEscape(guestUsername) + '"]');
		if (item) {
			var badge = item.querySelector('.badge');
			var count = unreadCounts[guestUsername];
			badge.textContent = count > 99 ? '99+' : String(count);
			badge.hidden = false;
			item.title = count + (count > 1 ? ' new messages' : ' new message');
			/* most recent conversation on top */
			elements.hotelGuestsSideBarId.insertBefore(item, elements.hotelGuestsSideBarId.firstChild);
		}
	};

	var cssEscape = function(value) {
		return window.CSS && CSS.escape ? CSS.escape(value) : String(value).replace(/["\\]/g, '\\$&');
	};

	var assignGuestCall = function() {
		var guestUsername, roomNbr;
		if (isHotel()) {
			if (!activeGuest) return;
			guestUsername = activeGuest.guestUsername;
			roomNbr = activeGuest.roomNumber;
		} else {
			guestUsername = session;
			roomNbr = roomNumber;
		}

		api("GET", "api/hotel/guest/assign/" + encodeURIComponent(guestUsername) + "/" + encodeURIComponent(roomNbr) + "/", null, { "chatSecurityToken": chatSecurityToken })
			.then(function(json) {
				closeModal(elements.settingsPopup);
				if (json.success == false && json.message != "Already assigned.") {
					showToast(json.message);
					return;
				}
				showToast('Guest is now available to all staff.');
			})
			.catch(function() { showToast('Unable to assign the guest, please try again.'); });
	};

	/* ---------------------------------------------------------------- guest side */

	var guestLogin = function() {
		api("GET", "api/hotel/guest/logIn", { guestUsername: session, roomNumber: roomNumber, roomTypeCode: roomTypeCode })
			.then(function(json) {
				if (json.success == false) {
					showFatal(json.message);
					return;
				}
				var data = parseData(json);

				chatSecurityToken = data.guestRoom.securityToken;
				hotelTimezone = data.hotelTimezone;
				receiver = data.guestRoom.hotelUsername != null ? data.guestRoom.hotelUsername : "Hotel Staff";

				activeGuest = data.guestRoom;
				guestList = [data.guestRoom];

				setConversationHeader('Room ' + data.guestRoom.roomNumber, 'Hotel guest relations');
				elements.textInputId.hidden = false;
				elements.logoutId.hidden = false;
				elements.app.classList.add('chat-open');

				connectToSocket();
				retreiveOldChatMessages(session, roomNumber, false);
				changeScreenState('', false, false, true);
				elements.txtMsg.focus();
			})
			.catch(function() { showFatal(); });
	};

	var guestLogout = function() {
		api("GET", "api/hotel/guest/logOut", { guestUsername: session, roomNumber: roomNumber }, { "chatSecurityToken": chatSecurityToken })
			.then(function(json) {
				closeModal(elements.settingsPopup);
				if (json.success == false) {
					showFatal(json.message);
					return;
				}
				disconnectSocket();
				changeScreenState('Logged out successfully!', false, false, false);
				elements.loadingDiv.hidden = false;
				elements.loadingDiv.classList.add('is-success');
			})
			.catch(function() { showFatal(); });
	};

	/* ---------------------------------------------------------------- conversation */

	var setConversationHeader = function(title, subtitle) {
		elements.selectedGuestUsername.textContent = title;
		elements.selectedGuestUsername.title = subtitle || '';
		if (subtitle) fillAvatar(elements.conversationAvatar, isHotel() ? title : 'Hotel');
		else {
			elements.conversationAvatar.textContent = '';
			elements.conversationAvatar.appendChild(icon('fa-comments'));
		}
		updateEmptyState();
	};

	var clearMessages = function() {
		elements.previousMsgContainer.textContent = '';
		elements.newMsgContainer.textContent = '';
		elements.showPrevMsgs.hidden = true;
		updateEmptyState();
	};

	var updateEmptyState = function() {
		var hasMessages = elements.previousMsgContainer.childElementCount + elements.newMsgContainer.childElementCount > 0;
		elements.emptyState.hidden = hasMessages;
		elements.emptyStateText.textContent = activeGuest ? 'No messages yet, say hello!' : 'Select a guest to start chatting';
	};

	var scrollToBottom = function() {
		elements.msgContainerBase.scrollTop = elements.msgContainerBase.scrollHeight;
	};

	var statusIcon = function(status) {
		switch (status) {
			case 'SEEN': return { cls: 'fa-eye', title: 'Seen' };
			case 'DELIVERED': return { cls: 'fa-check-circle-o', title: 'Delivered' };
			case 'FAILED': return { cls: 'fa-exclamation-circle', title: 'Not sent' };
			case 'QUEUED': return { cls: 'fa-clock-o', title: 'Waiting for connection' };
			default: return { cls: 'fa-circle-o', title: 'Not sent yet' };
		}
	};

	var setMessageStatus = function(msgid, status) {
		if (!msgid) return;
		var node = document.getElementById('msgid.i.' + msgid);
		if (!node) return;
		var s = statusIcon(status);
		node.className = 'fa ' + s.cls + ' message-status status-' + status.toLowerCase();
		node.title = s.title;
	};

	var buildMessage = function(msg, isSender, status) {
		var row = el('div', 'message-row ' + (isSender ? 'outgoing' : 'incoming'));
		var bubble = el('div', 'bubble');

		if (msg.contentType == 'IMAGE') {
			if (isSafeImageUrl(msg.content)) {
				var link = el('a', 'bubble-image');
				link.href = msg.content;
				link.target = '_blank';
				link.rel = 'noopener noreferrer';
				var img = el('img');
				img.src = msg.content;
				img.alt = 'Image';
				img.loading = 'lazy';
				img.referrerPolicy = 'no-referrer';
				link.appendChild(img);
				bubble.appendChild(link);
			} else {
				bubble.appendChild(el('div', 'message-text muted', 'Image unavailable'));
			}
		} else {
			bubble.appendChild(el('div', 'message-text', decodeContent(msg.content)));
		}

		var footer = el('div', 'message-meta');
		var created = msg.createdDate ? String(msg.createdDate) : '';
		if (created.indexOf('.') !== -1) created = created.substring(0, created.indexOf('.'));
		created = created.replace('T', ' ');
		var time = el('time', 'message-time', created.length >= 16 ? created.substring(11, 16) : created);
		time.title = created;
		footer.appendChild(time);

		if (isSender) {
			var s = statusIcon(status);
			var i = icon(s.cls + ' message-status status-' + status.toLowerCase());
			if (msg.msgid) i.id = 'msgid.i.' + msg.msgid;
			i.title = s.title;
			footer.appendChild(i);
		}
		bubble.appendChild(footer);
		row.appendChild(bubble);
		return row;
	};

	var appendDaySeparator = function(container, msg) {
		var created = String(msg.createdDate || '').substring(0, 10);
		if (!created) return;
		if (container.dataset.lastDay === created) return;
		container.dataset.lastDay = created;
		var separator = el('div', 'day-separator');
		separator.appendChild(el('span', null, created));
		container.appendChild(separator);
	};

	/** Message received (or sent) live on the socket */
	var displayMessage = function(msg, isSender, status) {
		var chatGuest = isSender ? msg.receiver : msg.sender;

		if (isHotel()) {
			var guestOfMessage = isSender ? msg.receiver : msg.sender;
			if (!activeGuest || activeGuest.guestUsername !== guestOfMessage) {
				if (!isSender) {
					markUnread(chatGuest);
					playSound();
				}
				return;
			}
		}

		appendDaySeparator(elements.newMsgContainer, msg);
		elements.newMsgContainer.appendChild(buildMessage(msg, isSender, status));
		if (!isSender) playSound();
		updateEmptyState();
		scrollToBottom();
	};

	var retreiveOldChatMessages = function(guestUsername, roomNbr, isTop) {
		if (loadingHistory) return;
		loadingHistory = true;
		elements.showPrevMsgs.hidden = false;
		elements.showPrevMsgs.disabled = true;
		elements.showPrevMsgs.textContent = "Loading previous messages...";

		var previousHeight = elements.msgContainerBase.scrollHeight;
		var token = chatSecurityToken;
		var path = "api/hotel/guest/" + encodeURIComponent(guestUsername) + "/" + encodeURIComponent(roomNbr) + "/messages/0/" + historySize;

		api("GET", path, null, { "chatSecurityToken": token })
			.then(function(json) {
				/* user switched to another chat meanwhile */
				if (token !== chatSecurityToken) return;
				if (json.success == false) {
					showToast(json.message);
					return;
				}
				var data = parseData(json);

				clearMessages();
				delete elements.previousMsgContainer.dataset.lastDay;
				delete elements.newMsgContainer.dataset.lastDay;
				remainingMessagesCount = data.remainingMessagesCount;

				(data.messageList || []).forEach(function(msg) {
					var mine = msg.sender == session;
					appendDaySeparator(elements.previousMsgContainer, msg);
					elements.previousMsgContainer.appendChild(buildMessage(msg, mine, msg.status == 'SEEN' ? 'SEEN' : 'DELIVERED'));
				});
				elements.newMsgContainer.dataset.lastDay = elements.previousMsgContainer.dataset.lastDay || '';

				elements.showPrevMsgs.hidden = !(remainingMessagesCount > 0);
				elements.showPrevMsgs.textContent = "Show previous messages";
				updateEmptyState();

				if (isTop) elements.msgContainerBase.scrollTop = elements.msgContainerBase.scrollHeight - previousHeight;
				else scrollToBottom();

				markAsSeen();
			})
			.catch(function() { showToast('Unable to load messages, please try again.'); })
			.finally(function() {
				loadingHistory = false;
				elements.showPrevMsgs.disabled = false;
				if (elements.showPrevMsgs.textContent.indexOf('Loading') === 0) elements.showPrevMsgs.hidden = true;
			});
	};

	/** Marks the messages received in the open chat as SEEN */
	var markAsSeen = function() {
		if (!activeGuest) return;
		var params = { roomNumber: activeGuest.roomNumber };
		if (isHotel()) params.hotelUsername = session;
		else params.guestUsername = session;
		api("GET", "api/chat/status/change", params, { "chatSecurityToken": chatSecurityToken }).catch(function() {});
	};

	/* ---------------------------------------------------------------- socket */

	var chatIdentifierOf = function(guest) {
		return customerId + "." + hotelId + "." + guest.roomNumber + "." + guest.guestUsername;
	};

	var disconnectSocket = function() {
		intentionalDisconnect = true;
		clearTimeout(reconnectTimer);
		if (websocket != null) {
			try { websocket.disconnect(function() {}); } catch (e) {}
		}
		websocket = null;
		connected = false;
		setConnectionState('offline', 'Offline');
	};

	var connectToSocket = function() {
		disconnectSocket();
		intentionalDisconnect = false;
		setConnectionState('connecting', 'Connecting...');

		var socket = new SockJS(config.wsUrl);
		var client = Stomp.over(socket);
		client.debug = null;
		websocket = client;

		client.connect({ "authorization": "Bearer " + authToken, "customerId": customerId }, function() {
			if (websocket !== client) return;
			connected = true;
			reconnectAttempts = 0;
			setConnectionState('online', 'Online');

			client.subscribe('/user/queue/errors', function(message) {
				var response = safeParse(message.body);
				if (!response) return;
				if (response.msgid) setMessageStatus(response.msgid, 'FAILED');
				showToast(response.errorMessage || 'Message could not be sent.');
			});

			guestList.forEach(function(guest) {
				client.subscribe('/topic/private/' + chatIdentifierOf(guest), onChatMessage, {
					"chatSecurityToken": guest.securityToken,
					"hotelId": hotelId,
					"roomNumber": guest.roomNumber,
					"guestUsername": guest.guestUsername
				});
			});

			if (isHotel()) { // if hotel, subscribe for a channel to refresh guest details
				client.subscribe('/topic/private/' + hotelId, function(message) {
					var response = safeParse(message.body);
					if (response && response.actionKey == "refresh_guest_list") getHotelGuestList(false);
				});
			}

			flushOutbox();
		}, function() {
			if (websocket !== client) return;
			connected = false;
			if (intentionalDisconnect) return;
			scheduleReconnect();
		});
	};

	var scheduleReconnect = function() {
		if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
			setConnectionState('offline', 'Offline');
			showToast('Connection lost, please refresh the page.');
			return;
		}
		var delay = Math.min(30000, 1000 * Math.pow(2, reconnectAttempts));
		reconnectAttempts++;
		setConnectionState('connecting', 'Reconnecting...');
		clearTimeout(reconnectTimer);
		reconnectTimer = setTimeout(connectToSocket, delay);
	};

	var safeParse = function(body) {
		try { return JSON.parse(body); } catch (e) { return null; }
	};

	var onChatMessage = function(message) {
		var response = safeParse(message.body);
		if (!response) return;
		if (response.errorMessage != null) {
			showToast(response.errorMessage);
			return;
		}
		response.createdDate = response.date + " " + response.time + '.';

		if (response.sender == session) {
			if (sentMsgIds[response.msgid]) {
				setMessageStatus(response.msgid, 'DELIVERED');
				delete sentMsgIds[response.msgid];
			} else {
				/* sent from another tab / device */
				displayMessage(response, true, 'DELIVERED');
			}
		} else {
			if (!isHotel() && response.sender) receiver = response.sender;
			displayMessage(response, false, 'DELIVERED');
		}
	};

	var sendSocketMessage = function(content, contentType) {
		if (!activeGuest || !chatSecurityToken) return;

		var msgid = generateMessageUniqueId();
		var message = {
			chatSecurityToken: chatSecurityToken,
			customerId: customerId,
			hotelId: hotelId,
			sender: session,
			senderType: isHotel() ? "HOTEL" : "GUEST",
			receiver: isHotel() ? activeGuest.guestUsername : receiver,
			content: contentType == 'IMAGE' ? content : encodeContent(content),
			contentType: contentType,
			roomNumber: activeGuest.roomNumber,
			roomTypeCode: roomTypeCode || "",
			reservationNumber: reservationNumber || "",
			hotelTimezone: hotelTimezone,
			msgid: msgid,
			createdDate: formatLocalDate(new Date())
		};

		sentMsgIds[msgid] = true;
		displayMessage(message, true, connected ? 'SENT' : 'QUEUED');

		if (connected && websocket) websocket.send("/app/sendmsg", { priority: 9 }, JSON.stringify(message));
		else outbox.push(message);
	};

	/* Re-send what was typed while offline */
	var flushOutbox = function() {
		var pending = outbox;
		outbox = [];
		pending.forEach(function(message) {
			setMessageStatus(message.msgid, 'SENT');
			websocket.send("/app/sendmsg", { priority: 9 }, JSON.stringify(message));
		});
	};

	/* ---------------------------------------------------------------- images */

	var uploadSendImageMsg = function() {
		var files = window.ImageUpload ? ImageUpload.files() : [];
		var caption = elements.txtMsgPopup.value;

		if (files.length === 0) {
			if (caption.trim() !== '') {
				elements.txtMsgPopup.value = '';
				closeModal(elements.uploadImgPopup);
				sendSocketMessage(caption, "TYPING");
			}
			return;
		}

		var data = new FormData();
		files.forEach(function(file, i) { data.append('image_' + i, file); });

		elements.uploadSendBtn.disabled = true;
		api("POST", "api/upload/image", null, { "customerName": customerName }, data)
			.then(function(json) {
				if (json.success == false) {
					showToast(json.message);
					return;
				}
				closeModal(elements.uploadImgPopup);
				if (caption.trim() !== '') sendSocketMessage(caption, "TYPING");
				elements.txtMsgPopup.value = '';
				(json.data || []).forEach(function(url) { sendSocketMessage(url, "IMAGE"); });
				ImageUpload.clear();
			})
			.catch(function() { showToast('Image upload failed, please try again.'); })
			.finally(function() { elements.uploadSendBtn.disabled = false; });
	};

	/* ---------------------------------------------------------------- modals */

	var lastFocused = null;
	var openModal = function(modal) {
		lastFocused = document.activeElement;
		modal.hidden = false;
		var focusable = modal.querySelector('button, input, textarea');
		if (focusable) focusable.focus();
	};

	var closeModal = function(modal) {
		if (modal.hidden) return;
		modal.hidden = true;
		if (lastFocused && lastFocused.focus) lastFocused.focus();
	};

	var autoGrow = function(textarea) {
		textarea.style.height = 'auto';
		textarea.style.height = Math.min(textarea.scrollHeight, 140) + 'px';
		textarea.style.overflowY = textarea.scrollHeight > 140 ? 'auto' : 'hidden';
	};

	/* ---------------------------------------------------------------- public API */

	return {
		initiate: function(e) {
			elements = e;
			changeScreenState('Loading...', false, true, false);
			fillRequestParms();

			if (!hotelId || !customerName || !session || (isGuest != 0 && isGuest != 1) || (isGuest == 1 && !roomNumber)) {
				showFatal('Invalid chat link.');
				return;
			}
			getCustomerByName();
		},

		sendMessage: function() {
			var input = elements.txtMsg.value;
			if (input.trim() == '') return;
			elements.txtMsg.value = '';
			autoGrow(elements.txtMsg);
			elements.txtMsg.focus();
			sendSocketMessage(input, "TYPING");
		},

		sendMessage_keyup: function(e) {
			if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) { e.preventDefault(); this.sendMessage(); }
		},

		autoGrow: autoGrow,

		logout: function() { guestLogout(); },

		muteAllNotifications: function() {
			settingMuteAllNotif = !settingMuteAllNotif;
			var label = elements.muteAllNotifId.querySelector('span');
			label.textContent = settingMuteAllNotif ? "Unmute Notifications" : "Mute Notifications";
			elements.muteAllNotifId.querySelector('i').className = 'fa ' + (settingMuteAllNotif ? 'fa-bell-o' : 'fa-bell-slash-o');
		},

		searchGuests_keyup: function() { filterGuests(); },

		showPreviousMsgs: function() {
			if (loadingHistory || !activeGuest) return;
			historySize += PAGE_SIZE;
			retreiveOldChatMessages(isHotel() ? activeGuest.guestUsername : session, activeGuest.roomNumber, true);
		},

		removeActiveGuest: function() {
			if (isHotel()) closeActiveGuest();
		},

		assignGuest: function() { assignGuestCall(); },

		uploadSendMessage_click: function() { uploadSendImageMsg(); },

		uploadSendMessage_keyup: function(e) {
			if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) { e.preventDefault(); uploadSendImageMsg(); }
		},

		openModal: openModal,
		closeModal: closeModal,
		showToast: showToast
	};
};
