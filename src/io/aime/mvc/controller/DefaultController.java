package io.aime.mvc.controller;

import net.apkc.emma.mvc.AbstractController;

public final class DefaultController extends AbstractController
{

    public static final String ABOUT_TEXT_PROPERTY = "AboutText";

    public void changeAboutText(String newAboutText)
    {
        setModelProperty(ABOUT_TEXT_PROPERTY, newAboutText);
    }
}
