package io.aime.mvc.model;

import io.aime.mvc.controller.DefaultController;
import net.apkc.emma.mvc.AbstractModel;

public final class DefaultModel extends AbstractModel
{

    private String aboutText = "";

    public void setAboutText(String newAboutText)
    {
        String oldAboutText = aboutText;
        aboutText = newAboutText;
        firePropertyChange(DefaultController.ABOUT_TEXT_PROPERTY, oldAboutText, newAboutText);
    }

    public String getAboutText()
    {
        return aboutText;
    }
}
