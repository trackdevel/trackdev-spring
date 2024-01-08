package org.udg.trackdev.spring.entity;

public enum TaskStatus {
    BACKLOG("BACKLOG"),
    TODO("PRIORITZADA"),
    INPROGRESS("EN PROGRES"),
    VERIFY("EN VERIFICACIO"),
    DONE("FINALITZADA"),
    DEFINED("DEFINIDA"),
    ;

    private final String text;
    TaskStatus(final String text) {
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