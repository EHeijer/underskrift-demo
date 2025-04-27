package com.example.underskrift_export.utils;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SqlWithParams {
    private String sql;
    private List<Object> params;
}
