package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

public class AppTest {

    @Test
    public void testMain() {
        try {
            App.main(null);
        } catch (Exception ex) {
            fail(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}