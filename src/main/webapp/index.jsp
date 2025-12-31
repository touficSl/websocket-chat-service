<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<%-- <% String WsUrl = getServletContext().getInitParameter("WsUrl"); %> --%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Messenger</title>
		
		<script type="text/javascript" src="scripts/variables.js"></script>
		
		<!-- <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script> -->
		<script src="https://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js" integrity="sha512-iKDtgDyTHjAitUDdLljGhenhPwrbBfqTKWO1mkhSFH3A7blITC9MhYon6SjnMhp4o0rADGw9yAC6EW4t5a4K3g==" crossorigin="anonymous"></script>
		<script type="text/javascript" src="scripts/chatroom.js"></script>
		
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script> 
		
	    <meta name="viewport" content="width=device-width, initial-scale=1">
		<script src="https://code.jquery.com/jquery-1.10.2.min.js"></script>
	    <link href="https://netdna.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
		<link rel="icon" type="image/png" href="images/icon.png">
				
		
		<script type="text/javascript" src="scripts/uploadImage.js"></script>
		
		<script src="https://netdna.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		
		<script type="text/javascript">
			<%-- var wsUri = '<%=WsUrl%>'; --%>
			var wsUri = "https://localhost:8080/messenger/sendmsg";
			var currentUrl = window.location.href;
			if (currentUrl.includes("localhost"))
				wsUri = "http://localhost:8080/messenger/sendmsg";
			var proxy = CreateProxy(wsUri);
			
			document.addEventListener("DOMContentLoaded", function(event) {
				proxy.initiate({
					loadingDiv: document.getElementById('loadingDiv'),
					msgPanel: document.getElementById('msgPanel'),
					txtMsg: document.getElementById('txtMsg'),
					//txtLogin: document.getElementById('txtLogin'),
					msgLabel: document.getElementById('msgLabel'),
					//btnLogin: document.getElementById('btnLogin'),
					msgContainer: document.getElementById('msgContainer'),
					hotelGuestsSideBarId: document.getElementById('hotelGuestsSideBarId'),
					guestsListId: document.getElementById('guestsListId'),
					conversation: document.getElementById('conversation'),
					selectedGuestUsername: document.getElementById('selectedGuestUsername'),
					textInputId: document.getElementById('textInputId'),
					textInputBaseId: document.getElementById('textInputBaseId'),
					closePopup: document.getElementById('closePopup'),
					closeUploadPopup: document.getElementById('closeUploadPopup'),
					muteAllNotifId: document.getElementById('muteAllNotifId'),
					logoutId: document.getElementById('logoutId'),
					msgContainerHotelMobileScreen: document.getElementById('msgContainerHotelMobileScreen'),
					textInputHotelMobileScreen: document.getElementById('textInputHotelMobileScreen'),
					newMessageBack: document.getElementById('newMessageBack'),
					headingCompose: document.getElementById('headingCompose'),
					toGuestMobileScreenId: document.getElementById('toGuestMobileScreenId'),
					msgContainerBase: document.getElementById('msgContainerBase'),
					searchText: document.getElementById('searchText'),
					showPrevMsgs: document.getElementById('showPrevMsgs'),
					previousMsgContainer: document.getElementById('previousMsgContainer'),
					newMsgContainer: document.getElementById('newMsgContainer'),
					assignGuestId: document.getElementById('assignGuestId'),
					txtMsgPopup: document.getElementById('txtMsgPopup')
				});
			});
		</script>
	</head>
	<body onresize="proxy.onresize()">
		<!-- <div id="container">
			<div id="loginPanel">
				<div id="infoLabel">Type a name to join the room</div>
				<div style="padding: 10px;">
					<input id="socket_url" type="text" class="loginInput"/>
					<input id="txtLogin" type="text" class="loginInput" onkeyup="proxy.login_keyup(event)"/>
					<button id="btnLogin" type="button" class="loginInput" onclick="proxy.login()">Login</button>
				</div>
				<div id="errorLabel" style="color:red; text-align:center"></div>
				<div id="loadingLabel" style="color:green; text-align:center">Loading...</div>
			</div>
			<div id="msgPanel" style="display: none">
				<div id="msgContainer" style="overflow: auto;"></div>
				<div id="msgController">
					<textarea id="txtMsg" title="Enter to send message" onkeyup="proxy.sendMessage_keyup(event)" style="height: 20px; width: 100%"></textarea>
					<button style="height: 30px; width: 100px" type="button" onclick="proxy.logout()">Logout</button>
				</div>
			</div>
		</div>  -->
		
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
			<div id="container">
				<div id="loadingDiv">
					<div id="msgLabel" style="text-align:center; font-size:15px"></div>
				</div>
			
				<div class="container app" id="msgPanel" style="display: none">
				  <div class="row app-one">
				    <div class="col-sm-4 side" id="guestsListId">
				      <div class="side-one">
				        <div class="row heading" style="display: flex; justify-content: center;">
				          <div class="col-sm-5 col-xs-3 heading-avatar">
				            <div class="heading-avatar-icon">
				            	<img src="https://localhost:8080/portal/demoapp/room/images/2_ofitel_logo.png">
				              <!-- <img src="https://bootdey.com/img/Content/avatar/avatar1.png"> -->
				            </div>
				          </div>
				          <!-- <div class="col-sm-1 col-xs-1  heading-dot  pull-right">
				            <a class="button" href="#settingsPopup"><i class="fa fa-ellipsis-v fa-2x  pull-right" aria-hidden="true"></i></a>
				          </div> -->
				          <div class="col-sm-2 col-xs-2 heading-compose  pull-right" id="headingCompose" style="width: 0;">
				            <i class="fa fa-comments fa-2x  pull-right" aria-hidden="true" style="visibility:hidden"></i>
				          </div>
				        </div>
				
				        <div class="row searchBox">
				          <div class="col-sm-12 searchBox-inner">
				            <div class="form-group has-feedback">
				              <input id="searchText" type="text" class="form-control" name="searchText" placeholder="Search" onkeyup="proxy.searchGuests_keyup(event)">
				              <span class="glyphicon glyphicon-search form-control-feedback"></span>
				            </div>
				          </div>
				        </div>
				
				        <div class="row sideBar" id="hotelGuestsSideBarId">
				        
				          <!-- Filled dynamically 
				          <div class="row sideBar-body">
				            <div class="col-sm-3 col-xs-3 sideBar-avatar">
				              <div class="avatar-icon">
				                <img src="https://bootdey.com/img/Content/avatar/avatar1.png">
				              </div>
				            </div>
				            <div class="col-sm-9 col-xs-9 sideBar-main">
				              <div class="row">
				                <div class="col-sm-8 col-xs-8 sideBar-name">
				                  <span class="name-meta">name
				                </span>
				                </div>
				                <div class="col-sm-4 col-xs-4 pull-right sideBar-time">
				                  <span class="time-meta pull-right">22:00
				                </span>
				                </div>
				              </div>
				            </div>
				          </div> -->
				          
				        </div>
				      </div>
				
				      <div class="side-two">
				        <div class="row newMessage-heading">
				          <div class="row newMessage-main">
				            <div class="col-sm-1 col-xs-1 newMessage-back" id="newMessageBack">
				              <i class="fa fa-arrow-left" aria-hidden="true" onclick="proxy.removeActiveGuest()"></i>
				            </div>
				            <div class="col-sm-10 col-xs-10 newMessage-title" id="toGuestMobileScreenId">
				            </div> 
				          </div>
				        </div>
				
				        <!-- <div class="row composeBox">
				          <div class="col-sm-12 composeBox-inner">
				            <div class="form-group has-feedback">
				              <input id="composeText" type="text" class="form-control" name="searchText" placeholder="Search People">
				              <span class="glyphicon glyphicon-search form-control-feedback"></span>
				            </div>
				          </div>
				        </div> -->
				
				        <div class="row compose-sideBar">
				         
				         <!-- Should be filled when using device screen for hotels only -->
					      <div class="row message" id="msgContainerHotelMobileScreen">
					      </div>
					      <div id="textInputHotelMobileScreen">
					      </div>
				          <!-- <div class="row sideBar-body">
				            <div class="col-sm-3 col-xs-3 sideBar-avatar">
				              <div class="avatar-icon">
				                <img src="https://bootdey.com/img/Content/avatar/avatar1.png">
				              </div>
				            </div>
				            <div class="col-sm-9 col-xs-9 sideBar-main">
				              <div class="row">
				                <div class="col-sm-8 col-xs-8 sideBar-name">
				                  <span class="name-meta">John Doe
				                </span>
				                </div>
				                <div class="col-sm-4 col-xs-4 pull-right sideBar-time">
				                  <span class="time-meta pull-right">18:18
				                </span>
				                </div>
				              </div>
				            </div>
				          </div> -->
				          
				        </div>
				        
				      </div>
				    </div>
				
				    <div class="col-sm-8 conversation" id= "conversation">
				      <div class="row heading">
				       <!--  <div class="col-sm-2 col-md-1 col-xs-3 heading-avatar">
				          <div class="heading-avatar-icon">
				            <img src="https://bootdey.com/img/Content/avatar/avatar6.png">
				          </div>
				        </div> -->
				        <div class="col-sm-8 col-xs-7 heading-name">
				          <a class="heading-name-meta" id="selectedGuestUsername">
				          	Hotel Guest Relation Chat Room
				          </a>
				          <span class="heading-online">Online</span>
				        </div>
				        <div class="col-sm-1 col-xs-1  heading-dot pull-right">
				            <a class="button" href="#settingsPopup"><i class="fa fa-ellipsis-v fa-2x  pull-right" aria-hidden="true"></i></a>
				        </div>
				      </div>
				
				      <div class="row message" id="msgContainerBase">
					      <div class="row message-body" id="msgContainer">
					        <div class="row message-previous">
					          <div class="col-sm-12 previous">
					            <a onclick="proxy.showPreviousMsgs()" id="showPrevMsgs" name="20"></a>
					          </div>
					        </div> 
					        <span id="previousMsgContainer">
					        </span>
					        <span id="newMsgContainer">
					        </span>
					       <!--  <div class="row message-body">
					          <div class="col-sm-12 message-main-receiver">
					            <div class="receiver">
					              <div class="message-text">
					               Hi, what are you doing?! Hi, what are you doing?! Hi, what are you doing?! Hi, what are you doing?! Hi, what are you doing?! Hi, what are you doing?! Hi, what are you doing?! Hi, what are you doing?!
					              </div>
					              <span class="message-time pull-right">
					                Sun
					              </span>
					            </div>
					          </div>
					        </div>
					
					        <div class="row message-body">
					          <div class="col-sm-12 message-main-sender">
					            <div class="sender">
					              <div class="message-text">
					                I am doing nothing man!
					              </div>
					              <span class="message-time pull-right">
					                Sun
					              </span>
					            </div>
					          </div>
					        </div> -->
					      </div>
				      </div>
				
					<div id="textInputBaseId">
					      <div class="row reply" id="textInputId", style="display:none">
					        <div class="col-sm-1 col-xs-1 reply-emojis">
					        <!-- second-btn for emoji picker  -->
					          <i class="fa fa-smile-o fa-2x  second-btn"></i>
					        </div>
					        <div class="col-sm-1 col-xs-1 reply-emojis">
					         	<a class="button" href="#uploadImgPopup"><i class="fa fa-paperclip fa-2x"></i></a>
					        </div>
					        <div class="col-sm-9 col-xs-9 reply-main">
					        <!-- two for emoji picker  -->
					          <textarea id="txtMsg" title="Enter to send message" onkeyup="proxy.sendMessage_keyup(event)"  class="form-control  two" rows="1" ></textarea>
						
					        </div>
					        <!-- <div class="col-sm-1 col-xs-1 reply-recording">
					          <i class="fa fa-microphone fa-2x" aria-hidden="true"></i>
					        </div> -->
					        <div class="col-sm-1 col-xs-1 reply-send" onclick="proxy.sendMessage_click(event)" >
					          <i class="fa fa-send fa-2x" aria-hidden="true" ></i>
					        </div>
					      </div>
				      </div>
				      
				      
				    </div>
				  </div>
				</div>
			</div>
			
			<div id="settingsPopup" class="overlay" style="z-index: 2000;">
				<div class="popup">
					<h2>Settings</h2>
					<a class="close" id="closePopup" href="#">&times;</a>
					<div class="content">
						<a onclick="proxy.muteAllNotifications()" id="muteAllNotifId">Mute Notifications</a> 
						
						<a onclick="proxy.assignGuest()" id="assignGuestId" style="display:none">Assign guest to other staffs</a> 
						<!-- <br>
						<a>My profile</a> -->
						<br>
						<a onclick="proxy.logout()" id="logoutId">Logout</a>
					</div>
				</div>
			</div>	 
			
			<div id="uploadImgPopup" class="overlay" style="z-index: 2000; overflow: scroll;">
				<div class="popup">
					<h2>Upload Images <i style="font-size:17px">(5 Max)</i></h2>
					<a class="close" id="closeUploadPopup" href="#">&times;</a>
					<div class="content">
					  	<br>
					  	
					  	<div class="upload__box" style="height: auto;">
						  <div class="upload__btn-box">
						    <label class="upload__btn">
						      <p>Upload images</p>
						      <input type="file" multiple="" data-max_length="5" class="upload__inputfile">
						    </label>
						  </div>
						  <div class="upload__img-wrap"></div>
						</div>
					  	
					  	<br>
					  	<div class="col-sm-1 col-xs-1 reply-emojis">
					        <!-- third-btn for emoji picker  -->
					          <i class="fa fa-smile-o fa-2x  third-btn"></i>
					        </div>
					        
				        <div class="col-sm-10 col-xs-10 " style = " padding-left: 2px;">
				        <!-- two for emoji picker  -->
				          <textarea id="txtMsgPopup" title="Add message" onkeyup="proxy.uploadSendMessage_keyup(event)"  class="form-control three" rows="1" ></textarea>
					
				        </div>
				        <!-- <div class="col-sm-1 col-xs-1 reply-recording">
				          <i class="fa fa-microphone fa-2x" aria-hidden="true"></i>
				        </div> -->
				        <div class="col-sm-1 col-xs-1 reply-send" onclick="proxy.uploadSendMessage_click(event)" >
				          <i class="fa fa-send fa-2x" aria-hidden="true" ></i>
				        </div>
					</div>
				</div>
			</div>	 
				
   			<script src="scripts/vanillaEmojiPicker.js"></script> 
    
			<script type="text/javascript">
				$(function(){
				    $(".heading-compose").click(function() {
				      $(".side-two").css({
				        "left": "0"
				      });
				    });
				
				    $(".newMessage-back").click(function() {
				      $(".side-two").css({
				        "left": "-100%"
				      });
				    });
				})
	
		        new EmojiPicker({
		            trigger: [
		                {
		                    selector: '.first-btn',
		                    insertInto: ['.one', '.two'] // '.selector' can be used without array
		                },
		                {
		                    selector: '.second-btn',
		                    insertInto: '.two'
		                },
		                {
		                    selector: '.third-btn',
		                    insertInto: '.three'
		                }
		            ],
		            closeButton: true,
		            //specialButtons: green
		        });
			</script>
		
		<link rel="stylesheet" type="text/css" href="content/styles/site.css">
	</body>
</html>