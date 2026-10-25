package com.ts.messenger.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.ts.entities.NCICustomerEntity;
import com.ts.messenger.authentication.Credentials;
import com.ts.messenger.authentication.Interceptor;
import com.ts.messenger.communication.Message;
import com.ts.messenger.communication.Message.MessageStatus;
import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.entities.MessageEntity;
import com.ts.messenger.entities.User;
import com.ts.messenger.services.GuestService;
import com.ts.messenger.services.HotelService;
import com.ts.messenger.services.MessageService;
import com.ts.messenger.services.UserService;
import com.ts.messenger.utils.Constants;
import com.ts.messenger.utils.Response;
import com.ts.messenger.utils.SecureTokens;
import com.ts.messenger.utils.Utils;

@RestController
@RequestMapping(path = "/api/")
public class APIsController {

	private static final Logger LOG = LoggerFactory.getLogger(APIsController.class);

	/* History paging limits */
	private static final int DEFAULT_PAGE_SIZE = 50;
	private static final int MAX_PAGE_SIZE = 5000;

	/* Upload limits */
	private static final int MAX_IMAGE_DIMENSION = 10000;
	private static final long MAX_IMAGE_PIXELS = 40_000_000L;
	private static final Set<String> ALLOWED_IMAGE_FORMATS = new HashSet<>(Arrays.asList("png", "jpeg", "gif", "bmp"));
	private static final Pattern CUSTOMER_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
	/* '.' separates the parts of the chat topic name, so it is not allowed in room numbers */
	private static final Pattern ROOM_NUMBER = Pattern.compile("^[^./\\\\\\p{Cntrl}]{1,64}$");

	@Autowired
	Interceptor interceptor;

	@Autowired
	MessageService messageService;

	@Autowired
	private GuestService guestService;

	@Autowired
	HotelService hotelService;

	@Autowired
	UserService userService;

	@Autowired
	MessagingController messagingController;

	/**
	 * API returns hotel guests list
	 */
	@RequestMapping(value = "/hotel/guests", method = RequestMethod.GET, produces = "application/json")
	public Response hotelGuests(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId,
			@RequestHeader(name="customerId", required = true) String customerId,
			@RequestParam(name="hotelUsername", required = true) String hotelUsername) {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		/* check if hotel username exist in utilisateur */
		User user = Message.isValidName(hotelUsername) ? userService.getByUserLogin(hotelUsername) : null;
		if (user == null)
			return new Response(false, Constants.ERROR, "Access denied.");

		JSONObject response = new JSONObject();
		response.put("hotelTimezone", hotelService.getHotelTimezone(hotelId));
		response.put("guestList", guestService.getGuestRooms(hotelUsername, hotelId));

		return new Response(true, Constants.SUCCESS, "Hotel guests list returned successfully", response.toString());
	}

