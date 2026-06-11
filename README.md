# Art Steganography

## Descripción del Proyecto

**Art Steganography** es una aplicación web desarrollada en Java con Spring Boot que combina técnicas de **criptografía** y **esteganografía** para ocultar mensajes secretos dentro de imágenes. El proyecto implementa un sistema de autenticación seguro con **WebAuthn** (autenticación biométrica) y **jBCrypt** (hash de contraseñas), además de funcionalidades de **OCR** (Reconocimiento Óptico de Caracteres) y conversión de imágenes a **arte ASCII**.

### ¿Para qué sirve?

Este proyecto permite a los usuarios:
- **Cifrar y descifrar** mensajes de texto usando AES-256-GCM
- **Comprimir y descomprimir** datos con GZIP
- **Ocultar mensajes** en imágenes usando técnica LSB (Least Significant Bit)
- **Extraer mensajes** ocultos de imágenes
- **Extraer texto** de imágenes mediante OCR (Tesseract)
- **Convertir imágenes** a arte ASCII
- **Autenticarse** mediante contraseña o biometría (WebAuthn)

---

##  Tecnologías y Librerías

### Backend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 | Lenguaje de programación principal |
| **Spring Boot** | 3.5.0 | Framework de aplicación |
| **Spring Web** | - | Para APIs REST y MVC |
| **Spring Data JPA** | - | ORM y persistencia de datos |
| **Thymeleaf** | - | Motor de plantillas HTML |
| **MySQL Connector** | - | Conector a base de datos |

### Seguridad
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **jBCrypt** | 0.4 | Hash de contraseñas con salt |
| **WebAuthn Server Core** | 2.5.0 | Autenticación biométrica (Yubico) |

### Procesamiento de Imágenes
| Librería | Versión | Propósito |
|----------|---------|-----------|
| **Tess4j** | 5.11.0 | OCR (Tesseract) para extracción de texto |

### Frontend
- **HTML5/CSS3** - Estructura y estilos
- **JavaScript (ES6+)** - Interactividad
- **Canvas API** - Efectos visuales Matrix
- **Fetch API** - Comunicación asíncrona

---

## Estructura del Proyecto

```
Art_Steganography/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/Pixel_To_Art_And_Steganography/
│   │   │       └── Art_Steganography/
│   │   │           ├── ArtSteganographyApplication.java    # Punto de entrada
│   │   │           ├── config/
│   │   │           │   └── WebAuthnConfig.java             # Configuración WebAuthn
│   │   │           ├── domain/
│   │   │           │   ├── User.java                       # Entidad Usuario
│   │   │           │   └── WebAuthnCredential.java           # Entidad Credencial
│   │   │           ├── dto/
│   │   │           │   └── RegisterForm.java               # DTO para registro
│   │   │           ├── repository/
│   │   │           │   ├── UserRepository.java             # Repositorio Usuario
│   │   │           │   ├── WebAuthnCredentialRepository.java
│   │   │           │   └── WebAuthnCredentialAdapter.java   # Adaptador WebAuthn
│   │   │           ├── service/
│   │   │           │   ├── ArtService.java                 # OCR y conversión ASCII
│   │   │           │   ├── AuthService.java                # Lógica de autenticación
│   │   │           │   ├── CryptoService.java              # Criptografía AES/GZIP
│   │   │           │   ├── StegoService.java               # Esteganografía LSB
│   │   │           │   └── WebAuthnService.java            # Servicio WebAuthn
│   │   │           └── web/
│   │   │               ├── api/
│   │   │               │   └── WebAuthApiController.java     # API REST WebAuthn
│   │   │               └── controller/
│   │   │                   ├── AsciiController.java          # Controlador ASCII
│   │   │                   ├── AuthController.java           # Controlador Auth
│   │   │                   ├── CryptoController.java         # Controlador Crypto
│   │   │                   ├── CryptoStegoController.java    # Controlador Stego+Crypto
│   │   │                   ├── DashboardController.java      # Controlador Dashboard
│   │   │                   ├── ProfileController.java        # Controlador Perfil
│   │   │                   └── StegoController.java          # Controlador Stego
│   │   └── resources/
│   │       ├── application.yml                             # Configuración BD
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── art.css                             # Estilos módulo Art
│   │       │   │   ├── crypto.css                          # Estilos módulo Crypto
│   │       │   │   ├── dashboard.css                       # Estilos dashboard
│   │       │   │   ├── stego.css                           # Estilos módulo Stego
│   │       │   │   └── styles.css                          # Estilos globales
│   │       │   ├── js/
│   │       │   │   └── WebAuth.js                          # Utilidades WebAuthn
│   │       │   └── images/
│   │       │       └── logo.jpg                            # Logo aplicación
│   │       └── templates/
│   │           ├── auth/
│   │           │   ├── login.html                          # Página login
│   │           │   ├── registro.html                       # Página registro
│   │           │   └── security.html                       # Centro seguridad
│   │           ├── dashboard/
│   │           │   └── dashboard.html                      # Dashboard principal
│   │           ├── fragments/
│   │           │   ├── navbar.html                         # Barra navegación
│   │           │   └── footer.html                         # Pie página
│   │           ├── layout/
│   │           │   └── base.html                           # Layout base Thymeleaf
│   │           └── modules/
│   │               ├── Art.html                            # Módulo OCR
│   │               ├── AsciiArt.html                       # Módulo ASCII Art
│   │               ├── crypto.html                         # Módulo Criptografía
│   │               └── Steganography.html                  # Módulo Esteganografía
│   └── test/
│       └── java/
│           └── ArtSteganographyApplicationTests.java
├── pom.xml                                                 # Dependencias Maven
├── mvnw                                                    # Maven Wrapper (Unix)
├── mvnw.cmd                                                # Maven Wrapper (Windows)
└── tessdata/
    └── eng.traineddata                                     # Modelo Tesseract inglés
```

