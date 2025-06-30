package com.petshop.login.config;

import com.petshop.login.model.NivelAcesso;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NivelAcessoConverter implements Converter<String, NivelAcesso> {

    @Override
    public NivelAcesso convert(String source) {
        return NivelAcesso.fromValue(source);
    }
}
