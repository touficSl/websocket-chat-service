package com.ts.messenger.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ts.messenger.authentication.Credentials;
import com.ts.messenger.authentication.Interceptor;
import com.ts.messenger.communication.Message.MessageStatus;
import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.entities.Hotel;
import com.ts.messenger.entities.MessageEntity;
import com.ts.messenger.entities.User;
import com.ts.messenger.services.GuestService;
import com.ts.messenger.services.HotelService;
import com.ts.messenger.services.MessageService;
import com.ts.messenger.services.TimezoneService;
import com.ts.messenger.services.UserService;
import com.ts.messenger.utils.Constants;
import com.ts.messenger.utils.Response;
import com.ts.messenger.utils.Utils;

@RestController
@RequestMapping(path = "/api/")
public class APIsController {

	@Autowired
	Interceptor interceptor;
	
	@Autowired
	MessageService messageService;
	
	@Autowired
	private GuestService guestService;
	
	@Autowired
	TimezoneService timezoneService;
	
	@Autowired
	HotelService hotelService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	MessagingController messagingController;
	
	/**
	 * API returns hotel guests list 
	 * @param authorization
	 * @param hotelId
	 * @param customerId
	 * @param hotelUsername
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/hotel/guests", method = RequestMethod.GET, produces = "application/json")
	public Response hotelGuests(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId, 
			@RequestHeader(name="customerId", required = true) String customerId, 
			@RequestParam(name="hotelUsername", required = true) String hotelUsername) throws Exception {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");
		
		/* check if hotel username exist in utilisateur */
		User user = userService.getByUserLogin(hotelUsername);
		if (user == null) 
			return new Response(false, Constants.ERROR, "Access denied.");

		/* Get hotel timezone */
		String hotelTimezone = getHotelTimeZone(hotelId);
		JSONObject response = new JSONObject();
		response.put("hotelTimezone", hotelTimezone);
		response.put("guestList", guestService.getGuestRooms(hotelUsername, hotelId));
		
