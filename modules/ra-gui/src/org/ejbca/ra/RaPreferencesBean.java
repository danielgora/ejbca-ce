package org.ejbca.ra;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.log4j.Logger;
import org.cesecore.config.RaStyleInfo;
import org.ejbca.core.ejb.ra.raadmin.AdminPreferenceSessionLocal;
import org.ejbca.ui.web.admin.administratorprivileges.RolesBean;

/**
 * 
 * @version $Id$
 *
 */

@ManagedBean
@ViewScoped
public class RaPreferencesBean implements Converter, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RaPreferencesBean.class);

    @EJB
    private AdminPreferenceSessionLocal adminPreferenceSession;

    @ManagedProperty(value = "#{raLocaleBean}")
    private RaLocaleBean raLocaleBean;

    public void setRaLocaleBean(final RaLocaleBean raLocaleBean) {
        this.raLocaleBean = raLocaleBean;
    }

    @ManagedProperty(value = "#{raAuthenticationBean}")
    private RaAuthenticationBean raAuthenticationBean;

    public void setRaAuthenticationBean(RaAuthenticationBean raAuthenticationBean) {
        this.raAuthenticationBean = raAuthenticationBean;
    }
    
    @ManagedProperty(value= "#{rolesBean}")
    private RolesBean rolesBean;
    
    public void setRoleBean(RolesBean rolesBean) {
        this.rolesBean = rolesBean;
    }

    private Locale currentLocale;

    private RaStyleInfo currentStyle;

    @PostConstruct
    public void init() {
        initLocale();
        initRaStyle();
    }

    public RaStyleInfo getCurrentStyle() {
        return currentStyle;
    }

    public void setCurrentStyle(final RaStyleInfo currentStyle) {
        this.currentStyle = currentStyle;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(final Locale locale) {
        this.currentLocale = locale;
        raLocaleBean.setLocale(locale);
    }

    public List<Locale> getLocales() {
        return raLocaleBean.getSupportedLocales();
    }

    public List<RaStyleInfo> getStyles() {

        List<RaStyleInfo> raStyleInfos = adminPreferenceSession.getAvailableRaStyleInfos(raAuthenticationBean.getAuthenticationToken());

        RaStyleInfo dummStyleInfo = new RaStyleInfo("Default", null, null, "");

        dummStyleInfo.setArchiveId(0);

        if (raStyleInfos.contains(dummStyleInfo)) {
            return raStyleInfos;
        } else {
            raStyleInfos.add(0, dummStyleInfo);
            return raStyleInfos;
        }
    }

    /**
     * Updates ra admin preferences data in database.
     * Does nothings in case the current data is equal to the data going to be set.
     */
    public void updatePreferences() {
        adminPreferenceSession.setCurrentRaLocale(currentLocale, raAuthenticationBean.getAuthenticationToken());
        adminPreferenceSession.setCurrentRaStyleId(currentStyle.getArchiveId(), raAuthenticationBean.getAuthenticationToken());
    }

    /**
     * The following two methods are used in converting RaStyleInfo to String and vice versa.
     * Required by JSF.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        List<RaStyleInfo> styleInfos = adminPreferenceSession.getAvailableRaStyleInfos(raAuthenticationBean.getAuthenticationToken());

        for (RaStyleInfo raStyleInfo : styleInfos) {
            if (raStyleInfo.getArchiveName().equals(value)) {
                return raStyleInfo;
            }
        }

        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {

        RaStyleInfo raStyleInfo = (RaStyleInfo) value;

        return raStyleInfo.getArchiveName();
    }

    /**
     * Used to reset the preferences page
     * @return
     */
    public String reset() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return viewId + "?faces-redirect=true";
    }

    private void initLocale() {
        Locale localeFromDB = adminPreferenceSession.getCurrentRaLocale(raAuthenticationBean.getAuthenticationToken());

        if (localeFromDB != null) {
            currentLocale = localeFromDB;
        } else {
            currentLocale = raLocaleBean.getLocale();
        }

    }

    private void initRaStyle() {

        Integer raStyleFromDB = adminPreferenceSession.getCurrentRaStyleId(raAuthenticationBean.getAuthenticationToken());

        if (raStyleFromDB != null) {

            List<RaStyleInfo> raStyleInfos = adminPreferenceSession.getAvailableRaStyleInfos(raAuthenticationBean.getAuthenticationToken());

            for (RaStyleInfo raStyleInfo : raStyleInfos) {
                if (raStyleInfo.getArchiveId() == raStyleFromDB) {
                    currentStyle = raStyleInfo;
                    return;
                }
            }
        }

    }

}