---

## Módulos y Funcionalidades

### 1. Módulo de Criptografía (`/crypto`)

Ubicado en: [`CryptoController.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/web/controller/CryptoController.java)

#### Operaciones disponibles:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/crypto` | GET | Muestra la interfaz de criptografía |
| `/crypto/cifrar` | POST | Cifra texto con AES-256-GCM + GZIP |
| `/crypto/descifrar` | POST | Descifra texto cifrado |
| `/crypto/comprimir` | POST | Comprime texto con GZIP |
| `/crypto/descomprimir` | POST | Descomprime texto GZIP |

#### Parámetros:
- `texto` - Texto plano a cifrar/comprimir
- `textoCifrado` - Texto cifrado en Base64
- `textoComprimido` - Texto comprimido en Base64
- `password` - Contraseña para cifrado/descifrado

---

### 2. Módulo de Esteganografía (`/stego`)

Ubicado en: [`CryptoStegoController.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/web/controller/CryptoStegoController.java)

#### Operaciones disponibles:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/stego` | GET | Muestra la interfaz de esteganografía |
| `/stego/hide` | POST | Oculta mensaje en imagen (cifrado + LSB) |
| `/stego/extract` | POST | Extrae mensaje de imagen (LSB + descifrado) |
| `/stego/download` | GET | Descarga imagen con mensaje oculto |
| `/stego/download-text` | GET | Descarga texto extraído |

#### Parámetros:
- `imageFile` - Archivo de imagen (PNG recomendado)
- `message` - Mensaje a ocultar
- `password` - Contraseña de cifrado

---

### 3. Módulo OCR - Imagen a Texto (`/modules/ascii`)

Ubicado en: [`AsciiController.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/web/controller/AsciiController.java)

#### Operaciones disponibles:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/modules/ascii` | GET | Muestra interfaz OCR |
| `/modules/ascii` | POST | Extrae texto de imagen con Tesseract |
| `/modules/ascii/art` | GET | Muestra interfaz ASCII Art |
| `/modules/ascii/art` | POST | Convierte imagen a arte ASCII |

---

### 4. Módulo de Autenticación (`/auth` y `/webauthn`)

Ubicado en: [`AuthController.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/web/controller/AuthController.java) y [`WebAuthApiController.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/web/api/WebAuthApiController.java)

#### Operaciones disponibles:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/auth/login` | GET | Muestra formulario de login |
| `/auth/login` | POST | Autenticación con email/password |
| `/auth/register` | GET | Muestra formulario de registro |
| `/auth/register` | POST | Registro de nuevo usuario |
| `/auth/logout` | POST | Cierra sesión |
| `/webauthn/register/options` | GET | Inicia registro WebAuthn |
| `/webauthn/register/finish` | POST | Finaliza registro WebAuthn |
| `/webauthn/login/options` | GET | Inicia login WebAuthn |
| `/webauthn/login/finish` | POST | Finaliza login WebAuthn |

---

## 🔧 Servicios y Métodos

### CryptoService

Ubicado en: [`CryptoService.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/service/CryptoService.java)

```java
@Service
public class CryptoService {
    // Constantes
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;
}
```

#### Métodos principales:

| Método | Parámetros | Retorno | Descripción |
|--------|------------|---------|-------------|
| `generarClave(String password)` | password | `SecretKey` | Genera clave AES-256 |
| `generarIV()` | - | `byte[]` | Genera IV aleatorio (12 bytes) |
| `comprimir(byte[] datos)` | datos | `byte[]` | Comprime con GZIP |
| `descomprimir(byte[] datosComprimidos)` | datos | `byte[]` | Descomprime GZIP |
| `cifrar(byte[] datos, SecretKey clave)` | datos, clave | `byte[]` | Cifra con AES-256-GCM (IV + datos) |
| `descifrar(byte[] datosCifradosConIV, SecretKey clave)` | datos, clave | `byte[]` | Descifra AES-256-GCM |
| `comprimirYCifrarV2(String textoPlano, String password)` | texto, password | `String` | Flujo completo: comprimir + cifrar (Base64) |
| `descifrarYDescomprimirV2(String datosCifradosBase64, String password)` | datos, password | `String` | Flujo inverso: descifrar + descomprimir |
| `generarClaveDesdePassword(String password)` | password | `SecretKey` | Deriva clave desde password (SHA-256) |
```

### StegoService

Ubicado en: [`StegoService.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/service/StegoService.java)

```java
@Service
public class StegoService {
    private static final String END_DELIMITER = "ENDSTEGO";
    private static final int BITS_PER_BYTE = 8;
    private static final int CHANNELS_PER_PIXEL = 3; // RGB
}
```

#### Métodos principales:

| Método | Parámetros | Retorno | Descripción |
|--------|------------|---------|-------------|
| `hideMessage(MultipartFile imageFile, String message)` | imagen, mensaje | `BufferedImage` | Oculta mensaje usando LSB en canales RGB |
| `extractMessage(MultipartFile imageFile)` | imagen | `String` | Extrae mensaje oculto (LSB) |
| `bytesToBits(byte[] bytes)` | bytes | `List<Integer>` | Convierte bytes a lista de bits (0/1) |
| `bitsToBytes(List<Integer> bits)` | bits | `byte[]` | Convierte bits a bytes |
| `imageToBytes(BufferedImage image)` | imagen | `byte[]` | Convierte imagen a PNG bytes |
| `bytesToImage(byte[] imageBytes)` | bytes | `BufferedImage` | Convierte bytes a imagen |

**Algoritmo LSB:**
- Modifica los bits menos significativos (LSB) de los canales RGB
- Cada píxel puede almacenar 3 bits (R, G, B)
- Usa delimitador `ENDSTEGO` para marcar el final del mensaje

---

### ArtService

Ubicado en: [`ArtService.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/service/ArtService.java)

#### Métodos principales:

| Método | Parámetros | Retorno | Descripción |
|--------|------------|---------|-------------|
| `extraerTextoDeImagen(MultipartFile imagen)` | imagen | `String` | OCR con Tesseract |
| `convertirImagenAAscii(MultipartFile archivoImagen)` | imagen | `String` | Convierte imagen a arte ASCII |
| `limpiarTexto(String texto)` | texto | `String` | Limpia texto extraído (privado) |

**Caracteres ASCII usados (de más oscuro a más claro):**
```
@#W$9876543210?!abc;:+=-,._
```

---

### AuthService

Ubicado en: [`AuthService.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/service/AuthService.java)

#### Métodos principales:

| Método | Parámetros | Retorno | Descripción |
|--------|------------|---------|-------------|
| `authenticate(String email, String password)` | email, password | `Optional<String>` | Autentica usuario con jBCrypt |
| `registerUser(RegisterForm form)` | form | `void` | Registra nuevo usuario |

---

### WebAuthnService

Ubicado en: [`WebAuthnService.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/service/WebAuthnService.java)

#### Métodos principales:

| Método | Parámetros | Retorno | Descripción |
|--------|------------|---------|-------------|
| `startRegistration(String email, String nombre, HttpSession session)` | email, nombre, session | `PublicKeyCredentialCreationOptions` | Inicia registro de credencial |
| `finishRegistration(String responseJson, HttpSession session)` | response, session | `void` | Finaliza registro y guarda credencial |
| `startLogin(String email, HttpSession session)` | email, session | `AssertionRequest` | Inicia proceso de login |
| `finishLogin(String responseJson, HttpSession session)` | response, session | `String` | Finaliza login y retorna username |

---

## Modelos de Dominio

### User

Ubicado en: [`User.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/domain/User.java)

```java
@Entity
@Table(name = "usuarios")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_usuario;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String contrasena;
}
```

### WebAuthnCredential

Ubicado en: [`WebAuthnCredential.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/domain/WebAuthnCredential.java)

```java
@Entity
@Table(name = "credenciales_webauthn")
@Data
public class WebAuthnCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User usuario;
    
    @Column(name = "credential_id", nullable = false, columnDefinition = "VARBINARY(255)")
    private byte[] credentialId;
    
    @Column(name = "public_key_cose", nullable = false, columnDefinition = "BLOB")
    private byte[] publicKeyCose;
    
    @Column(name = "sign_count", nullable = false)
    private long signCount;
    
    @Column(name = "aaguid", columnDefinition = "BINARY(16)")
    private byte[] aaguid;
    
    @Column(name = "nombre_dispositivo", length = 100)
    private String nombreDispositivo;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
}
```

---

## Repositorios

### UserRepository

Ubicado en: [`UserRepository.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/repository/UserRepository.java)

```java
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### WebAuthnCredentialRepository

Ubicado en: [`WebAuthnCredentialRepository.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/repository/WebAuthnCredentialRepository.java)

```java
public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, Long> {
    List<WebAuthnCredential> findByUsuario(User usuario);
    Optional<WebAuthnCredential> findByCredentialId(byte[] credentialId);
    boolean existsByUsuario(User usuario);
    long countByUsuario(User usuario);
}
```

---

## ⚙️ Configuración

### application.yml

Ubicado en: [`application.yml`](src/main/resources/application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://b7uqzz99t6agtndxtdcm-mysql.services.clever-cloud.com:3306/b7uqzz99t6agtndxtdcm?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: uv7et9mdsf9rvko9
    password: C1LcuGgBkZeHFJOgnP08
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 3
      minimum-idle: 2
      connection-timeout: 30000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
```

### WebAuthnConfig

Ubicado en: [`WebAuthnConfig.java`](src/main/java/com/Pixel_To_Art_And_Steganography/Art_Steganography/config/WebAuthnConfig.java)

```java
@Configuration
public class WebAuthnConfig {
    @Bean
    public RelyingParty relyingParty(WebAuthnCredentialAdapter adapter) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id("localhost")
                .name("Art Steganography")
                .build();
        
        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(adapter)
                .origins(Set.of("http://localhost:8080"))
                .build();
    }
}
```

---

## Flujos de Trabajo

### Flujo 1: Cifrado y Esteganografía (Ocultar Mensaje)

```
Texto Original
     ↓
[Comprimir con GZIP]
     ↓
[Cifrar con AES-256-GCM]
     ↓
[Codificar en Base64]
     ↓
[Ocultar en Imagen (LSB)]
     ↓
Imagen con Mensaje Oculto
```

### Flujo 2: Extracción y Descifrado (Extraer Mensaje)

```
Imagen con Mensaje Oculto
     ↓
[Extraer Bits (LSB)]
     ↓
[Decodificar de Base64]
     ↓
[Descifrar con AES-256-GCM]
     ↓
[Descomprimir con GZIP]
     ↓
Texto Original
```

### Flujo 3: Autenticación WebAuthn

```
Usuario ingresa email
     ↓
[GET /webauthn/login/options]
     ↓
Navegador solicita credencial (Huella/FaceID)
     ↓
[POST /webauthn/login/finish]
     ↓
Servidor verifica firma
     ↓
Sesión creada
```

---

## Seguridad

### Hash de Contraseñas (jBCrypt)

```java
// Registro
String contrasenaHasheada = BCrypt.hashpw(form.getContrasena(), BCrypt.gensalt(12));

// Login
userRepository.findByEmail(email)
    .filter(user -> BCrypt.checkpw(password, user.getContrasena()))
```

- Factor de trabajo: 12 (2^12 iteraciones)
- Salt automático generado por `BCrypt.gensalt()`

### Cifrado AES-256-GCM

- **Algoritmo:** AES/GCM/NoPadding
- **Tamaño clave:** 256 bits
- **Tag de autenticación:** 128 bits
- **IV:** 12 bytes aleatorios (GCM recomendado)
- **Ventaja:** Autenticación + Confidencialidad

### WebAuthn

- **Proveedor:** Yubico WebAuthn Server Core 2.5.0
- **Origen:** http://localhost:8080
- **Protección anti-replay:** signCount
- **Soporte:** Passkey, Huella dactilar, FaceID

---

## Instalación y Ejecución

### Prerrequisitos

- Java 21 JDK
- Maven 3.8+
- MySQL 8.0+ (o usar la BD en Clever Cloud configurada)
- Tesseract OCR (opcional, se incluye modelo en `tessdata/`)

### Pasos de instalación

1. **Clonar el repositorio:**
```bash
git clone <url-repositorio>
cd Art_Steganography
```

2. **Ejecutar con Maven Wrapper:**
```bash
./mvnw spring-boot:run
```

3. **O compilar y ejecutar:**
```bash
./mvnw clean package
java -jar target/Art_Steganography-0.0.1-SNAPSHOT.jar
```

4. **Acceder en navegador:**
```
http://localhost:8080
```

### Credenciales de prueba

```
Email: admin@example.com
Password: admin123
```

---

## Dependencias (pom.xml)

```xml
<!-- Spring Web MVC -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Thymeleaf -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- jBCrypt -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>

<!-- WebAuthn (Yubico) -->
<dependency>
    <groupId>com.yubico</groupId>
    <artifactId>webauthn-server-core</artifactId>
    <version>2.5.0</version>
</dependency>

<!-- Tesseract OCR (Tess4j) -->
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.11.0</version>
</dependency>
```

---

## Interfaz de Usuario

### Características visuales

- **Tema Matrix:** Fondo animado con lluvia de caracteres verdes
- **Diseño terminal:** Estilo cyberpunk con efectos de escaneo
- **Responsive:** Adaptable a dispositivos móviles
- **Drag & Drop:** Subida de imágenes mediante arrastre
- **Tabs dinámicos:** Navegación entre operaciones

### Páginas principales

1. **Login** - [`login.html`](src/main/resources/templates/auth/login.html)
2. **Dashboard** - [`dashboard.html`](src/main/resources/templates/dashboard/dashboard.html)
3. **Criptografía** - [`crypto.html`](src/main/resources/templates/modules/crypto.html)
4. **Esteganografía** - [`Steganography.html`](src/main/resources/templates/modules/Steganography.html)
5. **OCR** - [`Art.html`](src/main/resources/templates/modules/Art.html)
6. **ASCII Art** - [`AsciiArt.html`](src/main/resources/templates/modules/AsciiArt.html)
7. **Seguridad** - [`security.html`](src/main/resources/templates/auth/security.html)

---

## API REST - WebAuthn

### Registro de Credencial

```javascript
// 1. Obtener opciones
GET /webauthn/register/options?email=user@example.com&nombre=Juan

// 2. Enviar respuesta del navegador
POST /webauthn/register/finish
Content-Type: application/json
{
    "id": "...",
    "rawId": "...",
    "type": "public-key",
    "response": {
        "attestationObject": "...",
        "clientDataJSON": "..."
    },
    "clientExtensionResults": {}
}
```

### Login con Credencial

```javascript
// 1. Obtener challenge
GET /webauthn/login/options?email=user@example.com

// 2. Enviar respuesta del navegador
POST /webauthn/login/finish
Content-Type: application/json
{
    "id": "...",
    "rawId": "...",
    "type": "public-key",
    "response": {
        "authenticatorData": "...",
        "clientDataJSON": "...",
        "signature": "...",
        "userHandle": "..."
    },
    "clientExtensionResults": {}
}
```

---

## 🧪 Testing

```bash
# Ejecutar tests
./mvnw test

# Test específico
./mvnw test -Dtest=ArtSteganographyApplicationTests
```

---

##  Límites y Consideraciones

### Esteganografía LSB

- **Capacidad:** Cada píxel RGB puede almacenar 3 bits
- **Fórmula:** `capacidad_máxima = ancho × alto × 3 bits`
- **Recomendado:** Imágenes PNG para evitar compresión con pérdida
- **Advertencia:** Imágenes muy pequeñas no soportan mensajes largos

### OCR Tesseract

- **Idioma:** Inglés (requiere `eng.traineddata`)
- **Formato:** PNG, JPG, BMP soportados
- **Calidad:** Mejor resultado con imágenes nítidas y buen contraste

---

##  Contribuir

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Añadir nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---


##  Autor

Adrian Acosta & Angel Salgado

**Pixel To Art And Steganography Team**

---

