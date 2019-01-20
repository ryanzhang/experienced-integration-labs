package com.redhat.outbound.service;


import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.ResponseWrapper;

@WebService
public interface NextgateWS {

    CorporateAccount updateAccount(Person account);

}
