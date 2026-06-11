package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("webauthn")
public class AuthController {
    // ---------REGISTER MAPPING---------
    @GetMapping("/register/options")
    public String registerOptions() {
        return "register options";
    }

    @PostMapping("/register/finish")
    public String registerFinish() {
        return "register finish";
    }

    // ---------LOGIN MAPPING---------
    @GetMapping("/login/options")
    public String loginOptions() {
        return "login options";
    }

    @PostMapping("/login/finish")
    public String loginFinish() {
        return "login finish";
    }
}
