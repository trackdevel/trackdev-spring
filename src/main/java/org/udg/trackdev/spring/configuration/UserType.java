package org.udg.trackdev.spring.configuration;

public enum UserType {
    ADMIN("ADMIN"),
    PROFESSOR("PROFESSOR"),
    STUDENT("STUDENT")
    ;

    private final String text;

    /**
     * @param text
     */
    UserType(final String text) {
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