		return new Response(true, Constants.SUCCESS, "Hotel guests list returned successfully", response.toString());
	}
	
	/**
	 * API returns limited messages for a specific chat
	 * @param authorization
	 * @param hotelId
	 * @param customerId
	 * @param guestUsername
	 * @param roomNumber
	 * @param fromRow
	 * @param toRow
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/hotel/guest/{guestUsername}/{roomNumber}/messages/{fromRow}/{toRow}", method = RequestMethod.GET, produces = "application/json")
	public Response hotelGuestMessages(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId, 
			@RequestHeader(name="customerId", required = true) String customerId,
			@PathVariable(name="guestUsername", required = true) String guestUsername,
			@PathVariable(name="roomNumber", required = true) String roomNumber,
			@PathVariable(name="fromRow", required = true) String fromRow,
			@PathVariable(name="toRow", required = true) String toRow) throws Exception {

		int fromR = Utils.convertStringToInt(fromRow);
		int toR = Utils.convertStringToInt(toRow);
		if (fromR == -1) fromR = 0;
		if (toR == -1) toR = 50;
		
		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");
		
		/* fetch messaged order by DESC limited */
		List<MessageEntity> messageList = messageService.chatMessages(guestUsername, hotelId, roomNumber, fromR, toR);
		// return them ASC 
		Collections.sort(messageList);
		
		JSONObject response = new JSONObject();
		if (messageList.size() < toR)
			response.put("remainingMessagesCount", 0);
		else
			response.put("remainingMessagesCount", messageService.getAllChatMessages(guestUsername, hotelId, roomNumber).size() - toR);
		response.put("messageList", messageList);
		return new Response(true, Constants.SUCCESS, "Hotel guest messaged returnred successfully", response.toString());
	}

	@RequestMapping(value = "/hotel/guest/logIn", method = RequestMethod.GET, produces = "application/json")
	public Response logIn(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId, 
			@RequestHeader(name="customerId", required = true) String customerId,  
			@RequestParam(name="guestUsername", required = true) String guestUsername, 
			@RequestParam(name="roomNumber", required = true) String roomNumber, 
			@RequestParam(name="roomTypeCode", required = false) String roomTypeCode) throws Exception {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		GuestRoomEntity guestRoomEntity = guestService.checkIfExist(roomNumber, hotelId);
		
		if (guestRoomEntity == null) {
			
			/* Check if guestUsername already exist */
			GuestRoomEntity gre = guestService.checkIfExistGestUsername(guestUsername);
			if (gre != null) {
				return new Response(false, Constants.ERROR, "Access Denied, guestUsername already exist", null);
			}

			/* Create a new row in messenger_guest_room */
			guestRoomEntity = guestService.createGuest(guestUsername, "", roomNumber, roomTypeCode, hotelId);
			
			messagingController.sendHotelPrivateMessage(hotelId, "refresh_guest_list");
		} 
		else if (!guestRoomEntity.getGuestUsername().equals(guestUsername)) {
			return new Response(false, Constants.ERROR, "Access Denied", null);
		}

		/* Get hotel timezone */
		String hotelTimezone = getHotelTimeZone(hotelId);
		
		JSONObject response = new JSONObject();
		response.put("hotelTimezone", hotelTimezone);
		response.put("guestRoom", new JSONObject(guestRoomEntity));
		
		return new Response(true, Constants.SUCCESS, "Guest logged In successfully", response.toString());
	}

	@RequestMapping(value = "/hotel/guest/logOut", method = RequestMethod.GET, produces = "application/json")
	public Response logOut(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId, 
			@RequestHeader(name="customerId", required = true) String customerId, 
			@RequestHeader(name="chatSecurityToken", required = true) String chatSecurityToken, 
			@RequestParam(name="guestUsername", required = true) String guestUsername, 
			@RequestParam(name="roomNumber", required = true) String roomNumber) throws Exception {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		/* Get hotel timezone */
		String hotelTimezone = getHotelTimeZone(hotelId);
		
		int loggedOut = guestService.logOut(hotelId, roomNumber, guestUsername, chatSecurityToken, returnLoggedOutDate(hotelTimezone));
		
		messagingController.sendHotelPrivateMessage(hotelId, "refresh_guest_list");
		
		return new Response(true, Constants.SUCCESS, "Guest logged out successfully", loggedOut);
	}
	

	public String returnLoggedOutDate(String hotelTimeZone) {
		Date currentDate = new Date();
		SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat(Constants.DateTimeFormat);
		String date;
    	try {
			if (hotelTimeZone != null && !hotelTimeZone.trim().equalsIgnoreCase(""))
				simpleDateTimeFormat.setTimeZone(TimeZone.getTimeZone(hotelTimeZone));
			date = simpleDateTimeFormat.format(currentDate);
			
		} catch (Exception e) {
			e.printStackTrace();
			Utils.printLogMessage("APIsController.returnLoggedOutDate", "Timezone error");
			date = simpleDateTimeFormat.format(currentDate);
		}
		return date;
	}

	@RequestMapping(value = "/hotel/guest/assign/{guestUsername}/{roomNumber}", method = RequestMethod.GET, produces = "application/json")
	public Response assignGuest(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId, 
			@RequestHeader(name="customerId", required = true) String customerId, 
			@PathVariable(name="guestUsername", required = true) String guestUsername, 
			@PathVariable(name="roomNumber", required = true) String roomNumber,
			@RequestParam(name="toHotelUsername", required = false) String toHotelUsername) throws Exception {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");
		
		GuestRoomEntity guestRoomEntity = guestService.checkIfExist(roomNumber, hotelId);
		if (!guestRoomEntity.getGuestUsername().equals(guestUsername))
			return new Response(false, Constants.ERROR, "Invalid chat.", null);
		
		if (toHotelUsername == null && guestRoomEntity.getHotelUsername() == null)
			return new Response(false, Constants.ERROR, "Already assigned.", null);
		
		if (toHotelUsername != null) { /* Feature not used yet, to assign a guest to a specific hotel */
			User user = userService.getByUserLogin(toHotelUsername);
			if (user == null)
				return new Response(false, Constants.ERROR, "Invalid tohotelUsername.", null);
			
			guestRoomEntity.setHotelUsername(toHotelUsername);
		} 
		else /* Assigned for all hotels */
			guestRoomEntity.setHotelUsername(null);

		guestRoomEntity = guestService.save(guestRoomEntity);
		
		messagingController.sendHotelPrivateMessage(hotelId, "refresh_guest_list");
		
		return new Response(true, Constants.SUCCESS, "Guest assigned successfully", guestRoomEntity);
	}
	
	@RequestMapping(value = "/chat/status/change", method = RequestMethod.GET, produces = "application/json")
	public Response changeChatStatus(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId, 
			@RequestHeader(name="customerId", required = true) String customerId,
			@RequestParam(name="guestUsername", required = false) String guestUsername, 
			@RequestParam(name="hotelUsername", required = false) String hotelUsername, 
			@RequestParam(name="roomNumber", required = true) String roomNumber) throws Exception {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		GuestRoomEntity guestRoomEntity = guestService.checkIfExist(roomNumber, hotelId);
		if (guestRoomEntity == null)
			return new Response(false, Constants.ERROR, "Invalid chat.", null);
		
		if (guestUsername != null) {

			messageService.updateMessageStatus(roomNumber, guestUsername, guestRoomEntity.getId(), MessageStatus.SEEN.name());
			return new Response(true, Constants.SUCCESS, "Guest has seen the unread messages.", null);
		} 
		else if (hotelUsername != null) {

			messageService.updateMessageStatus(roomNumber, hotelUsername, guestRoomEntity.getId(), MessageStatus.SEEN.name());
			return new Response(true, Constants.SUCCESS, "Hotel has seen the unread messages.", null);
		}

		return new Response(false, Constants.ERROR, "Incorrect request.", null);
	}
	
	/**
	 * Upload max 5 images
	 * in application.properties I added 5 MB size upload file
	 * @param request
	 * @param authorization
	 * @param hotelId
	 * @param customerId
	 * @param customerName
	 * @param image_0
	 * @param image_1
	 * @param image_2
	 * @param image_3
	 * @param image_4
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/upload/image", method = RequestMethod.POST,  consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public Response uploadImage(
			HttpServletRequest request,
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader("hotelId") String hotelId, 
			@RequestHeader("customerId") String customerId,
			@RequestHeader("customerName") String customerName,
			@RequestPart(name = "image_0", required = false) MultipartFile image_0,
			@RequestPart(name = "image_1", required = false) MultipartFile image_1,
			@RequestPart(name = "image_2", required = false) MultipartFile image_2,
			@RequestPart(name = "image_3", required = false) MultipartFile image_3,
			@RequestPart(name = "image_4", required = false) MultipartFile image_4) throws Exception {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		String url = ServletUriComponentsBuilder.fromRequestUri(request)
	            .replacePath(null)
	            .build()
	            .toUriString();
		
		List<String> uploadedImgPaths = new ArrayList<String>();
		
		String imgPathRes = "";
		if (image_0 != null) {
			imgPathRes = handleImageUpload(image_0, customerName, "messengerimg");
			uploadedImgPaths.add(url + imgPathRes.toString().replace("tsl/midd_data/", ""));
		}

		if (image_1 != null) {
			imgPathRes = handleImageUpload(image_1, customerName, "messengerimg");
			uploadedImgPaths.add(url + imgPathRes.toString().replace("tsl/midd_data/", ""));
		}

		if (image_2 != null) {
			imgPathRes = handleImageUpload(image_2, customerName, "messengerimg");
			uploadedImgPaths.add(url + imgPathRes.toString().replace("tsl/midd_data/", ""));
		}

		if (image_3 != null) {
			imgPathRes = handleImageUpload(image_3, customerName, "messengerimg");
			uploadedImgPaths.add(url + imgPathRes.toString().replace("tsl/midd_data/", ""));
		}

		if (image_4 != null) {
			imgPathRes = handleImageUpload(image_4, customerName, "messengerimg");
			uploadedImgPaths.add(url + imgPathRes.toString().replace("tsl/midd_data/", ""));
		}
			
		return new Response(true, Constants.SUCCESS, "Images uploaded successfully", uploadedImgPaths);
	}
	
	public String handleImageUpload(MultipartFile multipartFile, String customerName, String imgName) {
		try {
			if (multipartFile == null) 
				return null;
			if (multipartFile.getInputStream() == null)
				return "Error, Incorrect image.";
			
			String imageName = new SimpleDateFormat("'" + imgName + "_'yyyyMMddHHmmss").format(Utils.getDateByCustomerTimezone(Constants.defaultLogsTimeZone));

			BufferedImage resizedImage = Utils.resizeImage(Integer.valueOf(600), Integer.valueOf(300), ImageIO.read(multipartFile.getInputStream()));

			String path = Credentials.getUploadImagesPath(customerName) + "/" + imageName + ".png";
			
			if (resizedImage != null) {
				File file = new File(path);
				
				try {
					ImageIO.write(resizedImage, "png", file);
					Files.setPosixFilePermissions(Paths.get(path), PosixFilePermissions.fromString("rw-r--r--"));
				} catch (Exception e) {}
				
				file.setReadable(true);
			}
			return path;
		} catch (Exception e1) {
			e1.printStackTrace();
			return "Error while uploading image.";
		}
	}
	
	public String getHotelTimeZone(String hotelId) {

		/* Get hotel entity + validation */
		Hotel hotel = hotelService.getOne(hotelId);
		if (hotel == null) {
			return "-1";
		}

		/* Get hotel timezone */
		String hotelTimezone = timezoneService.getTimezoneById(hotel.getTimezone()) != null ? 
				timezoneService.getTimezoneById(hotel.getTimezone()).getHotelTimezone()
				: null;
				
		return hotelTimezone;
	}
}