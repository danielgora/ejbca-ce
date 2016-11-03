/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.ui.web.admin;

import java.text.ParseException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;
import org.cesecore.util.SimpleTime;
import org.cesecore.util.StringTools;
import org.cesecore.util.ValidityDate;
import org.ejbca.ui.web.admin.configuration.EjbcaJSFHelper;

/** JSF validator to check that the input does not contain any invalid characters and is a valid time unit format (i.e. '3y 6mo -10d 6h 30m 30s') 
 *  or a validity end date (ISO8601 format, i.e. 'yyyy-MM-dd HH:mm:ssZZ', 'yyyy-MM-dd HH:mmZZ' or 'yyyy-MM-ddZZ' with optional '+00:00' appended). 
 * 
 * @version $Id$
 */
public class ValidityDateValidator implements Validator {

    private static final Logger log = Logger.getLogger(ValidityDateValidator.class);

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object object) throws ValidatorException {
        final String value = (String) object;
        boolean failed = true;
        if (!StringTools.hasSqlStripChars(value)) {
            // parse ISO8601
            try {
                // ANJAKOBS: before 6.6.1 range was a little more than epoch to 9999-12-31 23:59:59+01:00, now min value is '0001-01-01 00:00:00'
                ValidityDate.parseAsIso8601(value);
                failed = false;
            } catch (ParseException e) {
                // NOOP
            }
            if (failed) {
                // parse time unit format
                try {
                    if (SimpleTime.getSecondsFormat().parseMillis(value) > -1) {
                        failed = false;
                    }
                } catch (NumberFormatException e) {
                    // NOOP
                }
            }
        }
        if (failed) {
            if (log.isDebugEnabled()) {
                log.debug("Validating component " + component.getClientId(facesContext) + " with value \"" + value + "\" failed.");
            }
            final String message = EjbcaJSFHelper.getBean().getEjbcaWebBean().getText("INVALIDTIMEFORMAT");
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Validating component " + component.getClientId(facesContext) + " with value \"" + value + "\"");
            }
        }
    }
}
