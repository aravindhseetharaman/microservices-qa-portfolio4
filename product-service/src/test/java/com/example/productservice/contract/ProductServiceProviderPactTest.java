package com.example.productservice.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import com.example.productservice.model.Category;
import com.example.productservice.model.Product;
import com.example.productservice.model.Supplier;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

/**
 * Provider verification test for the contract order-service depends on.
 *
 * This spins up the real product-service Spring context on a random port,
 * seeds the database into whatever state each interaction's @State method
 * describes, then replays every interaction recorded in the Pact fetched
 * from the Pact Broker against the running app and checks the actual
 * response matches what the contract promises.
 *
 * The broker connection is configured via the pactbroker.* system
 * properties below (all overridable, e.g.
 * -Dpactbroker.url=https://your-org.pactflow.io -Dpactbroker.token=...).
 * Defaults point at the local broker started by
 * docker-compose.pact-broker.yml / spun up automatically in CI — see
 * .github/workflows/contract-tests.yml.
 *
 * Verification results (pass/fail) are published back to the broker so its
 * UI shows whether product-service currently satisfies order-service's
 * contract — the basis for a "can I deploy?" check in a real pipeline.
 */
@Provider("product-service")
@PactBroker(
        url = "${pactbroker.url:http://localhost:9292}",
        authentication = @PactBrokerAuth(token = "${pactbroker.token:}"),
        consumerVersionSelectors = {
                @VersionSelector(tag = "${pactbroker.consumertag:main}", latest = "true")
        }
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductServiceProviderPactTest {

    static {
        // Publish verification results (pass/fail) back to the broker so it
        // can answer "has this consumer's contract ever been satisfied by
        // this version of product-service?" Off by default so local runs
        // against a throwaway broker don't pollute anything; CI turns it on.
        System.setProperty("pact.verifier.publishResults",
                System.getProperty("pact.verifier.publishResults", "false"));
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    /**
     * Both interactions in the contract require the same fixture: product
     * id 1, nested under "Laptops & Accessories" -> "Electronics", with a
     * supplier attached. @DirtiesContext above guarantees a fresh in-memory
     * H2 database (and therefore a fresh identity sequence starting at 1)
     * for each state, so the hardcoded /products/1 path in the contract
     * always resolves.
     */
    @State({"product with id 1 exists", "product with id 1 exists and has sufficient stock"})
    void productWithId1Exists() {
        Category electronics = new Category();
        electronics.setName("Electronics");
        electronics.setDescription("All electronics");
        electronics = categoryRepository.save(electronics);

        Category laptopsAccessories = new Category();
        laptopsAccessories.setName("Laptops & Accessories");
        laptopsAccessories.setDescription("Laptop peripherals");
        laptopsAccessories.setParentCategory(electronics);
        laptopsAccessories = categoryRepository.save(laptopsAccessories);

        Product product = new Product();
        product.setName("Wireless Mouse");
        product.setDescription("Ergonomic wireless mouse");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(100);
        product.setCategory(laptopsAccessories);
        product.setSupplier(new Supplier(
                "Acme Peripherals Ltd", "sales@acmeperipherals.example", "+1-555-0100", null));

        productRepository.save(product);
    }
}