	/**
	 * API returns limited messages for a specific chat, requires the chat security token
	 */
	@RequestMapping(value = "/hotel/guest/{guestUsername}/{roomNumber}/messages/{fromRow}/{toRow}", method = RequestMethod.GET, produces = "application/json")
	public Response hotelGuestMessages(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId,
			@RequestHeader(name="customerId", required = true) String customerId,
			@RequestHeader(name="chatSecurityToken", required = true) String chatSecurityToken,
			@PathVariable(name="guestUsername", required = true) String guestUsername,
			@PathVariable(name="roomNumber", required = true) String roomNumber,
			@PathVariable(name="fromRow", required = true) String fromRow,
			@PathVariable(name="toRow", required = true) String toRow) {

		int fromR = Utils.convertStringToInt(fromRow);
		int toR = Utils.convertStringToInt(toRow);
		if (fromR < 0) fromR = 0;
		if (toR <= 0) toR = DEFAULT_PAGE_SIZE;
		if (toR > MAX_PAGE_SIZE) toR = MAX_PAGE_SIZE;

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		GuestRoomEntity guestRoom = guestService.findAuthorizedChat(roomNumber, hotelId, guestUsername, chatSecurityToken);
		if (guestRoom == null)
			return new Response(false, Constants.ERROR, "Invalid chat.");

		/* fetch messaged order by DESC limited */
		List<MessageEntity> messageList = new ArrayList<>(messageService.chatMessages(guestUsername, hotelId, roomNumber, fromR, toR));
		// return them ASC
		Collections.sort(messageList);

		/* Same "yyyy-MM-dd HH:mm:ss.S" format as before, expressed in the hotel timezone like live messages */
		SimpleDateFormat createdDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		String hotelTimezone = hotelService.getHotelTimezone(hotelId);
		if (hotelTimezone != null && !hotelTimezone.trim().isEmpty() && !"-1".equals(hotelTimezone))
			createdDateFormat.setTimeZone(TimeZone.getTimeZone(hotelTimezone));

		JSONArray messages = new JSONArray();
		for (MessageEntity messageEntity : messageList)
			messages.put(toClientJson(messageEntity, createdDateFormat));

		JSONObject response = new JSONObject();
		if (messageList.size() < toR)
			response.put("remainingMessagesCount", 0);
		else
			response.put("remainingMessagesCount", Math.max(0, messageService.countChatMessages(guestUsername, hotelId, roomNumber) - ((long) (fromR + 1) * toR)));
		response.put("messageList", messages);
		return new Response(true, Constants.SUCCESS, "Hotel guest messaged returnred successfully", response.toString());
	}

	private JSONObject toClientJson(MessageEntity messageEntity, SimpleDateFormat createdDateFormat) {
		JSONObject json = new JSONObject();
		json.put("id", messageEntity.getId());
		json.put("sender", messageEntity.getSender());
		json.put("receiver", messageEntity.getReceiver());
		json.put("content", messageEntity.getContent());
		json.put("contentType", messageEntity.getContentType());
		json.put("status", messageEntity.getStatus());
		json.put("roomNumber", messageEntity.getRoomNumber());
		if (messageEntity.getCreatedDate() != null)
			json.put("createdDate", createdDateFormat.format(messageEntity.getCreatedDate()));
		return json;
	}

	@RequestMapping(value = "/hotel/guest/logIn", method = RequestMethod.GET, produces = "application/json")
	public Response logIn(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId,
			@RequestHeader(name="customerId", required = true) String customerId,
			@RequestParam(name="guestUsername", required = true) String guestUsername,
			@RequestParam(name="roomNumber", required = true) String roomNumber,
			@RequestParam(name="roomTypeCode", required = false) String roomTypeCode) {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		if (!Message.isValidName(guestUsername) || !ROOM_NUMBER.matcher(roomNumber).matches()
				|| (roomTypeCode != null && roomTypeCode.length() > 32))
			return new Response(false, Constants.ERROR, "Invalid parameters.", null);

		GuestRoomEntity guestRoomEntity = guestService.checkIfExist(roomNumber, hotelId);

		if (guestRoomEntity == null) {

			/* Check if guestUsername already exist */
			GuestRoomEntity gre = guestService.checkIfExistGestUsername(guestUsername);
			if (gre != null) {
				return new Response(false, Constants.ERROR, "Access Denied, guestUsername already exist", null);
			}

			/* Create a new row in messenger_guest_room */
			guestRoomEntity = guestService.createGuest(guestUsername, "", roomNumber, roomTypeCode, hotelId);
			if (guestRoomEntity == null)
				return new Response(false, Constants.ERROR, "Unable to create the chat.", null);

			messagingController.sendHotelPrivateMessage(hotelId, "refresh_guest_list");
		}
		else if (!guestRoomEntity.getGuestUsername().equals(guestUsername)) {
			return new Response(false, Constants.ERROR, "Access Denied", null);
		}

		JSONObject response = new JSONObject();
		response.put("hotelTimezone", hotelService.getHotelTimezone(hotelId));
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
			@RequestParam(name="roomNumber", required = true) String roomNumber) {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		if (guestService.findAuthorizedChat(roomNumber, hotelId, guestUsername, chatSecurityToken) == null)
			return new Response(false, Constants.ERROR, "Invalid chat.", null);

		int loggedOut = guestService.logOut(hotelId, roomNumber, guestUsername, chatSecurityToken, returnLoggedOutDate(hotelService.getHotelTimezone(hotelId)));

		messagingController.sendHotelPrivateMessage(hotelId, "refresh_guest_list");

		return new Response(true, Constants.SUCCESS, "Guest logged out successfully", loggedOut);
	}


