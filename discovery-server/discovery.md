revshop/
├── discovery-server/          # Eureka – 8761
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── api-gateway/               # Spring Cloud Gateway + JWT check – 8080
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── user-service/              # register, login, JWT, profile, addresses, notifications, admin users – 8081
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── product-service/           # products, categories, S3 photos, stock, reviews – 8082
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── cart-service/              # cart + wishlist – 8083
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── order-service/             # checkout, orders, cancel, seller orders, dashboard – 8084
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── payment-service/           # mock Pay/Cancel + COD – 8085
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── angular-frontend/          # Angular 21 app
├── .github/
│   └── workflows/
│       ├── deploy-discovery.yml
│       ├── deploy-gateway.yml
│       ├── deploy-user.yml
│       ├── deploy-product.yml
│       ├── deploy-cart.yml
│       ├── deploy-order.yml
│       ├── deploy-payment.yml
│       └── deploy-frontend.yml
├── compose.yaml               # runs all services together
├── .gitignore
└── README.md


*No catalog-service.* Your trainer's example has one, but in your SRS and SDD the categories and catalogue live inside product-service. Don't add it, or your docs won't match your code.

*Inside every Java service* (user-service shown; the others follow the same pattern):


user-service/src/main/java/com/revshop/user/
├── UserServiceApplication.java
├── controller/     # REST endpoints
├── service/        # business logic
├── repository/     # JPA repositories
├── entity/         # tables
├── dto/            # request/response objects
├── client/         # Feign clients (calls to other services)
├── config/         # security, CORS, beans
└── exception/      # global error handler
src/main/resources/application.yml


*What to tick in Spring Initializr* (Java 21, Spring Boot 4.x, Maven):

| Service | Dependencies |
|---|---|
| discovery-server | Eureka Server |
| api-gateway | Reactive Gateway, Eureka Discovery Client (add the jjwt library to pom.xml yourself) |
| user-service | Spring Web, Spring Data JPA, MySQL Driver, Validation, Eureka Discovery Client, Spring Security, Lombok |
| product-service | Same as user-service minus Security, plus OpenFeign (add AWS SDK S3 to pom.xml yourself) |
| cart-service | Spring Web, JPA, MySQL Driver, Validation, Eureka Client, OpenFeign, Lombok |
| order-service | Spring Web, JPA, MySQL Driver, Validation, Eureka Client, OpenFeign, Lombok |
| payment-service | Spring Web, JPA, MySQL Driver, Validation, Eureka Client, OpenFeign, Lombok |

*Angular frontend:*


angular-frontend/src/app/
├── core/           # auth service, JWT interceptor, guards
├── shared/         # navbar, product card, common components
└── features/
├── auth/       # login, register
├── products/   # catalogue, search, product details
├── cart/       # cart, wishlist
├── checkout/   # checkout + Pay/Cancel popup
├── orders/     # my orders
├── seller/     # seller products, orders, dashboard
└── admin/      # users, categories, monitoring
