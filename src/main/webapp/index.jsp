<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" import="java.net.URI"%>
<%!
	/* HTML attribute escaping for every server value written in the page */
	private static String esc(String value) {
		if (value == null) return "";
		StringBuilder sb = new StringBuilder(value.length());
		for (char c : value.toCharArray()) {
			switch (c) {
				case '&': sb.append("&amp;"); break;
				case '<': sb.append("&lt;"); break;
				case '>': sb.append("&gt;"); break;
				case '"': sb.append("&quot;"); break;
				case '\'': sb.append("&#39;"); break;
				default: sb.append(c);
			}
		}
		return sb.toString();
	}

	/* scheme://host[:port] of a URL, empty when invalid */
	private static String origin(String url) {
		try {
			URI uri = new URI(url);
			if (uri.getScheme() == null || uri.getHost() == null) return "";
			String scheme = uri.getScheme().toLowerCase();
			if (!scheme.equals("http") && !scheme.equals("https")) return "";
			return scheme + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
		} catch (Exception e) {
			return "";
		}
	}
%>
<%
	String contextPath = request.getContextPath();
	/* MESSENGER_AUTH_SERVICE_URL (environment) overrides the AuthServiceUrl context-param of web.xml */
	String authServiceUrl = System.getenv("MESSENGER_AUTH_SERVICE_URL");
	if (authServiceUrl == null || authServiceUrl.trim().isEmpty()) authServiceUrl = getServletContext().getInitParameter("AuthServiceUrl");
	if (authServiceUrl == null || authServiceUrl.trim().isEmpty()) authServiceUrl = "https://localhost:8080";
	authServiceUrl = authServiceUrl.trim().replaceAll("/+$", "");
	String authOrigin = origin(authServiceUrl);

	String host = request.getServerName() + ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort());
	String wsOrigin = (request.isSecure() ? "wss://" : "ws://") + host.replaceAll("[^A-Za-z0-9.:\\-\\[\\]]", "");

	String csp = "default-src 'self'; "
		+ "script-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; "
		+ "style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com; "
		+ "font-src 'self' https://cdnjs.cloudflare.com; "
		+ "img-src 'self' data: blob:; "
		+ "media-src 'self'; "
		+ "connect-src 'self' " + wsOrigin + " " + authOrigin + "; "
		+ "frame-src 'self'; "
		+ "object-src 'none'; base-uri 'none'; form-action 'none'";