	public String returnLoggedOutDate(String hotelTimeZone) {
		SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat(Constants.DateTimeFormat);
		if (hotelTimeZone != null && !hotelTimeZone.trim().isEmpty() && !"-1".equals(hotelTimeZone))
			simpleDateTimeFormat.setTimeZone(TimeZone.getTimeZone(hotelTimeZone));
		return simpleDateTimeFormat.format(new Date());
	}

	@RequestMapping(value = "/hotel/guest/assign/{guestUsername}/{roomNumber}", method = RequestMethod.GET, produces = "application/json")
	public Response assignGuest(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId,
			@RequestHeader(name="customerId", required = true) String customerId,
			@RequestHeader(name="chatSecurityToken", required = true) String chatSecurityToken,
			@PathVariable(name="guestUsername", required = true) String guestUsername,
			@PathVariable(name="roomNumber", required = true) String roomNumber,
			@RequestParam(name="toHotelUsername", required = false) String toHotelUsername) {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		GuestRoomEntity guestRoomEntity = guestService.findAuthorizedChat(roomNumber, hotelId, guestUsername, chatSecurityToken);
		if (guestRoomEntity == null)
			return new Response(false, Constants.ERROR, "Invalid chat.", null);

		if (toHotelUsername == null && guestRoomEntity.getHotelUsername() == null)
			return new Response(false, Constants.ERROR, "Already assigned.", null);

		if (toHotelUsername != null) { /* Feature not used yet, to assign a guest to a specific hotel */
			User user = Message.isValidName(toHotelUsername) ? userService.getByUserLogin(toHotelUsername) : null;
			if (user == null)
				return new Response(false, Constants.ERROR, "Invalid tohotelUsername.", null);

			guestRoomEntity.setHotelUsername(toHotelUsername);
		}
		else /* Assigned for all hotels */
			guestRoomEntity.setHotelUsername(null);

		if (guestService.save(guestRoomEntity) == null)
			return new Response(false, Constants.ERROR, "Unable to assign guest.", null);

		messagingController.sendHotelPrivateMessage(hotelId, "refresh_guest_list");

		return new Response(true, Constants.SUCCESS, "Guest assigned successfully", null);
	}

