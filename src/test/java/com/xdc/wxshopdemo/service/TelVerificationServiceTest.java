package com.xdc.wxshopdemo.service;

import com.xdc.wxshopdemo.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TelVerificationServiceTest {
    public static AuthController.TelAndCode VALID_PARAMETER =
            new AuthController.TelAndCode("15622958503", null);
    public static AuthController.TelAndCode VALID_PARAMETER_CODE =
            new AuthController.TelAndCode("15622958503", "000000");
    public static AuthController.TelAndCode EMPTY_TEL =
            new AuthController.TelAndCode(null, null);

    @Test
    public void returnTrueIfValid() {
        Assertions.assertTrue(new TelVerificationService().verifyTelParameter(VALID_PARAMETER));
    }

    @Test
    public void returnFalseIfNoTel() {
        Assertions.assertFalse(new TelVerificationService().verifyTelParameter(EMPTY_TEL));
        Assertions.assertFalse(new TelVerificationService().verifyTelParameter(null));
    }
}
