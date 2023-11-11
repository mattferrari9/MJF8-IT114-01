package CRProject.common;

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Determines how to process the data on the receiver's side
     */
    private PayloadType payloadType;

    /**
     * Retrieves the type of payload.
     *
     * @return The type of payload as a PayloadType enumeration.
     */
    public PayloadType getPayloadType() {
        return payloadType;
    }

    /**
     * Sets the type of payload.
     *
     * @param payloadType The type of payload to be set.
     */
    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * Who the payload is from
     */
    private String clientName;

    public String getClientName() {
        return clientName;
    }

    /**
     * Setter method for clientName
     * Takes one parameter, clientName
     * 
     * @param clientName
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /** 
     * Generic string message
    */
    private String message;

    public String getMessage() {
        return message;
    }

    /**
     * Setter method for message
     * Takes one parameter, message
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

    public int getNumber() {
        return number;
    }

    /**
     * Setter method for number
     * Takes one parameter, number
     * 
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * toString method for all parameters
     * 
     * @param null
     */
    @Override
    public String toString() {
        return String.format("Type[%s], Number[%s], Message[%s]", getPayloadType().toString(), getNumber(),
                getMessage());
    }
}