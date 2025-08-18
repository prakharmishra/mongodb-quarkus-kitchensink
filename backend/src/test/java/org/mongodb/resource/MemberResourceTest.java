package org.mongodb.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.SecurityAttribute;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class MemberResourceTest {

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testListMembersAsAdmin() {
        given()
            .when().get("/api/members")
            .then()
            .statusCode(200)
            .body("data", notNullValue());
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    @JwtSecurity(claims = {@Claim(key = "email", value = "test@asdf.com")})
    void testListMembersAsUser() {
        given()
            .when().get("/api/members")
            .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testListMembersWithInvalidSize() {
        given()
            .queryParam("size", -1)
            .when().get("/api/members")
            .then()
            .statusCode(400)
            .body(equalTo("Size must be greater than 0"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testGetMemberByIdNotFound() {
        given()
            .when().get("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testUpdateMemberWithValidData() {
        String memberJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "phoneNumber": "1234567890"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(memberJson)
            .when().put("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testUpdateMemberWithBlankId() {
        String memberJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "phoneNumber": "1234567890"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(memberJson)
            .when().put("/api/members/ ")
            .then()
            .statusCode(405);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testUpdateMemberWithInvalidData() {
        String memberJson = """
            {
                "firstName": "",
                "lastName": "Doe",
                "phoneNumber": "invalid"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(memberJson)
            .when().put("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testDeleteMember() {
        given()
            .when().delete("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void testDeleteMemberWithBlankId() {
        given()
            .when().delete("/api/members/ ")
            .then()
            .statusCode(405);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void testDeleteMemberAsUserForbidden() {
        given()
            .when().delete("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(403);
    }

    @Test
    void testEndpointsRequireAuthentication() {
        given()
            .when().get("/api/members")
            .then()
            .statusCode(401);

        given()
            .when().get("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(401);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when().put("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(401);

        given()
            .when().delete("/api/members/507f1f77bcf86cd799439011")
            .then()
            .statusCode(401);
    }
}