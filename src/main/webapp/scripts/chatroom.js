var CreateProxy = function(wsUri) {
	var elements = null;
	
	var websocket = null;
	var audio = null;
	
	var userChatIdentifier = null;
	var isGuest = null;
	var guestList = [];

	var settingMuteAllNotif = false;
	
	var hotelDeviceScreen = 767;
	
	var changeScreenState = function(errorLabelTxt, errorLabelColor, loadingDivDisp, msgPanelDisp, txtMsgFocus) {

		elements.msgLabel.innerHTML = errorLabelTxt;
		elements.msgLabel.style.color = errorLabelColor;

		elements.loadingDiv.style.display = loadingDivDisp;

		elements.msgPanel.style.display = msgPanelDisp;
		elements.msgContainer.style.display = msgPanelDisp;
		
		if (txtMsgFocus == true) elements.txtMsg.focus();
	};

	var authToken = null, customerId = null, customerName = null, hotelId = null, deviceTypeId = "3C1F3EE3A445C1BA", chatSecurityToken = null, session = null, receiver = null, sender = null;
	var roomTypeCode = null, roomNumber = null, reservationNumber = null, hotelTimezone = null;
	var fillRequestParms = function() {
		
//		var currentUrl = window.location.href;
//		customerName = currentUrl.substring(0, currentUrl.indexOf('.'));
//
//		if(customerName.includes("http://")) {
//			customerName = customerName.replace("http://", "");
//		}
//		else if(customerName.includes("https://")) {
//			customerName = customerName.replace("https://", "");
//		}
		
		var queryString = window.location.search;
		
		var urlParams = new URLSearchParams(queryString);
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
        form.append("deviceTypeId", deviceTypeId);
        
		$.ajax({
            method: "POST",
            url: "https://localhost:8080/getCustomerByName",
            data: form,
            timeout: 50000,
            processData: false,
            mimeType: "multipart/form-data",
            contentType: false,
            success: function (data,status,xhr) {
            	
            	var json = JSON.parse(data);
            	
            	if (json.success == false) {
        			changeScreenState('Please refresh the page.', 'red', 'block', 'none', false);
            		return;
            	}
            	
            	var headersOpt = {
                	"content-type": "application/json",
                };

            	customerId = json.data.customerId;
                var jsonData = {
                    customerId: customerId,
                    customerKey: json.data.customerKey
                };

               generateToken(headersOpt, jsonData);
            },
            error: function (jqXhr, textStatus, errorMessage) {

     			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
            }
        });
	};
	
	var generateToken = function (headersOpt, jsonData) {
		 $.ajax({
             method: "POST",
             url: "https://localhost:8080/token/generateToken",
             dataType: "json",
             headers: headersOpt,
             data: JSON.stringify(jsonData),
             timeout: 50000,
             success: function (json,status,xhr) {

	            	if (json.success == false) {
	        			changeScreenState('Please refresh the page.', 'red', 'block', 'none', false);
	            		return;
	            	}
	            	
	            	authToken = json.data.token;
	            	
	            	if (isGuest == 0) {
	            		elements.guestsListId.style.display = 'block';

	                	getHotelGuestList();
	            	} 
	            	
	            	else if (isGuest == 1 && session != null) {

	            		guestLogin();
	            	}
             },
             error: function (jqXhr, textStatus, errorMessage) {

      			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
	        }

         });
	};
	
	var getCurrenUrl = function() {

		var currentUrl = window.location.href;
		if (currentUrl.includes("index.jsp"))
			currentUrl = currentUrl.substring(0, currentUrl.indexOf('index.jsp'));
		else	
			currentUrl = currentUrl.substring(0, currentUrl.indexOf('?'));
		
		return currentUrl;
	};
	
	var getHotelGuestList = function() {
		
		var headersOpt = {
                "content-type": "application/json",
                "customerId": customerId,
                "authorization": "Bearer " + authToken,
                "hotelId": hotelId
            };
		
		var currentUrl = getCurrenUrl() + "api/hotel/guests?hotelUsername=" + session;
		$.ajax({
            method: "GET",
            url: currentUrl,
            dataType: "json",
            headers: headersOpt,
            timeout: 50000,
            success: function (json,status,xhr) {

            	json = JSON.parse(json.data);
            	
            	if (json.success == false) {
        			changeScreenState(json.message, 'red', 'block', 'none', false);
            		return;
            	}
            	
            	guestList = json.guestList;
            	hotelTimezone = json.hotelTimezone;
        		elements.logoutId.style.display = 'none';
            	
            	showGuestList(guestList);
            	
            },
            error: function (jqXhr, textStatus, errorMessage) {

     			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
        		return;
            }

        });
	}

	var showGuestList = function(guestList) {
		
		elements.hotelGuestsSideBarId.innerHTML = "";
		for (var i = 0; i < guestList.length; i++) {
			
			var div = document.createElement('div');
			div.className = 'row sideBar-body';
			div.id = guestList[i].guestUsername;
			div.roomNumber = guestList[i].roomNumber;
			div.chatSecurityToken = guestList[i].securityToken;
			div.onclick = function(event) {

				roomNumber = event.currentTarget.roomNumber;
				userChatIdentifier = event.currentTarget.id;

        		elements.previousMsgContainer.innerHTML = "";
        		elements.newMsgContainer.innerHTML = "";
        		retreiveOldChatMessages(userChatIdentifier, roomNumber, false);
        		
				chatSecurityToken = event.currentTarget.chatSecurityToken;
				var msgContainers = document.querySelectorAll(`[id^="msgContainer."]`);
				for (var i = 0; i < msgContainers.length; i++) {
					msgContainers[i].style.display = 'none';
				}
				
				var div = document.getElementById('msgContainer.' + userChatIdentifier);
				if (div != null) {
					div.style.display = 'block';
				}
				elements.selectedGuestUsername.innerHTML = userChatIdentifier;
				elements.textInputId.style.display = 'block';
				
				for (var i = 0; i < guestList.length; i++) {
					var div = document.getElementById(guestList[i].guestUsername);
					if (div.style.backgroundColor == "rgb(242, 242, 242)") {
						div.style.backgroundColor = "#fff";
						div.active = false;
					}
				}
				event.currentTarget.style.backgroundColor = "#f2f2f2";
				event.currentTarget.active = true;
				
				elements.assignGuestId.style.display = 'block';
				onResize();

				elements.txtMsg.focus();
			}

			var div2 = document.createElement('div');
			div2.className = 'col-sm-3 col-xs-3 sideBar-avatar';
			
			var div3 = document.createElement('div');
			div3.className = 'avatar-icon';
			
			var img = document.createElement('img');
			img.src = "https://bootdey.com/img/Content/avatar/avatar1.png";
			div3.appendChild(img);
			div2.appendChild(div3);
			div.appendChild(div2);

			
			var div4 = document.createElement('div');
			div4.className = 'col-sm-9 col-xs-9 sideBar-main';
			var div5 = document.createElement('div');
			div5.className = 'row';
			var div6 = document.createElement('div');
			div6.className = 'col-sm-8 col-xs-8 sideBar-name';

			var span = document.createElement('span');
			span.className = 'name-meta';
			span.innerHTML = guestList[i].guestUsername + "<br>Ro." + guestList[i].roomNumber;
			div6.appendChild(span);
			div5.appendChild(div6);
			div4.appendChild(div5);
			div.appendChild(div4);

			var div7 = document.createElement('div');
			div7.className = 'col-sm-4 col-xs-4 pull-right sideBar-tim';
			var span2 = document.createElement('span');
			span2.className = 'time-meta pull-right';
			if (guestList[i].lastContentTime != null && guestList[i].lastContentTime != undefined)
				span2.innerHTML = guestList[i].lastContentTime;
			div7.appendChild(span2);
			div5.appendChild(div7);
			
			elements.hotelGuestsSideBarId.appendChild(div);
			
		}
		// open socket connection
		connectToSocket();
		changeScreenState('Loading...', '#fff', 'none', 'block', true);
	};
	
	var guestLogin = function () {
		var headersOpt = {
                "content-type": "application/json",
                "customerId": customerId,
                "authorization": "Bearer " + authToken,
                "hotelId": hotelId
            };

		var currentUrl = getCurrenUrl() + "api/hotel/guest/logIn?guestUsername=" + session + "&roomNumber=" + roomNumber + "&roomTypeCode=" + roomTypeCode
		$.ajax({
            method: "GET",
            url: currentUrl,
            dataType: "json",
            headers: headersOpt,
            timeout: 50000,
            success: function (json,status,xhr) {

            	if (json.success == false) {
        			changeScreenState(json.message, 'red', 'block', 'none', false);
            		return;
            	}
            	json = JSON.parse(json.data);
            	
            	chatSecurityToken = json.guestRoom.securityToken;
            	hotelTimezone = json.hotelTimezone;
            	receiver = "Hotel Staff";
            	if (json.guestRoom.hotelUsername != null)
            		receiver = json.guestRoom.hotelUsername;
            	
            	elements.selectedGuestUsername.innerHTML = "Room # " + json.guestRoom.roomNumber;
				elements.textInputId.style.display = 'block';
        		elements.guestsListId.style.display = 'none';
        		elements.conversation.style.width = '100%';
        		elements.logoutId.style.display = 'block';
        		
        		guestList.push(json.guestRoom);  
    			connectToSocket();

        		retreiveOldChatMessages(session, roomNumber, false);
        		
    			changeScreenState('Loading...', '#fff', 'none', 'block', true);
            	
            },
            error: function (jqXhr, textStatus, errorMessage) {

     			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
        		return;
            }

        });
	};
	
	var guestLogout = function (event) {
		var headersOpt = {
                "content-type": "application/json",
                "customerId": customerId,
                "authorization": "Bearer " + authToken,
                "hotelId": hotelId,
                "chatSecurityToken": chatSecurityToken
            };

		var currentUrl = getCurrenUrl() + "api/hotel/guest/logOut?guestUsername=" + session + "&roomNumber=" + roomNumber 
		$.ajax({
            method: "GET",
            url: currentUrl,
            dataType: "json",
            headers: headersOpt,
            timeout: 50000,
            success: function (json,status,xhr) {

            	if (json.success == false) {
        			elements.closePopup.click();
        			changeScreenState(json.message, 'red', 'block', 'none', false);
            		return;
            	}
				disconnectSocket();
    			elements.closePopup.click();
    			changeScreenState('Logged out successfully!', 'green', 'block', 'none', true);
            	
            },
            error: function (jqXhr, textStatus, errorMessage) {

     			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
        		return;
            }

        });
	};
	
	var disconnectSocket = function() {
		// Initiate the socket and set up the events
		if (websocket != null && websocket != undefined) {
			websocket.disconnect(function() {
            	console.log("Websocket disconnected");
       		});
		}
		websocket = null;
	};
	
	var connectToSocket = function() {
		
		disconnectSocket();
		
    	socket = new SockJS(wsUri);
    	websocket = Stomp.over(socket);
		
    	websocket.connect({}, function () {
    		for (var i = 0; i < guestList.length; i++) { 

	            var chatIdentifier = customerId + "." + hotelId + "." + guestList[i].roomNumber + "." + guestList[i].guestUsername + "";
	
	            websocket.subscribe('/topic/private/' + chatIdentifier, function (message) {
	            	var response = JSON.parse(message.body);
	            	
	            	if (response.errorMessage != null) {
	        			changeScreenState(response.errorMessage, 'red', 'block', 'none', false);
	            	} 
	            	else {
	            		response.createdDate = response.date + " " + response.time + '.';
	            		if (response.sender == session)
	            			displayDeliveredMessage(response, true); // sender
	            		else 
	            			displayMessage(response, false, true, false, false); // receiver
	            	}
	            });
    		}
            
            if (isGuest == 0) { // if hotel, subscribe for a channel to refresh guest details

	            websocket.subscribe('/topic/private/' + hotelId, function (message) {
	            	var response = JSON.parse(message.body);
	            	if (response.actionKey == "refresh_guest_list") {
	            		getHotelGuestList();
	            	}
	            });
            }
        });
	};

	var displayDeliveredMessage = function(msg, isSender) {

		var divMsg = document.getElementById('msgid.i.' + msg.msgid);
		if (divMsg != undefined && divMsg != null)
			divMsg.className = 'fa fa-check-circle-o pull-right'; // DELIVERED
	}
	
	var unreadReceivedMsgs = [];
	var displayMessage = function(msg, isSender, isDelivered, isPreviousMsg, isTop) {
		 
//		if (elements.msgContainer.childNodes.length == 100) {
//			elements.msgContainer.removeChild(elements.msgContainer.childNodes[0]);
//		}

		var msgId = null;
		if (isSender)
			msgId = msg.receiver;
		else 
			msgId = msg.sender;
		
		var div0 = null;
		if (!isPreviousMsg) {
			div0 = document.getElementById('msgContainer.' + msgId);
			if (div0 == null || div0 == undefined) {
				div0 = document.createElement('div');
				div0.id = 'msgContainer.' + msgId;
				div0.className = 'row message-body';
			}
			
			if (isGuest == 0) {  
				var activeGuest = getActiveGuest();
				div0.style.display = 'block';
				if (activeGuest == null ||
						(activeGuest != null && activeGuest != msgId)) {

					div0.style.display = 'none';
	            	playSound();
				}

				if (msg.sender !=  session) {
					var getunreadReceivedMsg = getUnreadMsg(msgId);
					if (getunreadReceivedMsg == null) {
						var unreadReceivedMsg = {};
						unreadReceivedMsg.id = msgId;
						unreadReceivedMsg.msgNumber = 1;
						unreadReceivedMsgs.push(unreadReceivedMsg);
						getunreadReceivedMsg = unreadReceivedMsg;
					}
					else 
						getunreadReceivedMsg.msgNumber = getunreadReceivedMsg.msgNumber + 1;
					
					var userDiv = document.getElementById(msgId);
					userDiv.style.backgroundColor = "#068484";
					userDiv.title = getunreadReceivedMsg.msgNumber + " new message";
					if (getunreadReceivedMsg.msgNumber > 1)
						userDiv.title = getunreadReceivedMsg.msgNumber + " new messages";
				}
			}
		}
		
		var div = document.createElement('div');
		div.className = 'row message-body';
		var div2 = document.createElement('div');
		var div3 = document.createElement('div');
		if (isSender) {
			div2.className = 'col-sm-12 message-main-sender';
			div3.className = 'sender';
		} else {
			div2.className = 'col-sm-12 message-main-receiver';
			div3.className = 'receiver';
			if (!isPreviousMsg)
				playSound();
		}
		var div4 = document.createElement('div');
		div4.className = 'message-text';
		if (msg.contentType == 'TYPING')
			div4.innerHTML = msg.content;
		else if (msg.contentType == 'IMAGE') {
			var coverImgDiv = document.createElement('div');
			coverImgDiv.className = "upload__img-box";
			
			var imgDiv = document.createElement('div');
			imgDiv.className = "img-bg";
			imgDiv.style.paddingBottom = "100%";
			imgDiv.style.backgroundImage = "url('" + msg.content + "')";
			coverImgDiv.appendChild(imgDiv);
			div4.appendChild(coverImgDiv);
		}
		div3.appendChild(div4);

		var footerSpan = document.createElement('span');

		var dateSpan = document.createElement('span');
		dateSpan.style.fontSize = '11px';
		dateSpan.style.color = '#9a9a9a';
		
		var createdDate = msg.createdDate.substring(0, msg.createdDate.indexOf('.'));
		createdDate = createdDate.replace('T', ' '); // for send message 
		dateSpan.innerHTML = createdDate;
		dateSpan.title = dateSpan.innerHTML;
		footerSpan.appendChild(dateSpan);
		
		var i = document.createElement('i');
		i.id = 'msgid.i.' + msg.msgid;
		i.className = 'fa fa-circle-o pull-right'; // NOT SENT YET
		if (isDelivered)
			i.className = 'fa fa-check-circle-o pull-right'; // DELIVERED
		if (msg.status == "SEEN")
			i.className = 'fa fa-solid fa-eye pull-right'; // SEEN
			
//		i.style.cssFloat = 'right';
		i.style.fontSize = '11px';
		footerSpan.appendChild(i);
		
		div3.appendChild(footerSpan);

		div2.appendChild(div3);
		div.appendChild(div2);
		
		if (isPreviousMsg) {
			elements.previousMsgContainer.appendChild(div);
		} else {
			div0.appendChild(div);
			elements.newMsgContainer.appendChild(div0);
		}
		if (!isTop)
			elements.msgContainerBase.scrollTop = elements.msgContainerBase.scrollHeight;
	};
	
	var getUnreadMsg = function(msgId) {
		for (var i = 0; i < unreadReceivedMsgs.length; i++) {
			if (unreadReceivedMsgs[i].id == msgId)
				return unreadReceivedMsgs[i];
		}
		return null;
	};
	
	var playSound = function() {
		if (settingMuteAllNotif)
			return;
		
		if (audio == null) {
			audio = new Audio('content/sounds/beep.wav');
		}
		
		audio.play();
	};
	
	
	var getActiveGuest = function() {

		for (var i = 0; i < guestList.length; i++) {
			var div = document.getElementById(guestList[i].guestUsername);
			if (div != null && div.active == true)
				return div.id;
		}
		return null;
	};
	
	var sendSocketMessage = function(content, contentType) {
		 var chatIdentifier = customerId + "." + hotelId + "." + roomNumber + "." + userChatIdentifier + "";
		 sender = session;
		 if (isGuest == 1) {
			 senderType = "GUEST";
		 } else if (isGuest == 0) {
			 senderType = "HOTEL";
			 receiver = getActiveGuest();
		 }
		 if (roomTypeCode == null) roomTypeCode = "";
		 if (reservationNumber == null) reservationNumber = "";

		/* Used to show Message Status */
		var msgid = generateMessageUniqueId();
			
         var message = {
         		chatSecurityToken: chatSecurityToken,
         		customerId: customerId, 
         		hotelId: hotelId, 
 				sender : sender,
 				senderType : senderType,
 				receiver : receiver,
 				content : returnNewContentWithEmojisCode(content),
 				contentType : contentType,
 				roomNumber: roomNumber,
 				roomTypeCode: roomTypeCode,
 				reservationNumber: reservationNumber,
 				hotelTimezone: hotelTimezone,
 				msgid: msgid,
 				createdDate: new Date().toISOString()
 				
 		};

 		displayMessage(message, true, false, false, false); // sender
 
        websocket.send("/app/sendmsg", {priority: 9}, JSON.stringify(message));
	};
	
	var returnNewContentWithEmojisCode = function(content) {
		var emojis = [];
 		for (var i = 0; i < categories.length; i++) {
 	 		var categoryObj = emojiObj[categories[i]];
 	 		categoryObj.forEach(ej => {
 	 	 		
 	 			var emoji = {};
 	 			emoji.img = ej.emoji;
 	 			emoji.code = '&#' + ej.emoji.codePointAt(0) + ";";
 	 			emojis.push(emoji);
 	        });
 		}
 		
 		var msgArray = Array.from(content);
 		var newContent = "";
 		for (var i = 0; i < msgArray.length; i++) {
 			emojiCode = returnEmojiCode(emojis, msgArray[i]);
 			if (emojiCode != null) 
 				newContent += emojiCode;
 			else
 				newContent += msgArray[i];
 		}
 		
 		return newContent;
	}
	
	var returnEmojiCode = function(emojis, char){

 		for (var i = 0; i < emojis.length; i++) {
 			if (emojis[i].img == char)
 				return emojis[i].code;
 		}
 		return null;
	}
	
	var settingMuteAllNotifications = function () {
		settingMuteAllNotif = !settingMuteAllNotif;
		if (settingMuteAllNotif)
			elements.muteAllNotifId.innerHTML = "Unmute Notifications";
		else 
			elements.muteAllNotifId.innerHTML = "Mute Notifications";
	}

	var oldChatMessager = [];
	var fromRow = 0, initToRow = 100, toRow = 100;
	var remainingMessagesCount = 0;
	var retreiveOldChatMessages = function (guestUsername, roomNbr, isTop) {
					
		elements.showPrevMsgs.innerHTML = "Loading Previous Messages...";
		var headersOpt = {
                "content-type": "application/json",
                "customerId": customerId,
                "authorization": "Bearer " + authToken,
                "hotelId": hotelId
            };

		var currentUrl = getCurrenUrl() + "api/hotel/guest/" + guestUsername + "/" + roomNbr + "/messages/" + fromRow + "/" + toRow;
		$.ajax({
            method: "GET",
            url: currentUrl,
            dataType: "json",
            headers: headersOpt,
            timeout: 50000,
            success: function (json,status,xhr) {

            	if (json.success == false) {
        			elements.closePopup.click();
        			changeScreenState(json.message, 'red', 'block', 'none', false);
            		return;
            	}
    			
            	json = JSON.parse(json.data);

        		elements.previousMsgContainer.innerHTML = "";
        		elements.newMsgContainer.innerHTML = "";
    			remainingMessagesCount = json.remainingMessagesCount;
    			elements.showPrevMsgs.innerHTML = "";
    			if (remainingMessagesCount > 0)
        			elements.showPrevMsgs.innerHTML = "Show Previous Messages";
    			/* for loop messages, show messages */
    			var oldMessagesList = Array.from(json.messageList);
    			for (var i = 0; i < oldMessagesList.length; i++) {
            		if (oldMessagesList[i].sender == session)
            			displayMessage(oldMessagesList[i], true, true, true, isTop); // sender
            		else
            			displayMessage(oldMessagesList[i], false, true, true, isTop); // receiver
    			}
            	
            },
            error: function (jqXhr, textStatus, errorMessage) {

     			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
        		return;
            }

        });
	}
	
	var onResize = function() {
		if (isGuest == 0) {
			var width = window.innerWidth;
			if (width <= hotelDeviceScreen) {
				var activeGuest = getActiveGuest();
				if (activeGuest != null) {
					elements.toGuestMobileScreenId.innerHTML = activeGuest; 
					elements.msgContainerHotelMobileScreen.appendChild(elements.msgContainer);
					elements.msgContainerHotelMobileScreen.scrollTop = elements.msgContainerHotelMobileScreen.scrollHeight;
					elements.msgContainerHotelMobileScreen.style.display = 'block';
					
					elements.textInputHotelMobileScreen.appendChild(elements.textInputId);
					elements.headingCompose.click();
				}
			} else {
				elements.newMessageBack.click();
				elements.msgContainerBase.appendChild(elements.msgContainer);
				elements.msgContainer.style.display = 'block';
				
				elements.textInputBaseId.appendChild(elements.textInputId);
			}
		}
	}
	
	var msgidList = [];
	var generateMessageUniqueId = function() {
		var msgid = "msgid." + createMessageUniqueId(6);

		for (var i = 0; i < msgidList.length; i++){
			if (msgidList[i] == msgid)
				generateMessageUniqueId();
		}
		msgidList.push(msgid);
		return msgid;
	}
	
	var createMessageUniqueId  = function(length) {
	    var result           = '';
	    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	    var charactersLength = characters.length;
	    for ( var i = 0; i < length; i++ ) {
	      result += characters.charAt(Math.floor(Math.random() * charactersLength));
	   }
	   return result;
	}
	
	var assignGuestCall = function () {

		if (isGuest == 0) {

			guestUsername = getActiveGuest();
			div = document.getElementById(guestUsername);
			roomNbr = div.roomNumber;
		} 
		else if (isGuest == 1) {

			guestUsername = session;
			roomNbr = roomNumber;
		} 
		else
			return;
		
		var headersOpt = {
                "content-type": "application/json",
                "customerId": customerId,
                "authorization": "Bearer " + authToken,
                "hotelId": hotelId
            };

		var currentUrl = getCurrenUrl() + "api/hotel/guest/assign/" + guestUsername + "/" + roomNbr + "/"; 
		$.ajax({
            method: "GET",
            url: currentUrl,
            dataType: "json",
            headers: headersOpt,
            timeout: 50000,
            success: function (json,status,xhr) {

            	if (json.success == false) {
            		if (json.message != "Already assigned.") {
            			elements.closePopup.click();
            			changeScreenState(json.message, 'red', 'block', 'none', false);
                		return;
            		}
            	}
    			elements.closePopup.click();
            },
            error: function (jqXhr, textStatus, errorMessage) {

     			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
        		return;
            }

        });
	};

	var uploadSendImageMsg = function() {

		var data = new FormData();
		for (var i = 0; i < imgArray.length; i++) {
		    data.append('image_'+i, imgArray[i]);
		}
		uploadImage(data);
	};

	var uploadImage = function(data) {
		
		var headersOpt = {
                "customerId": customerId,
                "authorization": "Bearer " + authToken,
                "hotelId": hotelId,
                "customerName": customerName
            };

		
		var currentUrl = getCurrenUrl() + "api/upload/image";
		$.ajax({
            method: "POST",
            url: currentUrl,
            dataType: "json",
            data: data,
            processData: false, 
            contentType: false, 
            headers: headersOpt,
            timeout: 50000,
            success: function (json,status,xhr) {

            	if (json.success == false) {
        			elements.closePopup.click();
        			changeScreenState(json.message, 'red', 'block', 'none', false);
            		return;
            	} 

         		var imageUrlList = Array.from(json.data);
        		sendMessageAfterUploadImages(imageUrlList);
            },
            error: function (jqXhr, textStatus, errorMessage) {
     			changeScreenState('Error, Please refresh the page.', 'red', 'block', 'none', false);
        		return;
            }

        });
	};
	
	var sendMessageAfterUploadImages = function(imageUrlList) {

		var content = elements.txtMsgPopup.value;
		if (content != undefined && content != null && content.trim != '') {
    		elements.txtMsgPopup.value = '';
			elements.closeUploadPopup.click();
    		sendSocketMessage(content, "TYPING");
		}
		
		/* after finishing the images upload */
		for (var i = 0; i < imageUrlList.length; i++) {
			if (!imageUrlList[i].includes("Error")) /* If returned error so the image was not uploaded successfully */
				sendSocketMessage(imageUrlList[i], "IMAGE");
		}
		
		var uploadedImages = document.querySelectorAll('.upload__img-close');
		uploadedImages.forEach(closeImg => {
			closeImg.click();
		});
	};
	
	return {
		initiate: function(e) {
			elements = e;
			elements.closePopup.click();
			changeScreenState('Loading...', '#fff', 'block', 'none', false);
			
			fillRequestParms();
			
	        getCustomerByName();
				
		},
		sendMessage: function() {
			elements.txtMsg.focus();
			
			var input = elements.txtMsg.value.trim();
			if (input == '') { 
				return; 
			}

			var content = elements.txtMsg.value;
			var contentType = "TYPING";
			
			elements.txtMsg.value = '';
			
			sendSocketMessage(content, contentType);

		},
		login_keyup: function(e) { if (e.keyCode == 13) { this.login(); } },
		sendMessage_keyup: function(e) { if (e.keyCode == 13) { this.sendMessage(); } },
		sendMessage_click: function(e) { this.sendMessage(); },
		
		logout: function() {
			
			guestLogout();
		},
		
		muteAllNotifications: function() {
			settingMuteAllNotifications();
		},
		
		onresize: function() {
			onResize();
		},

		searchGuests_keyup: function() {

			for (var i = 0; i < guestList.length; i++) {
				var div = document.getElementById(guestList[i].guestUsername);

				if (elements.searchText.value.trim() == "") {
					div.style.display = "block";
				} else if (!guestList[i].guestUsername.includes(elements.searchText.value)) 
					div.style.display = "none";
				else 
					div.style.display = "block";
					
			}
		},
		
		showPreviousMsgs: function(){
			if (elements.showPrevMsgs.innerHTML.includes("Loading"))
				return;
			
			fromRow = 0;
			toRow = initToRow + toRow;
			
			if (isGuest == 1) 
				retreiveOldChatMessages(session, roomNumber, true);
		},
		
		removeActiveGuest: function() {
			for (var i = 0; i < guestList.length; i++) {
				var div = document.getElementById(guestList[i].guestUsername);
				if (div != null && div.style.backgroundColor == "rgb(242, 242, 242)") {
					div.style.backgroundColor = "#fff";
					div.active = false;
				}
			}
		},
		
		assignGuest: function() {
			assignGuestCall();
		},
		
		uploadSendMessage_click: function() {
			uploadSendImageMsg();
		},
		
		uploadSendMessage_keyup: function(e) {
			if (e.keyCode == 13) { 
				uploadSendImageMsg();
			}
		}
	}
};