	@RequestMapping(value = "/chat/status/change", method = RequestMethod.GET, produces = "application/json")
	public Response changeChatStatus(
			@RequestHeader(name="authorization", required = true) String authorization,
			@RequestHeader(name="hotelId", required = true) String hotelId,
			@RequestHeader(name="customerId", required = true) String customerId,
			@RequestHeader(name="chatSecurityToken", required = true) String chatSecurityToken,
			@RequestParam(name="guestUsername", required = false) String guestUsername,
			@RequestParam(name="hotelUsername", required = false) String hotelUsername,
			@RequestParam(name="roomNumber", required = true) String roomNumber) {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		GuestRoomEntity guestRoomEntity = guestService.checkIfExist(roomNumber, hotelId);
		if (guestRoomEntity == null || !SecureTokens.equals(guestRoomEntity.getSecurityToken(), chatSecurityToken))
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
	 * Upload max 5 images (5 MB each, see application.properties)
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
			@RequestPart(name = "image_4", required = false) MultipartFile image_4) {

		int customerAuth = interceptor.preHandle(authorization, customerId, hotelId);
		if (customerAuth != Constants.SUCCESS)
			return new Response(false, customerAuth, "Authentication error");

		/* customerName builds the storage path: it must be safe and belong to the authenticated customer */
		if (!isCustomerNameOfCustomer(customerName, customerId))
			return new Response(false, Constants.ERROR, "Invalid customerName.");

		String url = ServletUriComponentsBuilder.fromRequestUri(request)
	            .replacePath(null)
	            .build()
	            .toUriString();

		List<String> uploadedImgPaths = new ArrayList<String>();
		for (MultipartFile image : Arrays.asList(image_0, image_1, image_2, image_3, image_4)) {
			if (image == null || image.isEmpty())
				continue;
			String imgPathRes = handleImageUpload(image, customerName, "messengerimg");
			if (imgPathRes != null)
				uploadedImgPaths.add(url + imgPathRes.replace("tsl/midd_data/", ""));
		}

		if (uploadedImgPaths.isEmpty())
			return new Response(false, Constants.ERROR, "Error, no valid image uploaded.");

		return new Response(true, Constants.SUCCESS, "Images uploaded successfully", uploadedImgPaths);
	}

	private boolean isCustomerNameOfCustomer(String customerName, String customerId) {
		if (customerName == null || !CUSTOMER_NAME.matcher(customerName).matches())
			return false;
		try {
			NCICustomerEntity customer = Utils.getCustomerById(customerId);
			return customer != null && customer.getName() != null && customer.getName().equalsIgnoreCase(customerName);
		} catch (Exception e) {
			LOG.warn("Unable to resolve customer {}", customerId);
			return false;
		}
	}

	/**
	 * Decodes, resizes and re-encodes the image as PNG (strips any embedded payload/metadata).
	 * @return the stored file path, null on failure
	 */
	public String handleImageUpload(MultipartFile multipartFile, String customerName, String imgName) {
		try {
			BufferedImage image = readImageSafely(multipartFile);
			if (image == null)
				return null;

			BufferedImage resizedImage = Utils.resizeImage(Integer.valueOf(600), Integer.valueOf(300), image);
			if (resizedImage == null)
				return null;

			Path directory = Paths.get(Credentials.getUploadImagesPath(customerName)).normalize();
			Files.createDirectories(directory);

			String imageName = imgName + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(Utils.getDateByCustomerTimezone(Constants.defaultLogsTimeZone))
					+ "_" + UUID.randomUUID().toString().replace("-", "") + ".png";
			Path path = directory.resolve(imageName).normalize();
			if (!path.startsWith(directory))
				return null;

			File file = path.toFile();
			if (!ImageIO.write(resizedImage, "png", file))
				return null;

			try {
				Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-r--r--"));
			} catch (UnsupportedOperationException e) {
				file.setReadable(true, false);
			}

			return path.toString().replace('\\', '/');
		} catch (Exception e) {
			LOG.warn("Image upload failed: {}", e.getMessage());
			return null;
		}
	}

	/** Checks format and dimensions before decoding to avoid decompression bombs */
	private BufferedImage readImageSafely(MultipartFile multipartFile) throws Exception {
		try (InputStream input = multipartFile.getInputStream();
				ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
			if (imageInput == null)
				return null;

			Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
			if (!readers.hasNext())
				return null;

			ImageReader reader = readers.next();
			try {
				if (!ALLOWED_IMAGE_FORMATS.contains(reader.getFormatName().toLowerCase()))
					return null;

				reader.setInput(imageInput, true, true);
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);
				if (width <= 0 || height <= 0 || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION
						|| (long) width * height > MAX_IMAGE_PIXELS)
					return null;

				return reader.read(0);
			} finally {
				reader.dispose();
			}
		}
	}
}
