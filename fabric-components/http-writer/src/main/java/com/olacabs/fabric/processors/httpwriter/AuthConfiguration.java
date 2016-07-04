package com.olacabs.fabric.processors.httpwriter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by avanish.pandey on 04/07/16.
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthConfiguration {
    private String username;
    private String password;
}