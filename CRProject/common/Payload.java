package CRProject.common;

import java.io.Serializable;

public class Payload implements Serializable {
    // read https://www.baeldung.com/java-serial-version-uid
    private static final long serialVersionUID = 3L;// change this if the class changes

    /**
     * Determines how to process the data on the receiver's side
     */
    private PayloadType payloadType;

    /**
     * Getter method for payload type.
     * 
     * @return
     */
    public PayloadType getPayloadType() {
        return payloadType;
    }

    /**
     * Setter method for payload type.
     * Takes one parameter, payload type
     * 
     * @param payloadType
     */
    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * Who the payload is from
     */
    private String clientName;

    /**
     * Getter method for client name.
     * 
     * @return
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Setter method for client name.
     * Takes one parameter, client name
     * 
     * @param clientName
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    private long clientId;

    /**
     * Getter method for client ID.
     * 
     * @return
     */
    public long getClientId() {
        return clientId;
    }

    /**
     * Setter method for client ID.
     * Takes one parameter, client ID.
     * 
     * @param clientId
     */
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    /**
     * Generic text based message
     */
    private String message;

    /**
     * Getter method for message.
     * 
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter method for message.
     * Takes one parameter, message.
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Generic number for example sake
     */
    private int number;

    /**
     * Getter method for number
     * 
     * @return
     */
    public int getNumber() {
        return number;
    }

    /**
     * Setter method for number.
     * Takes one parameter, number.
     * 
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return String.format("ClientId[%s], ClientName[%s], Type[%s], Number[%s], Message[%s]", getClientId(),
                getClientName(), getPayloadType().toString(), getNumber(),
                getMessage());
    }
}