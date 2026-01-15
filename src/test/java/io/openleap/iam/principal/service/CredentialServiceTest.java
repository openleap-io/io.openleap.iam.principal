package io.openleap.iam.principal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CredentialService Unit Tests")
class CredentialServiceTest {

    private CredentialService credentialService;

    @BeforeEach
    void setUp() {
        credentialService = new CredentialService();
    }

    @Nested
    @DisplayName("generateApiKey")
    class GenerateApiKey {

        @Test
        @DisplayName("should generate API key with correct prefix")
        void shouldGenerateApiKeyWithCorrectPrefix() {
            // given
            String expectedPrefix = "sk_live_";

            // when
            String apiKey = credentialService.generateApiKey();

            // then
            assertThat(apiKey).startsWith(expectedPrefix);
        }

        @Test
        @DisplayName("should generate API key with sufficient length")
        void shouldGenerateApiKeyWithSufficientLength() {
            // given
            int prefixLength = "sk_live_".length();
            int minBase64Length = 32; // 32 bytes base64 encoded

            // when
            String apiKey = credentialService.generateApiKey();

            // then
            assertThat(apiKey.length()).isGreaterThanOrEqualTo(prefixLength + minBase64Length);
        }

        @Test
        @DisplayName("should generate unique API keys")
        void shouldGenerateUniqueApiKeys() {
            // given
            // when
            String apiKey1 = credentialService.generateApiKey();
            String apiKey2 = credentialService.generateApiKey();
            String apiKey3 = credentialService.generateApiKey();

            // then
            assertThat(apiKey1).isNotEqualTo(apiKey2);
            assertThat(apiKey2).isNotEqualTo(apiKey3);
            assertThat(apiKey1).isNotEqualTo(apiKey3);
        }

        @Test
        @DisplayName("should generate URL-safe API key")
        void shouldGenerateUrlSafeApiKey() {
            // given
            // when
            String apiKey = credentialService.generateApiKey();
            String encodedPart = apiKey.substring("sk_live_".length());

            // then
            // URL-safe base64 should not contain + or /
            assertThat(encodedPart).doesNotContain("+");
            assertThat(encodedPart).doesNotContain("/");
            assertThat(encodedPart).doesNotContain("="); // no padding
        }
    }

    @Nested
    @DisplayName("hashApiKey")
    class HashApiKey {

        @Test
        @DisplayName("should return 64-character hex string for SHA-256")
        void shouldReturn64CharacterHexString() {
            // given
            String apiKey = "sk_live_testApiKey123";

            // when
            String hash = credentialService.hashApiKey(apiKey);

            // then
            assertThat(hash).hasSize(64);
            assertThat(hash).matches("[a-f0-9]+");
        }

        @Test
        @DisplayName("should produce consistent hash for same input")
        void shouldProduceConsistentHashForSameInput() {
            // given
            String apiKey = "sk_live_consistentTestKey";

            // when
            String hash1 = credentialService.hashApiKey(apiKey);
            String hash2 = credentialService.hashApiKey(apiKey);

            // then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("should produce different hashes for different inputs")
        void shouldProduceDifferentHashesForDifferentInputs() {
            // given
            String apiKey1 = "sk_live_firstKey";
            String apiKey2 = "sk_live_secondKey";

            // when
            String hash1 = credentialService.hashApiKey(apiKey1);
            String hash2 = credentialService.hashApiKey(apiKey2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("should hash empty string without error")
        void shouldHashEmptyStringWithoutError() {
            // given
            String emptyApiKey = "";

            // when
            String hash = credentialService.hashApiKey(emptyApiKey);

            // then
            assertThat(hash).hasSize(64);
            // SHA-256 of empty string is known
            assertThat(hash).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        }

        @Test
        @DisplayName("should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // given
            String unicodeApiKey = "sk_live_テスト鍵";

            // when
            String hash = credentialService.hashApiKey(unicodeApiKey);

            // then
            assertThat(hash).hasSize(64);
            assertThat(hash).matches("[a-f0-9]+");
        }
    }

    @Nested
    @DisplayName("Integration: generateApiKey and hashApiKey")
    class GenerateAndHashIntegration {

        @Test
        @DisplayName("should be able to hash generated API key")
        void shouldBeAbleToHashGeneratedApiKey() {
            // given
            String apiKey = credentialService.generateApiKey();

            // when
            String hash = credentialService.hashApiKey(apiKey);

            // then
            assertThat(hash).hasSize(64);
            assertThat(hash).matches("[a-f0-9]+");
        }

        @Test
        @DisplayName("should produce different hashes for each generated key")
        void shouldProduceDifferentHashesForEachGeneratedKey() {
            // given
            String apiKey1 = credentialService.generateApiKey();
            String apiKey2 = credentialService.generateApiKey();

            // when
            String hash1 = credentialService.hashApiKey(apiKey1);
            String hash2 = credentialService.hashApiKey(apiKey2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }
    }
}