%>
<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
		<meta http-equiv="Content-Security-Policy" content="<%= esc(csp) %>">
		<meta name="referrer" content="no-referrer">
		<meta name="color-scheme" content="light dark">
		<title>Messenger</title>
		<link rel="icon" type="image/png" href="images/icon.png">

		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"
			integrity="sha384-wvfXpqpZZVQGK6TAh5PVlGOfQNHSoD2xbE+QkPxCAFlNEevoEH3Sl0sibVcOQVnN" crossorigin="anonymous" referrerpolicy="no-referrer">
		<link rel="stylesheet" href="content/styles/site.css">
	</head>
	<body data-base-path="<%= esc(contextPath) %>/" data-ws-url="<%= esc(contextPath) %>/sendmsg" data-auth-url="<%= esc(authServiceUrl) %>">

		<div id="loadingDiv" class="screen" role="status" aria-live="polite">
			<div class="screen-card">
				<div class="spinner" id="loadingSpinner" aria-hidden="true"></div>
				<div id="msgLabel" class="screen-label">Loading...</div>
			</div>
		</div>

		<div class="app" id="msgPanel" hidden>

			<aside class="sidebar" id="guestsListId" hidden>
				<header class="sidebar-header">
					<img class="brand-logo" src="images/icon.png" alt="">
					<div class="sidebar-title">
						<strong>Guest chats</strong>
						<span id="guestCount" class="muted"></span>
					</div>
				</header>
				<div class="search">
					<i class="fa fa-search" aria-hidden="true"></i>
					<input id="searchText" type="search" placeholder="Search guests or rooms" autocomplete="off" aria-label="Search guests">
				</div>
				<ul class="guest-list" id="hotelGuestsSideBarId" role="listbox" aria-label="Guests"></ul>
				<div class="guest-list-empty muted" id="guestListEmpty" hidden>No active guest chats</div>
			</aside>

			<main class="conversation" id="conversation">
				<header class="conversation-header">
					<button type="button" class="icon-btn back-btn" id="newMessageBack" aria-label="Back to guests">
						<i class="fa fa-arrow-left" aria-hidden="true"></i>
					</button>
					<div class="avatar" id="conversationAvatar" aria-hidden="true"><i class="fa fa-comments"></i></div>
					<div class="conversation-title">
						<span id="selectedGuestUsername">Hotel Guest Relation Chat Room</span>
						<span class="connection" id="connectionStatus" data-state="offline">Offline</span>
					</div>
					<button type="button" class="icon-btn" id="settingsBtn" aria-label="Settings" aria-haspopup="dialog">
						<i class="fa fa-ellipsis-v" aria-hidden="true"></i>
					</button>
				</header>

				<section class="messages" id="msgContainerBase" aria-live="polite">
					<div id="msgContainer" class="message-list">
						<button type="button" class="load-previous" id="showPrevMsgs" hidden></button>
						<div id="previousMsgContainer"></div>
						<div id="newMsgContainer"></div>
					</div>
					<div class="empty-state" id="emptyState">
						<i class="fa fa-comments-o" aria-hidden="true"></i>
						<p id="emptyStateText">Select a guest to start chatting</p>
					</div>
				</section>

				<footer class="composer" id="textInputId" hidden>
					<button type="button" class="icon-btn second-btn" aria-label="Insert emoji"><i class="fa fa-smile-o" aria-hidden="true"></i></button>
					<button type="button" class="icon-btn" id="attachBtn" aria-label="Send images" aria-haspopup="dialog"><i class="fa fa-paperclip" aria-hidden="true"></i></button>
					<textarea id="txtMsg" class="two" rows="1" maxlength="2000" placeholder="Type a message" aria-label="Message"></textarea>
					<button type="button" class="send-btn" id="sendBtn" aria-label="Send"><i class="fa fa-paper-plane" aria-hidden="true"></i></button>
				</footer>
			</main>
		</div>

		<div class="toast" id="toast" role="alert" hidden></div>

		<div id="settingsPopup" class="modal" hidden role="dialog" aria-modal="true" aria-labelledby="settingsTitle">
			<div class="modal-card modal-small">
				<header class="modal-header">
					<h2 id="settingsTitle">Settings</h2>
					<button type="button" class="icon-btn" id="closePopup" data-close-modal aria-label="Close"><i class="fa fa-times" aria-hidden="true"></i></button>
				</header>
				<div class="menu">
					<button type="button" class="menu-item" id="muteAllNotifId"><i class="fa fa-bell-slash-o" aria-hidden="true"></i><span>Mute Notifications</span></button>
					<button type="button" class="menu-item" id="assignGuestId" hidden><i class="fa fa-users" aria-hidden="true"></i><span>Assign guest to other staffs</span></button>
					<button type="button" class="menu-item danger" id="logoutId" hidden><i class="fa fa-sign-out" aria-hidden="true"></i><span>Logout</span></button>
				</div>
			</div>
		</div>

		<div id="uploadImgPopup" class="modal" hidden role="dialog" aria-modal="true" aria-labelledby="uploadTitle">
			<div class="modal-card">
				<header class="modal-header">
					<h2 id="uploadTitle">Send images <small class="muted">(5 max, 5 MB each)</small></h2>
					<button type="button" class="icon-btn" id="closeUploadPopup" data-close-modal aria-label="Close"><i class="fa fa-times" aria-hidden="true"></i></button>
				</header>
				<label class="dropzone">
					<i class="fa fa-cloud-upload" aria-hidden="true"></i>
					<span>Choose images</span>
					<input type="file" multiple accept="image/png,image/jpeg,image/gif,image/bmp" data-max_length="5" class="upload__inputfile">
				</label>
				<div class="upload__img-wrap"></div>
				<div class="composer composer-modal">
					<button type="button" class="icon-btn third-btn" aria-label="Insert emoji"><i class="fa fa-smile-o" aria-hidden="true"></i></button>
					<textarea id="txtMsgPopup" class="three" rows="1" maxlength="2000" placeholder="Add a message (optional)" aria-label="Image caption"></textarea>
					<button type="button" class="send-btn" id="uploadSendBtn" aria-label="Send images"><i class="fa fa-paper-plane" aria-hidden="true"></i></button>
				</div>
			</div>
		</div>

		<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"
			integrity="sha384-svdnwiXBL+Js+yUneJSRFy24NgGiOfDM/vkQl/vCeVrcbYD6yW8EwPr1FnxTEZsR" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"
			integrity="sha384-clKAk7WaYNQiu/pGVWisoFQcSK2VL7YweoI1gim9/HFbMsRvD2ZtLWNoZoL4df65" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
		<script src="scripts/variables.js"></script>
		<script src="scripts/vanillaEmojiPicker.js"></script>
		<script src="scripts/uploadImage.js"></script>
		<script src="scripts/chatroom.js"></script>
		<script src="scripts/main.js"></script>
	</body>
</html>
