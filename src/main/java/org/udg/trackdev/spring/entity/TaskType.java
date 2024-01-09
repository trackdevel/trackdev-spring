package org.udg.trackdev.spring.entity;

public enum TaskType {
    USER_STORY("HISTORIA D'USUARI"),
    TASK("TASCA"),
    ;

    private final String text;

    /**
     * @param text
     */
    TaskType(final String text) {
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
