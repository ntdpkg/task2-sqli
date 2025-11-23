package com.vulnapp.utils;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;

public class FreeMarkerConfig {
    private static Configuration cfg = null;

    public static Configuration getConfiguration() throws IOException {
        if (cfg == null) {
            cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setClassLoaderForTemplateLoading(FreeMarkerConfig.class.getClassLoader(), "/templates");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
        }
        return cfg;
    }
}