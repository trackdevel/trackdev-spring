package org.udg.trackdev.spring.entity;

public enum SprintStatus {
    DRAFT("PLANIFICAT"),
    ACTIVE("ACTIU"),
    CLOSED("TANCAT"),
    ;

    private final String text;
    SprintStatus(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}