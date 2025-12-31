# WebSocket Chat Service 💬🏨

A real-time chat application designed for hotel-guest communication, enabling seamless messaging between hotel staff and room guests. Built with Spring Boot WebSocket technology and a modern web frontend.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue.svg)](https://stomp.github.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📋 Overview

This WebSocket-based chat service provides a robust communication platform for hotels to interact with their guests in real-time. Guests can request services, ask questions, or report issues directly from their rooms, while hotel staff can respond instantly through a unified dashboard.

**Use Case:** Hotel staff and room guests communicate in real-time for service requests, inquiries, and support.

## ✨ Features

### 🔄 Real-Time Communication
- **Instant Messaging**: WebSocket-powered real-time chat with zero polling
- **STOMP Protocol**: Reliable message delivery using STOMP over WebSocket
- **Bi-directional Communication**: Full duplex communication between guests and staff
- **Message Persistence**: Chat history stored for future reference

### 🏨 Hotel-Specific Features
- **Room-Based Identification**: Guests identified by room numbers
- **Staff Dashboard**: Centralized interface for hotel staff to manage multiple conversations
- **Service Request Routing**: Messages automatically routed to appropriate departments
- **Guest Authentication**: Secure room-based authentication system
- **Multi-Guest Support**: Handle multiple guest conversations simultaneously

### 🎨 User Interface
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Bootstrap Framework**: Modern, clean interface with Bootstrap 4/5
- **Real-time Updates**: Messages appear instantly without page refresh
- **Typing Indicators**: See when the other party is typing
- **Message Timestamps**: Track conversation timeline
- **Read Receipts**: Know when messages are delivered and read

### 🔒 Security & Performance
- **Secure WebSocket Connections**: WSS (WebSocket Secure) support
- **Session Management**: Secure user sessions with Spring Security
- **Connection Pooling**: Efficient connection management
- **Auto-reconnect**: Automatic reconnection on connection loss
- **CORS Configuration**: Controlled cross-origin resource sharing

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 2.x
- **WebSocket**: Spring WebSocket + STOMP
- **Build Tool**: Maven
- **Java Version**: Java 8+
- **Server**: Embedded Tomcat
- **Messaging**: Spring Messaging

### Frontend
- **HTML5**: Semantic markup
- **CSS3**: Modern styling with animations
- **Bootstrap**: Responsive framework (4.x/5.x)
- **JavaScript**: ES6+ features
- **AJAX**: Asynchronous HTTP requests
- **SockJS**: WebSocket fallback support
- **STOMP.js**: STOMP client library

### DevOps
- **Docker**: Containerization support
- **AWS CodeBuild**: CI/CD integration
- **Maven**: Build automation

## 📁 Project Structure

```
websocket-chat-service/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── hotel/
│       │           └── messenger/
│       │               ├── config/
│       │               │   ├── WebSocketConfig.java
│       │               │   └── SecurityConfig.java
│       │               ├── controller/
│       │               │   ├── ChatController.java
│       │               │   └── MessageController.java
│       │               ├── model/
│       │               │   ├── ChatMessage.java
│       │               │   ├── User.java
│       │               │   └── Room.java
│       │               ├── service/
│       │               │   ├── ChatService.java
│       │               │   └── UserService.java
│       │               └── MessengerApplication.java
│       ├── resources/
│       │   ├── static/
│       │   │   ├── css/
│       │   │   │   └── style.css
│       │   │   ├── js/
│       │   │   │   ├── main.js
│       │   │   │   └── websocket.js
│       │   │   └── index.html
│       │   └── application.properties
│       └── webapp/
│           └── WEB-INF/
├── Dockerfile
├── buildspec.yml
├── pom.xml
└── README.md
```

## 🚀 Installation & Setup

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java JDK 8 or higher**
- **Maven 3.6+**
- **Git**
- **Docker** (optional, for containerized deployment)

### 1. Clone the Repository

```bash
git clone https://github.com/touficSl/websocket-chat-service.git
cd websocket-chat-service
```

### 2. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/messenger

# WebSocket Configuration
spring.websocket.message-size-limit=8192
spring.websocket.send-buffer-size-limit=524288
spring.websocket.send-time-limit=20000

# Logging
logging.level.com.hotel.messenger=DEBUG
logging.level.org.springframework.web.socket=DEBUG

# Session Configuration
server.servlet.session.timeout=30m
```

### 3. Build the Application

```bash
# Clean and build using Maven
mvn clean install

# Skip tests if needed
mvn clean install -DskipTests
```

### 4. Run the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using JAR**
```bash
java -jar target/messenger-1.0.0.jar
```

**Option C: Using the WAR file**
```bash
# Deploy messenger.war to Tomcat or any servlet container
```

### 5. Access the Application

Open your browser and navigate to:
```
http://localhost:8080/messenger
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t websocket-chat-service:latest .
```

### Run Docker Container

```bash
docker run -d \
  -p 8080:8080 \
  --name chat-service \
  websocket-chat-service:latest
```

### Using Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  chat-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    restart: unless-stopped
```

Run with:
```bash
docker-compose up -d
```

## 💻 Usage

### For Hotel Guests

1. **Access the Chat Interface**
   - Navigate to `http://hotel-domain.com/messenger`
   - Enter your room number
   - Enter your name

2. **Start Chatting**
   - Type your message in the input field
   - Click "Send" or press Enter
   - View real-time responses from hotel staff

3. **Service Requests**
   - Request room service
   - Report maintenance issues
   - Ask for information
   - Request amenities

### For Hotel Staff

1. **Login to Staff Dashboard**
   - Navigate to `http://hotel-domain.com/messenger/staff`
   - Login with staff credentials
   - View all active guest conversations

2. **Manage Conversations**
   - Select guest conversation from the list
   - View conversation history
   - Send responses in real-time
   - Mark issues as resolved

3. **Route Messages**
   - Tag conversations by department
   - Assign to specific staff members
   - Set priority levels
   - Add internal notes

## 🔌 WebSocket Endpoints

### Connection Endpoint
```
ws://localhost:8080/messenger/chat
```

### Message Destinations

**Send Message (Client → Server)**
```javascript
stompClient.send("/app/chat.sendMessage", {}, 
  JSON.stringify({
    sender: "Room 101",
    content: "Need extra towels",
    type: "CHAT"
  })
);
```

**Subscribe to Messages (Server → Client)**
```javascript
stompClient.subscribe('/topic/public', function(message) {
  showMessage(JSON.parse(message.body));
});
```

**User Join Notification**
```javascript
stompClient.send("/app/chat.addUser", {},
  JSON.stringify({
    sender: username,
    type: "JOIN"
  })
);
```

## 📡 API Endpoints

### REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/messages` | Get chat history |
| POST | `/api/messages` | Send a new message |
| GET | `/api/rooms` | Get all active rooms |
| GET | `/api/users` | Get online users |
| POST | `/api/auth/login` | Staff authentication |
| DELETE | `/api/session` | Logout user |

### WebSocket Events

| Event | Description |
|-------|-------------|
| `CONNECT` | Client connects to WebSocket |
| `JOIN` | User joins a chat room |
| `CHAT` | Regular chat message |
| `LEAVE` | User leaves chat room |
| `TYPING` | User is typing |
| `ERROR` | Error notification |

## 🔧 Configuration

### WebSocket Configuration

The WebSocket configuration is in `WebSocketConfig.java`:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOrigins("*")
                .withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }
}
```

### Security Configuration

Configure authentication and authorization in `SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/chat/**").permitAll()
                .antMatchers("/staff/**").hasRole("STAFF")
                .anyRequest().authenticated();
    }
}
```

## 🎨 Frontend Integration

### Basic HTML Setup

```html
<!DOCTYPE html>
<html>
<head>
    <title>Hotel Chat Service</title>
    <link rel="stylesheet" href="/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <div id="chat-container">
        <div id="message-area"></div>
        <form id="message-form">
            <input type="text" id="message-input" placeholder="Type a message...">
            <button type="submit">Send</button>
        </form>
    </div>
    <script src="/js/main.js"></script>
</body>
</html>
```

### JavaScript WebSocket Client

```javascript
let stompClient = null;

function connect() {
    const socket = new SockJS('/messenger/chat');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        
        stompClient.subscribe('/topic/public', function(message) {
            showMessage(JSON.parse(message.body));
        });
        
        stompClient.send("/app/chat.addUser",
            {},
            JSON.stringify({sender: username, type: 'JOIN'})
        );
    });
}

function sendMessage() {
    const messageContent = messageInput.value.trim();
    
    if(messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT'
        };
        
        stompClient.send("/app/chat.sendMessage", {}, 
            JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}
```

## 🔍 Troubleshooting

### Common Issues

**WebSocket Connection Fails**
```bash
# Check if port is available
netstat -an | grep 8080

# Check firewall rules
sudo ufw status

# Enable WebSocket in application.properties
spring.websocket.enabled=true
```

**Messages Not Received**
- Verify STOMP subscription paths
- Check browser console for errors
- Ensure SockJS fallback is working
- Verify CORS configuration

**High Memory Usage**
- Implement connection pooling
- Set message size limits
- Configure session timeout
- Enable connection recycling

### Debug Mode

Enable detailed logging:

```properties
logging.level.org.springframework.web.socket=TRACE
logging.level.org.springframework.messaging=TRACE
logging.level.com.hotel.messenger=DEBUG
```

## 📊 Performance Optimization

### Connection Pooling

```java
@Bean
public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    scheduler.setThreadNamePrefix("wss-");
    scheduler.initialize();
    return scheduler;
}
```

### Message Compression

```properties
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,application/json
```

### Caching

```java
@Cacheable("messages")
public List<ChatMessage> getRecentMessages(String roomId) {
    return messageRepository.findTop50ByRoomIdOrderByTimestampDesc(roomId);
}
```

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Manual WebSocket Testing

Use tools like:
- **Postman**: WebSocket client
- **wscat**: CLI WebSocket client
- **Browser DevTools**: Network tab

```bash
# Using wscat
npm install -g wscat
wscat -c ws://localhost:8080/messenger/chat
```

## 🚀 Deployment

### AWS Deployment

The project includes AWS CodeBuild configuration (`buildspec.yml`):

```yaml
version: 0.2

phases:
  build:
    commands:
      - mvn clean package
      - docker build -t websocket-chat-service .
  post_build:
    commands:
      - docker tag websocket-chat-service:latest $ECR_REPO_URI:latest
      - docker push $ECR_REPO_URI:latest
```

### Production Checklist

- [ ] Enable HTTPS/WSS
- [ ] Configure production database
- [ ] Set up load balancer for WebSocket sticky sessions
- [ ] Enable connection pooling
- [ ] Configure monitoring and logging
- [ ] Set up auto-scaling
- [ ] Implement rate limiting
- [ ] Configure CDN for static assets
- [ ] Enable CORS for production domains
- [ ] Set up backup and disaster recovery

## 🗺️ Roadmap

- [ ] Message encryption (end-to-end)
- [ ] File sharing capability
- [ ] Voice/Video call integration
- [ ] Mobile app (iOS/Android)
- [ ] Push notifications
- [ ] Chatbot integration for common requests
- [ ] Analytics dashboard
- [ ] Multi-language support
- [ ] Integration with hotel PMS systems
- [ ] Guest feedback and rating system

## 🐛 Known Issues

- ⚠️ Source code requires modifications (as noted in original README)
- ⚠️ Session persistence needs enhancement for horizontal scaling
- ⚠️ Message history pagination needs optimization for large datasets
- ⚠️ Reconnection logic can be improved for mobile devices

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features
- Keep methods small and focused

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Toufic Sleiman**

- GitHub: [@touficSl](https://github.com/touficSl)
- LinkedIn: [Toufic Sleiman](https://www.linkedin.com/in/toufic-sleiman/)
- Twitter: [@sleiman_toufic](https://twitter.com/sleiman_toufic)
- Freelancer: [TouficSl](https://www.freelancer.com/u/TouficSl)

## 🙏 Acknowledgments

- Spring Framework team for excellent WebSocket support
- Bootstrap team for the responsive framework
- STOMP.js and SockJS developers
- Hotel industry professionals for feature suggestions

## 📧 Support

For support and questions:

- Create an issue in this repository
- Email: touficsleiman.uae@gmail.com
- LinkedIn: Message me directly, 
https://www.linkedin.com/in/toufic-sleiman

## 📸 Documentations

- https://docs.google.com/document/d/150jGXb03iGVY0f1ekDfZ1DIZO6IOwd7JXOXyXRaIito/edit?usp=sharing

---

Made with ❤️ for better hotel-guest communication
