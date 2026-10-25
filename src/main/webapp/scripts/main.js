/* Page bootstrap: wires the DOM to the chat proxy (no inline handlers, CSP friendly) */
(function() {

	var body = document.body;
	var proxy = CreateProxy({
		basePath: body.dataset.basePath || '/',
		wsUrl: body.dataset.wsUrl,
		authUrl: (body.dataset.authUrl || '').replace(/\/+$/, '')
	});
	window.proxy = proxy;

	var $ = function(id) { return document.getElementById(id); };

	document.addEventListener('DOMContentLoaded', function() {

		var elements = {
			app: $('msgPanel'),
			loadingDiv: $('loadingDiv'),
			loadingSpinner: $('loadingSpinner'),
			msgPanel: $('msgPanel'),
			msgLabel: $('msgLabel'),
			txtMsg: $('txtMsg'),
			msgContainer: $('msgContainer'),
			hotelGuestsSideBarId: $('hotelGuestsSideBarId'),
			guestsListId: $('guestsListId'),
			guestCount: $('guestCount'),
			guestListEmpty: $('guestListEmpty'),
			conversation: $('conversation'),
			conversationAvatar: $('conversationAvatar'),
			connectionStatus: $('connectionStatus'),
			selectedGuestUsername: $('selectedGuestUsername'),
			textInputId: $('textInputId'),
			settingsPopup: $('settingsPopup'),
			uploadImgPopup: $('uploadImgPopup'),
			muteAllNotifId: $('muteAllNotifId'),
			logoutId: $('logoutId'),
			assignGuestId: $('assignGuestId'),
			newMessageBack: $('newMessageBack'),
			msgContainerBase: $('msgContainerBase'),
			searchText: $('searchText'),
			showPrevMsgs: $('showPrevMsgs'),
			previousMsgContainer: $('previousMsgContainer'),
			newMsgContainer: $('newMsgContainer'),
			emptyState: $('emptyState'),
			emptyStateText: $('emptyStateText'),
			txtMsgPopup: $('txtMsgPopup'),
			uploadSendBtn: $('uploadSendBtn'),
			toast: $('toast')
		};

		ImageUpload.init({ notify: proxy.showToast });

		elements.txtMsg.addEventListener('keydown', function(e) { proxy.sendMessage_keyup(e); });
		elements.txtMsg.addEventListener('input', function() { proxy.autoGrow(elements.txtMsg); });
		$('sendBtn').addEventListener('click', function() { proxy.sendMessage(); });

		elements.searchText.addEventListener('input', function() { proxy.searchGuests_keyup(); });
		elements.showPrevMsgs.addEventListener('click', function() { proxy.showPreviousMsgs(); });
		elements.newMessageBack.addEventListener('click', function() { proxy.removeActiveGuest(); });

		$('settingsBtn').addEventListener('click', function() { proxy.openModal(elements.settingsPopup); });
		$('attachBtn').addEventListener('click', function() { proxy.openModal(elements.uploadImgPopup); });
		elements.muteAllNotifId.addEventListener('click', function() { proxy.muteAllNotifications(); });
		elements.assignGuestId.addEventListener('click', function() { proxy.assignGuest(); });
		elements.logoutId.addEventListener('click', function() { proxy.logout(); });

		elements.uploadSendBtn.addEventListener('click', function() { proxy.uploadSendMessage_click(); });
		elements.txtMsgPopup.addEventListener('keydown', function(e) { proxy.uploadSendMessage_keyup(e); });

		/* modal close: X button, backdrop click, Escape */
		[elements.settingsPopup, elements.uploadImgPopup].forEach(function(modal) {
			modal.addEventListener('click', function(e) {
				if (e.target === modal || e.target.closest('[data-close-modal]')) proxy.closeModal(modal);
			});
		});
		document.addEventListener('keydown', function(e) {
			if (e.key !== 'Escape' || document.querySelector('.fg-emoji-container')) return;
			proxy.closeModal(elements.settingsPopup);
			proxy.closeModal(elements.uploadImgPopup);
		});

		new EmojiPicker({
			trigger: [
				{ selector: '.second-btn', insertInto: '.two' },
				{ selector: '.third-btn', insertInto: '.three' }
			],
			closeButton: true
		});

		proxy.initiate(elements);
	});
})();
