/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
 
package se.anatom.ejbca.webdist.webconfiguration;

import java.io.IOException;

import se.anatom.ejbca.ra.raadmin.GlobalConfiguration;

/**
 * An class interpreting the langage properties files. I contains one method getText that returns
 * the presented text in the users prefered language.
 *
 * @author  Philip Vendil
 * @version $Id: WebLanguages.java,v 1.13 2004-04-16 07:39:01 anatom Exp $
 */
public class WebLanguages {

    /** Construtor used to load static content. An instance must be declared with this constructor before
     *  any WebLanguage object can be used. */
    /** Special constructor used by Ejbca web bean */
    public WebLanguages(GlobalConfiguration globalconfiguration) throws IOException {
      if(languages == null){
        // Get available languages.
         availablelanguages=null;

         String availablelanguagesstring = globalconfiguration .getAvailableLanguagesAsString();
         availablelanguages =  availablelanguagesstring.split(",");
         for(int i=0; i < availablelanguages.length;i++){
           availablelanguages[i] =  availablelanguages[i].trim().toUpperCase();
         }
           // Load availabe languages
         languages = new LanguageProperties[availablelanguages.length];
         for(int i = 0; i < availablelanguages.length; i++){
           languages[i] = new LanguageProperties();
           languages[i].load(this.getClass().getResourceAsStream("/" + globalconfiguration .getLanguagePath() + "/"
                                                                    + globalconfiguration .getLanguageFilename() + "."
                                                                    + availablelanguages[i].toLowerCase() +".properties"));
         }
      }
    }

    public WebLanguages(int preferedlang, int secondarylang) {
      this.userspreferedlanguage=preferedlang;
      this.userssecondarylanguage=secondarylang;
    }


    /** The main method that looks up the template text in the users prefered language. */
    public  String getText(String template){
      String returnvalue = null;
      try{
        returnvalue= languages[userspreferedlanguage].getProperty(template);
        if(returnvalue == null)
          returnvalue= languages[userssecondarylanguage].getProperty(template);

      }catch(java.lang.NullPointerException e){}
      if(returnvalue == null)
        returnvalue= "No text available";
      return returnvalue;
    }

    /* Returns a textstring containing the available languages */
    public static String[] getAvailableLanguages(){
      return availablelanguages;
    }


    // Protected fields
    private int userspreferedlanguage;
    private int userssecondarylanguage;

    private static String[] availablelanguages;
    private static LanguageProperties[] languages = null;

}
