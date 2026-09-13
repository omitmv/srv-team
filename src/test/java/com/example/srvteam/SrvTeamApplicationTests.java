package com.example.srvteam;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest(properties = {
    "DB_URL=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
    "DB_USERNAME=sa",
    "DB_PASSWORD=",
    "JWT_SECRET=ChangeThisJwtSecretKeyToASecureValue32BytesLongForTests",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SrvTeamApplicationTests {

  @Autowired
  private Environment environment;

  @Test
  void contextLoadsWithExternalizedConfiguration() {
    assertThat(environment.getProperty("spring.datasource.url")).contains("jdbc:h2:mem:testdb");
    assertThat(environment.getProperty("jwt.secret"))
        .isEqualTo("ChangeThisJwtSecretKeyToASecureValue32BytesLongForTests");
  }
}
