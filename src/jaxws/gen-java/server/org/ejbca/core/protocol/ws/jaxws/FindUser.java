
package org.ejbca.core.protocol.ws.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.ejbca.core.protocol.ws.objects.UserMatch;

@XmlRootElement(name = "findUser", namespace = "http://ws.protocol.core.ejbca.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "findUser", namespace = "http://ws.protocol.core.ejbca.org/")
public class FindUser {

    @XmlElement(name = "arg0", namespace = "")
    private UserMatch arg0;

    /**
     * 
     * @return
     *     returns UserMatch
     */
    public UserMatch getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(UserMatch arg0) {
        this.arg0 = arg0;
    }

}
